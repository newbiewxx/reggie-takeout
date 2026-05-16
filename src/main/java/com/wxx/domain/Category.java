package com.wxx.domain; // 实体类所在包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略注解
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 表字段注解
import com.baomidou.mybatisplus.annotation.TableLogic; // MyBatis-Plus 逻辑删除注解
import lombok.Data; // Lombok：自动生成 getter/setter/toString 等
import java.io.Serializable; // 序列化接口
import java.time.LocalDateTime; // Java 8 时间类型

@Data // Lombok 注解：自动生成 getter、setter、equals、hashCode、toString
public class Category implements Serializable { // 分类实体，实现序列化

    private static final long serialVersionUID = 1L; // 序列化版本号

    private Long id; // 主键 ID（雪花算法生成）

    /** 类型：1=菜品分类，2=套餐分类 */
    private Integer type;

    /** 分类名称（如"川菜"、"饮品"） */
    private String name;

    /** 排序序号，值越小越靠前 */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充（由 MetaObjectHandler 处理）
    private LocalDateTime createTime; // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime; // 修改时间

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private Long createUser; // 创建人 ID

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private Long updateUser; // 修改人 ID
}