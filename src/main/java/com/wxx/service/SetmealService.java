package com.wxx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wxx.domain.Setmeal;
import com.wxx.dto.SetmealDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐（含套餐内菜品）
     */
    @Transactional
    void saveWithDishes(SetmealDto dto);

    /**
     * 修改套餐（含套餐内菜品）
     */
    @Transactional
    void updateWithDishes(SetmealDto dto);

    /**
     * 删除套餐（含关联菜品）
     */
    @Transactional
    void deleteWithDishes(List<Long> ids);
}
