package com.wxx.service.impl; // Service 实现包

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl; // MyBatis-Plus 基础实现类
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
     * @param dto 菜品信息 + 口味列表
     */
    @Override
    @Transactional // 保证菜品和口味同时保存或回滚
    public void saveWithFlavors(DishDto dto) {
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
}
