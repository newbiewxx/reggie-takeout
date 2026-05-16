package com.wxx.service.impl; // Service 实现包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus 条件构造器
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl; // MyBatis-Plus 基础实现类
import java.util.List; // 集合接口
import com.wxx.domain.Dish; // 菜品实体类
import com.wxx.domain.DishFlavor; // 菜品口味实体类
import com.wxx.dto.DishDto; // 菜品 DTO
import com.wxx.mapper.DishMapper; // 菜品 Mapper
import com.wxx.service.DishFlavorService; // 菜品口味 Service 接口
import com.wxx.service.DishService; // 菜品 Service 接口
import lombok.RequiredArgsConstructor; // Lombok：构造器注入
import lombok.extern.slf4j.Slf4j; // Lombok：日志对象
import org.springframework.stereotype.Service; // Spring 服务注解
import org.springframework.transaction.annotation.Transactional; // 事务注解

@Service // 声明为 Spring Bean，自动扫描注入
@RequiredArgsConstructor // 为 final 字段生成构造器（Spring 自动注入）
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    private final DishFlavorService dishFlavorService; // 菜品口味服务（构造器注入）

    /**
     * 新增菜品，同时保存口味信息
     * 如果存在已逻辑删除的同名菜品，先将其名称改为 "原名_已被删除"
     * @param dto 菜品信息 + 口味列表
     */
    @Override
    public void saveWithFlavors(DishDto dto) {
        // 查询已逻辑删除的同名菜品，改名以释放唯一约束
        List<Dish> deletedList = baseMapper.selectDeletedByName(dto.getName());
        for (Dish deleted : deletedList) {
            baseMapper.renameDeletedById(deleted.getId(), deleted.getName() + "_已被删除");
            log.info("同名已删除菜品已改名 - ID={}, 原名称={}", deleted.getId(), dto.getName());
        }

        // 1. 保存菜品基本信息
        this.save(dto);
        Long dishId = dto.getId();
        log.info("新增菜品 - ID={}, name={}", dishId, dto.getName());

        // 2. 保存菜品口味
        if (dto.getFlavors() != null && !dto.getFlavors().isEmpty()) {
            for (DishFlavor flavor : dto.getFlavors()) {
                flavor.setDishId(dishId);
            }
            dishFlavorService.saveBatch(dto.getFlavors());
            log.info("新增菜品口味 - 数量={}", dto.getFlavors().size());
        }
    }

    /**
     * 修改菜品，同时更新口味信息（先删后插）
     * 如果存在已逻辑删除的同名菜品，先将其名称改为 "原名_已被删除"
     * @param dto 菜品信息 + 口味列表
     */
    @Override
    public void updateWithFlavors(DishDto dto) {
        // 查询已逻辑删除的同名菜品（排除自身），改名以释放唯一约束
        List<Dish> deletedList = baseMapper.selectDeletedByName(dto.getName());
        for (Dish deleted : deletedList) {
            if (deleted.getId().equals(dto.getId())) {
                continue;
            }
            baseMapper.renameDeletedById(deleted.getId(), deleted.getName() + "_已被删除");
            log.info("同名已删除菜品已改名 - ID={}, 原名称={}", deleted.getId(), dto.getName());
        }

        // 1. 更新菜品基本信息
        this.updateById(dto);
        Long dishId = dto.getId();
        log.info("更新菜品 - ID={}, name={}", dishId, dto.getName());

        // 2. 删除原有口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishId);
        dishFlavorService.remove(queryWrapper);

        // 3. 重新插入新口味
        if (dto.getFlavors() != null && !dto.getFlavors().isEmpty()) {
            for (DishFlavor flavor : dto.getFlavors()) {
                flavor.setDishId(dishId);
                flavor.setId(null); // 新插入，清除旧 ID
            }
            dishFlavorService.saveBatch(dto.getFlavors());
            log.info("更新菜品口味 - 数量={}", dto.getFlavors().size());
        }
    }

    /**
     * 删除菜品（含关联口味）
     * @param ids 菜品 ID 列表
     */
    @Override
    public void deleteWithFlavors(List<Long> ids) {
        log.info("删除菜品（含口味） - ids={}", ids);

        // 1. 删除关联口味
        LambdaQueryWrapper<DishFlavor> flavorQuery = new LambdaQueryWrapper<>();
        flavorQuery.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(flavorQuery);

        // 2. 删除菜品（逻辑删除，@TableLogic 自动处理）
        this.removeByIds(ids);
    }
}
