package com.wxx.common; // 公共工具包

import com.fasterxml.jackson.databind.DeserializationFeature; // 反序列化特性配置
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson 核心：对象与 JSON 互转
import com.fasterxml.jackson.databind.module.SimpleModule; // Jackson 自定义序列化/反序列化模块
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer; // 序列化器：将 Long/BigInteger 转为字符串
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer; // 反序列化器：String → LocalDate
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer; // 反序列化器：String → LocalDateTime
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer; // 反序列化器：String → LocalTime
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer; // 序列化器：LocalDate → String
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer; // 序列化器：LocalDateTime → String
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer; // 序列化器：LocalTime → String
import java.math.BigInteger; // 大整数类型
import java.time.LocalDate; // Java 8 日期
import java.time.LocalDateTime; // Java 8 日期时间
import java.time.LocalTime; // Java 8 时间
import java.time.format.DateTimeFormatter; // 日期时间格式化器
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES; // 静态导入：遇到未知属性是否抛异常

/**
 * 对象映射器：基于 Jackson 将 Java 对象转为 JSON（序列化），或将 JSON 转为 Java 对象（反序列化）
 * 继承 ObjectMapper，统一配置日期格式、Long 转 String 等全局序列化规则，
 * 避免在每个实体类上重复加 @JsonSerialize 注解
 */
public class JacksonObjectMapper extends ObjectMapper { // 继承 ObjectMapper，扩展全局配置

    // 日期时间格式常量
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd"; // 日期格式
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"; // 日期时间格式
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss"; // 时间格式

    public JacksonObjectMapper() { // 构造函数，执行自定义配置
        super(); // 调用父类 ObjectMapper 构造

        // 反序列化时，遇到 JSON 中未知的属性不抛异常，直接忽略
        this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 与上一行作用相同（另一种写法），确保属性不存在的兼容处理
        this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 注册自定义序列化器与反序列化器
        SimpleModule simpleModule = new SimpleModule()
                // 反序列化：String → LocalDateTime（如 "2026-05-14 14:30:00"）
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                // 反序列化：String → LocalDate（如 "2026-05-14"）
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                // 反序列化：String → LocalTime（如 "14:30:00"）
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))

                // 序列化：BigInteger → String（防止前端 JS 丢失精度）
                .addSerializer(BigInteger.class, ToStringSerializer.instance)
                // 序列化：Long → String（雪花算法生成的 ID 超出 JS 安全整数范围）
                .addSerializer(Long.class, ToStringSerializer.instance)
                // 序列化：LocalDateTime → String（如 "2026-05-14 14:30:00"）
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                // 序列化：LocalDate → String（如 "2026-05-14"）
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                // 序列化：LocalTime → String（如 "14:30:00"）
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        // 将自定义模块注册到 ObjectMapper，全局生效
        this.registerModule(simpleModule);
    }
}