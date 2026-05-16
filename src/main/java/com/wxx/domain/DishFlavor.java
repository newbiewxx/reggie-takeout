package com.wxx.domain; // 实体类包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 表字段注解
import lombok.Data; // Lombok：自动生成 getter/setter/toString 等
import java.io.Serializable; // 序列化接口
import java.time.LocalDateTime; // Java 8 时间类型

@Data // Lombok 注解：自动生成 getter、setter、equals、hashCode、toString
public class DishFlavor implements Serializable { // 菜品口味实体

    private static final long serialVersionUID = 1L; // 序列化版本号

    private Long id; // 主键 ID（雪花算法生成）

    /** 所属菜品 ID（关联 Dish） */
    private Long dishId;

    /** 口味名称（如"辣度"、"甜度"） */
    private String name;

    /** 口味标签列表（JSON 字符串，如 ["微辣","中辣","特辣"]） */
    private String value;

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime; // 修改时间

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private Long createUser; // 创建人 ID

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private Long updateUser; // 修改人 ID
}
