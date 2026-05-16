package com.wxx.dto;

import com.wxx.domain.Setmeal;
import com.wxx.domain.SetmealDish;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SetmealDto extends Setmeal {

    /** 分类名称（页面展示用） */
    private String categoryName;

    /** 套餐内菜品列表 */
    private List<SetmealDish> setmealDishes;
}
