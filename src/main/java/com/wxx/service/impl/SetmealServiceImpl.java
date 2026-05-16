package com.wxx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxx.domain.Setmeal;
import com.wxx.domain.SetmealDish;
import com.wxx.dto.SetmealDto;
import com.wxx.mapper.SetmealMapper;
import com.wxx.service.SetmealDishService;
import com.wxx.service.SetmealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    private final SetmealDishService setmealDishService;

    @Override
    public void saveWithDishes(SetmealDto dto) {
        this.save(dto);
        Long setmealId = dto.getId();
        log.info("新增套餐 - ID={}, name={}", setmealId, dto.getName());

        List<SetmealDish> setmealDishes = dto.getSetmealDishes();

        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            for (SetmealDish dish : setmealDishes) {
                dish.setSetmealId(setmealId);
            }
            setmealDishService.saveBatch(dto.getSetmealDishes());
            log.info("新增套餐菜品 - 数量={}", dto.getSetmealDishes().size());
        }
    }

    @Override
    public void updateWithDishes(SetmealDto dto) {
        this.updateById(dto);
        Long setmealId = dto.getId();
        log.info("更新套餐 - ID={}, name={}", setmealId, dto.getName());

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealId);
        setmealDishService.remove(queryWrapper);

        if (dto.getSetmealDishes() != null && !dto.getSetmealDishes().isEmpty()) {
            for (SetmealDish dish : dto.getSetmealDishes()) {
                dish.setSetmealId(setmealId);
                dish.setId(null);
            }
            setmealDishService.saveBatch(dto.getSetmealDishes());
            log.info("更新套餐菜品 - 数量={}", dto.getSetmealDishes().size());
        }
    }

    @Override
    public void deleteWithDishes(List<Long> ids) {
        log.info("删除套餐（含关联菜品） - ids={}", ids);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper);

        this.removeByIds(ids);
    }
}
