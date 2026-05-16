package com.wxx.domain; // 实体类所在包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略注解（INSERT / INSERT_UPDATE / UPDATE）
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 表字段注解，用于指定字段填充策略
import com.fasterxml.jackson.databind.annotation.JsonSerialize; // Jackson JSON 序列化注解
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer; // Jackson Long 转字符串序列化器
import lombok.Data; // Lombok 注解，编译时自动生成 getter / setter / equals / hashCode / toString
import java.io.Serializable; // 序列化接口，支持对象在网络 / 缓存中传输
import java.time.LocalDateTime; // Java 8 时间API，替代 Date

@Data // Lombok 注解：自动生成 getter、setter、equals、hashCode、toString
public class Employee implements Serializable { // 实现 Serializable 以便在会话或缓存中传输

    private static final long serialVersionUID = 1L; // 序列化版本号，保证反序列化时版本一致

    // @JsonSerialize(using = ToStringSerializer.class) // Long 转 String 序列化，避免前端 JS 丢失精度
    private Long id; // 主键 ID（雪花算法生成）
    private String username; // 登录账号
    private String name; // 员工姓名
    private String password; // 登录密码
    private String phone; // 手机号
    private String sex; // 性别
    private String idNumber; // 身份证号
    private Integer status; // 状态：0=禁用，1=启用

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充创建时间（由 MetaObjectHandler 处理）
    private LocalDateTime createTime; // 记录创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充修改时间
    private LocalDateTime updateTime; // 记录最后修改时间

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充（由 MetaObjectHandler 处理）
    private Long createUser; // 创建人 ID

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private Long updateUser; // 最后修改人 ID
}