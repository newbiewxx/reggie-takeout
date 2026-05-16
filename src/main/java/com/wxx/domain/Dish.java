package com.wxx.domain; // 实体类包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 表字段注解
import com.baomidou.mybatisplus.annotation.TableLogic; // MyBatis-Plus 逻辑删除注解
import lombok.Data; // Lombok：自动生成 getter/setter/toString 等
import java.io.Serializable; // 序列化接口
import java.math.BigDecimal; // 金额高精度类型
import java.time.LocalDateTime; // Java 8 时间类型

@Data // Lombok 注解：自动生成 getter、setter、equals、hashCode、toString
public class Dish implements Serializable { // 菜品实体，实现序列化

    private static final long serialVersionUID = 1L; // 序列化版本号

    private Long id; // 主键 ID（雪花算法生成）

    /** 菜品名称 */
    private String name;

    /** 菜品分类 ID（关联 Category） */
    private Long categoryId;

    /** 菜品价格（单位：分，前端展示时除以 100） */
    private BigDecimal price;

    /** 菜品码 */
    private String code;

    /** 图片路径 */
    private String image;

    /** 菜品描述 */
    private String description;

    /** 售卖状态：0=停售，1=起售 */
    private Integer status;

    /** 排序序号，值越小越靠前 */
    private Integer sort;

    /** 逻辑删除标志：0=未删除，1=已删除 */
    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime; // 修改时间

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private Long createUser; // 创建人 ID

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private Long updateUser; // 修改人 ID
}
