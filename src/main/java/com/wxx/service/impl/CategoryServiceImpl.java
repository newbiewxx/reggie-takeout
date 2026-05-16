package com.wxx.service.impl; // Service 实现包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus 条件构造器
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl; // MyBatis-Plus 基础实现类
import com.wxx.exception.BusinessException; // 自定义业务异常
import com.wxx.domain.Category; // 分类实体类
import com.wxx.domain.Dish; // 菜品实体类
import com.wxx.domain.Setmeal; // 套餐实体类
import com.wxx.mapper.CategoryMapper; // 分类 Mapper
import com.wxx.service.CategoryService; // 分类 Service 接口
import com.wxx.service.DishService; // 菜品 Service 接口
import com.wxx.service.SetmealService; // 套餐 Service 接口
import lombok.RequiredArgsConstructor; // Lombok：生成带 final 字段的构造器注入
import lombok.extern.slf4j.Slf4j; // Lombok：日志对象
import org.springframework.stereotype.Service; // Spring 服务注解

@Service // 声明为 Spring Bean，自动扫描注入
@RequiredArgsConstructor // 为 final 字段生成构造器（Spring 自动注入）
@Slf4j // 自动生成 log 日志对象
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    // 关联的服务（构造器注入）
    private final DishService dishService;
    private final SetmealService setmealService;

    /**
     * 删除分类：删除前校验是否被菜品或套餐引用
     * @param id 分类 ID
     */
    @Override
    public void removeWithCheck(Long id) {
        // 检查分类下是否关联了菜品
        LambdaQueryWrapper<Dish> dishQuery = new LambdaQueryWrapper<>();
        dishQuery.eq(Dish::getCategoryId, id);
        if (dishService.count(dishQuery) > 0) {
            throw new BusinessException("删除失败，该分类下已关联菜品");
        }

        // 检查分类下是否关联了套餐
        LambdaQueryWrapper<Setmeal> setmealQuery = new LambdaQueryWrapper<>();
        setmealQuery.eq(Setmeal::getCategoryId, id);
        if (setmealService.count(setmealQuery) > 0) {
            throw new BusinessException("删除失败，该分类下已关联套餐");
        }

        // 校验通过，执行删除
        this.removeById(id);
        log.info("删除分类成功 - ID={}", id);
    }
}
