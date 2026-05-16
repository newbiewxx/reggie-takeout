package com.wxx.controller; // 控制器包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus 条件构造器
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // MyBatis-Plus 分页对象
import com.wxx.common.R; // 统一响应结果封装类
import com.wxx.domain.Category; // 分类实体类
import com.wxx.domain.Dish; // 菜品实体类
import com.wxx.dto.DishDto; // 菜品 DTO
import com.wxx.domain.DishFlavor; // 菜品口味实体类
import com.wxx.service.CategoryService; // 分类服务接口
import com.wxx.service.DishFlavorService; // 菜品口味服务接口
import com.wxx.service.DishService; // 菜品服务接口
import lombok.RequiredArgsConstructor; // Lombok：生成带 final 字段的构造器注入
import lombok.extern.slf4j.Slf4j; // Lombok：日志对象
import org.apache.commons.lang.StringUtils; // 字符串工具类
import org.springframework.beans.BeanUtils; // Spring Bean 属性拷贝
import org.springframework.web.bind.annotation.DeleteMapping; // DELETE 请求映射注解
import org.springframework.web.bind.annotation.GetMapping; // GET 请求映射注解
import org.springframework.web.bind.annotation.PathVariable; // URL 路径变量绑定注解
import org.springframework.web.bind.annotation.PostMapping; // POST 请求映射注解
import org.springframework.web.bind.annotation.PutMapping; // PUT 请求映射注解
import org.springframework.web.bind.annotation.RequestBody; // 请求体 JSON 绑定
import org.springframework.web.bind.annotation.RequestMapping; // 类级别请求映射
import org.springframework.web.bind.annotation.RequestParam; // 请求参数绑定注解
import org.springframework.web.bind.annotation.RestController; // REST 控制器注解

import java.util.Arrays; // 数组工具
import java.util.Collections; // 空集合
import java.util.List; // 集合接口
import java.util.Map; // 键值对集合
import java.util.Objects; // 对象工具
import java.util.Set; // 集合接口
import java.util.stream.Collectors; // 流式处理

@RestController // 组合注解 = @Controller + @ResponseBody，返回 JSON
@Slf4j // 自动生成 log 日志对象
@RequestMapping("dish") // 请求映射前缀：/dish
@RequiredArgsConstructor // 为 final 字段生成构造器（Spring 自动注入）
public class DishController {

    private final DishService dishService; // 菜品服务（构造器注入）
    private final CategoryService categoryService; // 分类服务（构造器注入）
    private final DishFlavorService dishFlavorService; // 菜品口味服务（构造器注入）

    /**
     * 菜品分页查询
     * @param page     当前页码
     * @param pageSize 每页条数
     * @param name     可选：按菜品名称模糊筛选
     * @return 统一响应结果（含分页数据，附带分类名称）
     */
    @GetMapping("/page") // GET /dish/page?page=1&pageSize=10&name=宫保
    public R<Page<DishDto>> page(@RequestParam int page, @RequestParam int pageSize, String name) {
        log.info("菜品分页查询 - page={}, pageSize={}, name={}", page, pageSize, name);

        // 1. 创建分页对象并查询 Dish 原始数据
        Page<Dish> dishPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage, queryWrapper);

        // 2. 将 Dish 记录转换为 DishDto，填充分类名称
        Page<DishDto> dtoPage = new Page<>(dishPage.getCurrent(), dishPage.getSize(), dishPage.getTotal());

