package com.wxx.domain; // 实体类包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 表字段注解
import com.baomidou.mybatisplus.annotation.TableLogic; // MyBatis-Plus 逻辑删除注解
import lombok.Data; // Lombok：自动生成 getter/setter/toString 等
import java.io.Serializable; // 序列化接口
import java.math.BigDecimal; // 金额高精度类型
import java.time.LocalDateTime; // Java 8 时间类型

@Data // Lombok 注解：自动生成 getter、setter、equals、hashCode、toString
public class SetmealDish implements Serializable { // 套餐-菜品关联实体

    private static final long serialVersionUID = 1L; // 序列化版本号

    private Long id; // 主键 ID（雪花算法生成）

    /** 所属套餐 ID（关联 Setmeal） */
    private Long setmealId;

    /** 菜品 ID（关联 Dish） */
    private Long dishId;

    /** 菜品名称（冗余字段，方便展示） */
    private String name;

    /** 菜品单价（冗余字段，单位：分） */
    private BigDecimal price;

    /** 份数 */
    private Integer copies;

    /** 排序序号，值越小越靠前 */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime; // 修改时间

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private Long createUser; // 创建人 ID

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private Long updateUser; // 修改人 ID

    @TableLogic // 逻辑删除注解
    private Integer isDeleted; // 逻辑删除标志：0=未删除，1=已删除
}
