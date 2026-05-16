package com.wxx; // 项目根包

import lombok.extern.slf4j.Slf4j; // Lombok：自动生成 log 日志对象
import org.mybatis.spring.annotation.MapperScan; // MyBatis：指定 Mapper 接口扫描路径
import org.springframework.boot.SpringApplication; // Spring Boot 启动器
import org.springframework.boot.autoconfigure.SpringBootApplication; // Spring Boot 核心注解
import org.springframework.boot.web.servlet.ServletComponentScan; // 扫描 Servlet 组件（Filter、Listener、Servlet）

@SpringBootApplication // Spring Boot 核心注解：组合了 @Configuration + @EnableAutoConfiguration + @ComponentScan
@Slf4j // Lombok：生成 log 对象，用于打印日志
@MapperScan("com.wxx.mapper") // 扫描 Mapper 接口所在包，MyBatis 会自动为其生成代理实现类
@ServletComponentScan // 扫描 Servlet 组件（如过滤器 Filter），使 @WebFilter、@WebListener 等注解生效
public class ReggieTakeoutApplication { // 项目启动类

    public static void main(String[] args) { // 程序入口
        log.info("项目启动成功..."); // 启动成功日志
        SpringApplication.run(ReggieTakeoutApplication.class, args); // 启动 Spring Boot 应用
    }

}
