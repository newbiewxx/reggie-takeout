package com.wxx.common; // 公共工具包

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler; // MyBatis-Plus 元对象处理器接口
import lombok.extern.slf4j.Slf4j; // Lombok日志
import org.apache.ibatis.reflection.MetaObject; // MyBatis 反射工具，用于操作实体类字段
import org.springframework.stereotype.Component; // Spring 组件注解

import java.time.LocalDateTime; // Java 8 时间 API

@Slf4j // 自动生成 log 对象
@Component // 声明为 Spring Bean，自动被扫描
public class MyMetaObjectHandler implements MetaObjectHandler { // 实现 MyBatis-Plus 自动填充处理器

    /**
     * 插入时自动填充（INSERT 策略的字段）
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("自动填充 - 插入操作");

        // 填充创建时间（字段不存在或已有值则跳过）
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());

        // 填充修改时间（和创建时间一致）
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 填充创建人 ID（从 BaseContextPlus 获取当前登录员工，由 Filter 设置到 ThreadLocal）
        this.strictInsertFill(metaObject, "createUser", Long.class, BaseContextPlus.getCurrentId());

        // 填充修改人 ID
        this.strictInsertFill(metaObject, "updateUser", Long.class, BaseContextPlus.getCurrentId());
    }

    /**
     * 更新时自动填充（INSERT_UPDATE 策略的字段）
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("自动填充 - 更新操作");

        // 更新修改时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 更新修改人 ID
        this.strictUpdateFill(metaObject, "updateUser", Long.class, BaseContextPlus.getCurrentId());
    }
}
