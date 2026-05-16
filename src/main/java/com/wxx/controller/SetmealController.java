package com.wxx.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxx.common.R;
import com.wxx.domain.Category;
import com.wxx.domain.Setmeal;
import com.wxx.domain.SetmealDish;
import com.wxx.dto.SetmealDto;
import com.wxx.service.CategoryService;
import com.wxx.service.SetmealDishService;
import com.wxx.service.SetmealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("setmeal")
@RequiredArgsConstructor
public class SetmealController {

    private final SetmealService setmealService;
    private final CategoryService categoryService;
    private final SetmealDishService setmealDishService;

    /**
     * 套餐分页查询
     * @param page     当前页码
     * @param pageSize 每页条数
     * @param name     可选：按套餐名称模糊筛选
     * @return 统一响应结果（含分页数据，附带分类名称）
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(@RequestParam int page, @RequestParam int pageSize, String name) {
        log.info("套餐分页查询 - page={}, pageSize={}, name={}", page, pageSize, name);

        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage, queryWrapper);

        Page<SetmealDto> dtoPage = new Page<>(setmealPage.getCurrent(), setmealPage.getSize(), setmealPage.getTotal());

        Set<Long> categoryIds = setmealPage.getRecords().stream()
                .map(Setmeal::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap() :
                categoryService.listByIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName));

        List<SetmealDto> dtoList = setmealPage.getRecords().stream().map(setmeal -> {
            SetmealDto dto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, dto);
            dto.setCategoryName(categoryMap.get(setmeal.getCategoryId()));
            return dto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return R.success(dtoPage);
    }

    /**
     * 根据 ID 查询套餐（含套餐内菜品）
     * @param id 套餐 ID
     * @return 统一响应结果（含套餐信息 + 菜品列表）
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id) {
        log.info("查询套餐 - ID={}", id);

        Setmeal setmeal = setmealService.getById(id);
        if (setmeal == null) {
            return R.error("套餐不存在");
        }

        SetmealDto dto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, dto);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        dto.setSetmealDishes(setmealDishService.list(queryWrapper));

        return R.success(dto);
    }

    /**
     * 新增套餐（含套餐内菜品）
     * @param dto 套餐信息 + 菜品列表
     * @return 统一响应结果
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto dto) {
        log.info("新增套餐 - name={}, categoryId={}, dishes={}", dto.getName(), dto.getCategoryId(),
                dto.getSetmealDishes() != null ? dto.getSetmealDishes().size() : 0);

        setmealService.saveWithDishes(dto);
        return R.success("新增套餐成功");
    }

    /**
     * 修改套餐（含套餐内菜品）
     * @param dto 套餐信息 + 菜品列表
     * @return 统一响应结果
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto dto) {
        log.info("修改套餐 - ID={}, name={}, categoryId={}, dishes={}", dto.getId(), dto.getName(),
                dto.getCategoryId(), dto.getSetmealDishes() != null ? dto.getSetmealDishes().size() : 0);

        if (dto.getId() == null) {
            return R.error("修改失败，套餐ID不能为空");
        }

        setmealService.updateWithDishes(dto);
        return R.success("修改套餐成功");
    }

    /**
     * 删除套餐（支持批量，逗号分隔）
     * @param ids 套餐 ID，多个用逗号分隔
     * @return 统一响应结果
     */
    @DeleteMapping
    public R<String> delete(@RequestParam String ids) {
        log.info("删除套餐 - ids={}", ids);

        if (StringUtils.isEmpty(ids)) {
            return R.error("删除失败，套餐ID不能为空");
        }

        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        setmealService.deleteWithDishes(idList);
        return R.success("删除成功");
    }

    /**
     * 批量起售/停售
     * @param status 状态：1=起售，0=停售
     * @param ids    套餐 ID（逗号分隔）
     * @return 统一响应结果
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status,
                            @RequestParam String ids) {
        log.info("修改套餐状态 - status={}, ids={}", status, ids);

        if (StringUtils.isEmpty(ids)) {
            return R.error("操作失败，套餐ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            return R.error("操作失败，状态值无效");
        }

        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, idList);

        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmealService.update(setmeal, queryWrapper);
        log.info("状态修改成功 - status={}, ids={}", status, ids);
        return R.success(status == 1 ? "起售成功" : "停售成功");
    }
}
