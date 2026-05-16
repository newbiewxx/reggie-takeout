package com.wxx.config; // 配置类所在包

import com.wxx.common.JacksonObjectMapper; // 自定义 Jackson 对象映射器（Long 转 String、日期格式化等）
import lombok.extern.slf4j.Slf4j; // Lombok：自动生成 log 日志对象
import org.springframework.context.annotation.Configuration; // Spring 配置类注解
import org.springframework.http.converter.HttpMessageConverter; // HTTP 消息转换器接口
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter; // Jackson 消息转换器
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry; // 静态资源处理器注册器
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // Spring MVC 配置接口

import java.util.List; // 集合接口

@Configuration // 声明为 Spring 配置类，会被自动扫描加载
@Slf4j // Lombok：生成 log 对象，用于打印日志
public class SpringMvcConfig implements WebMvcConfigurer { // 实现 WebMvcConfigurer，自定义 Spring MVC 行为

    /**
     * 配置静态资源映射，使前端页面能正确访问
     */
    @Override // 重写 WebMvcConfigurer 方法
    public void addResourceHandlers(ResourceHandlerRegistry registry) { // 静态资源处理器
        log.info("开始进行静态资源映射..."); // 启动时打印日志

        // 访问 /backend/xxx 时，到 classpath:/backend/ 目录查找文件
        registry.addResourceHandler("/backend/**") // URL 匹配模式
                .addResourceLocations("classpath:/backend/"); // 对应静态资源目录

        // 访问 /front/xxx 时，到 classpath:/front/ 目录查找文件
        registry.addResourceHandler("/front/**") // URL 匹配模式
                .addResourceLocations("classpath:/front/"); // 对应静态资源目录
    }

    /**
     * 扩展 Spring MVC 消息转换器
     * 将自定义的 JacksonObjectMapper 注册到消息转换器列表的第一位，使其优先生效
     */
    @Override // 重写 WebMvcConfigurer 方法
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) { // 消息转换器列表
        log.info("扩展消息转换器..."); // 启动时打印日志

        // 创建基于 Jackson 的 HTTP 消息转换器
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter =
                new MappingJackson2HttpMessageConverter();

        // 设置自定义 ObjectMapper（Long 转字符串、日期格式化等全局配置）
        mappingJackson2HttpMessageConverter.setObjectMapper(new JacksonObjectMapper());

        // 将自定义转换器插入到列表最前面，确保优先级最高
        converters.add(0, mappingJackson2HttpMessageConverter);
    }
}