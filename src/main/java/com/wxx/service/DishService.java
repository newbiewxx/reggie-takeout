package com.wxx.service; // 服务接口包

import com.baomidou.mybatisplus.extension.service.IService; // MyBatis-Plus 基础 Service 接口
import com.wxx.domain.Dish; // 菜品实体类
import com.wxx.dto.DishDto; // 菜品 DTO
import org.springframework.transaction.annotation.Transactional;
import java.util.List; // 集合接口

public interface DishService extends IService<Dish> { // 继承 IService，自动提供 CRUD 方法

    /**
     * 新增菜品（含口味信息）
     * @param dto 菜品信息 + 口味列表
     */
    @Transactional
    void saveWithFlavors(DishDto dto);

    /**
     * 修改菜品（含口味信息）
     * @param dto 菜品信息 + 口味列表
     */
    @Transactional
    void updateWithFlavors(DishDto dto);

    /**
     * 删除菜品（含关联口味）
     * @param ids 菜品 ID 列表
     */
    @Transactional
    void deleteWithFlavors(List<Long> ids);
}
