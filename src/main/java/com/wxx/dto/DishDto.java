package com.wxx.dto; // 数据传输对象包

import com.wxx.domain.Dish; // 菜品实体类
import com.wxx.domain.DishFlavor; // 菜品口味实体类
import lombok.Data; // Lombok：自动生成 getter/setter/toString 等
import lombok.EqualsAndHashCode;

import java.util.List; // 集合接口

/**
 * 菜品 DTO：扩展 Dish，携带页面展示所需的额外字段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DishDto extends Dish { // 继承 Dish，复用所有字段

    /** 分类名称（页面展示用，非数据库字段） */
    private String categoryName;

    /** 菜品口味列表（新增/修改时传输用） */
    private List<DishFlavor> flavors;
}