        // 批量查询分类名称：收集所有 categoryId 一次查完
        Set<Long> categoryIds = dishPage.getRecords().stream()
                .map(Dish::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap() :
                categoryService.listByIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName));

        List<DishDto> dtoList = dishPage.getRecords().stream().map(dish -> {
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(dish, dto);
            dto.setCategoryName(categoryMap.get(dish.getCategoryId()));
            return dto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return R.success(dtoPage);
    }

    /**
     * 根据分类 ID 查询菜品列表（用于下拉选择）
     * @param categoryId 可选：分类 ID
     * @return 菜品列表
     */
    @GetMapping("/list")
    public R<List<Dish>> list(Long categoryId) {
        log.info("菜品列表查询 - categoryId={}", categoryId);

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        return R.success(dishService.list(queryWrapper));
    }

    /**
     * 根据 ID 查询菜品（含口味信息，用于回显）
     * @param id 菜品 ID
     * @return 统一响应结果（含菜品信息 + 口味列表）
     */
    @GetMapping("/{id}") // GET /dish/{id}
    public R<DishDto> get(@PathVariable Long id) {
        log.info("查询菜品 - ID={}", id);

        Dish dish = dishService.getById(id);
        if (dish == null) {
            return R.error("菜品不存在");
        }

        // 复制到 DTO
        DishDto dto = new DishDto();
        BeanUtils.copyProperties(dish, dto);

        // 查询口味列表
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        dto.setFlavors(dishFlavorService.list(queryWrapper));

        return R.success(dto);
    }

    /**
     * 新增菜品（含口味信息）
     * @param dto 菜品信息 + 口味列表
     * @return 统一响应结果
     */
    @PostMapping // POST /dish
    public R<String> save(@RequestBody DishDto dto) {
        log.info("新增菜品 - name={}, categoryId={}, flavors={}", dto.getName(), dto.getCategoryId(),
                dto.getFlavors() != null ? dto.getFlavors().size() : 0);

        // 检查同分类下是否已存在同名菜品（@TableLogic 自动过滤已删除的记录）
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getName, dto.getName())
                    .eq(dto.getCategoryId() != null, Dish::getCategoryId, dto.getCategoryId());
        if (dishService.count(queryWrapper) > 0) {
            return R.error("新增失败，该分类下已存在同名菜品");
        }

        dishService.saveWithFlavors(dto);
        return R.success("新增菜品成功");
    }

    /**
     * 修改菜品（含口味信息）
     * @param dto 菜品信息 + 口味列表
     * @return 统一响应结果
     */
    @PutMapping // PUT /dish
    public R<String> update(@RequestBody DishDto dto) {
        log.info("修改菜品 - ID={}, name={}, categoryId={}, flavors={}", dto.getId(), dto.getName(),
                dto.getCategoryId(), dto.getFlavors() != null ? dto.getFlavors().size() : 0);

        if (dto.getId() == null) {
            return R.error("修改失败，菜品ID不能为空");
        }

        // 检查同分类下是否已存在同名菜品（排除自身）
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getName, dto.getName())
                    .eq(dto.getCategoryId() != null, Dish::getCategoryId, dto.getCategoryId())
                    .ne(Dish::getId, dto.getId());
        if (dishService.count(queryWrapper) > 0) {
            return R.error("修改失败，该分类下已存在同名菜品");
        }

        dishService.updateWithFlavors(dto);
        return R.success("修改菜品成功");
    }

    /**
     * 删除菜品（支持批量，逗号分隔）
     * @param ids 菜品 ID，多个用逗号分隔
     * @return 统一响应结果
     */
    @DeleteMapping // DELETE /dish?ids=xxx 或 /dish?ids=1,2,3
    public R<String> delete(@RequestParam String ids) {
        log.info("删除菜品 - ids={}", ids);

        if (StringUtils.isEmpty(ids)) {
            return R.error("删除失败，菜品ID不能为空");
        }

        // 解析逗号分隔的 ID 列表
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        dishService.deleteWithFlavors(idList);
        return R.success("删除成功");
    }

    /**
     * 批量起售/停售
     * @param status 状态：1=起售，0=停售
     * @param ids    菜品 ID（逗号分隔）
     * @return 统一响应结果
     */
    @PostMapping("/status/{status}") // POST /dish/status/1?ids=1,2,3
    public R<String> status(@PathVariable Integer status,
                            @RequestParam String ids) {
        log.info("修改菜品状态 - status={}, ids={}", status, ids);

        if (StringUtils.isEmpty(ids)) {
            return R.error("操作失败，菜品ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            return R.error("操作失败，状态值无效");
        }

        // 解析逗号分隔的 ID 列表
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // 批量更新状态
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, idList);

        Dish dish = new Dish();
        dish.setStatus(status);
        dishService.update(dish, queryWrapper);
        log.info("状态修改成功 - status={}, ids={}", status, ids);
        return R.success(status == 1 ? "起售成功" : "停售成功");
    }
}
