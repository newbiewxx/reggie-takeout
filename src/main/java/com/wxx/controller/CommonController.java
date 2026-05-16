package com.wxx.controller; // 控制器包

import com.wxx.common.R; // 统一响应结果封装类
import lombok.extern.slf4j.Slf4j; // Lombok：日志对象
import org.springframework.beans.factory.annotation.Value; // 读取配置文件
import org.springframework.util.FileCopyUtils; // Spring 文件拷贝工具类
import org.springframework.web.bind.annotation.*; // MVC 请求映射注解
import org.springframework.web.multipart.MultipartFile; // 文件上传对象

import javax.annotation.PostConstruct; // 初始化方法注解
import javax.servlet.ServletOutputStream; // 响应输出流
import javax.servlet.http.HttpServletResponse; // HTTP 响应对象
import java.io.File; // 文件操作
import java.io.FileInputStream; // 文件输入流
import java.io.IOException; // IO 异常
import java.util.UUID; // 生成唯一文件名

@RestController // 组合注解 = @Controller + @ResponseBody，返回 JSON
@Slf4j // 自动生成 log 日志对象
@RequestMapping("common") // 请求映射前缀：/common
public class CommonController {

    @Value("${reggie.upload-path}") // 注入配置文件中的上传路径
    private String uploadPath;

    /**
     * 初始化方法：将相对路径转为绝对路径
     * 解决 Mac/Linux 下嵌入式 Tomcat 工作目录不一致导致的路径问题
     */
    @PostConstruct // 依赖注入完成后自动执行
    public void init() {
        // 如果配置的是相对路径，则基于项目根目录拼接为绝对路径
        File dir = new File(uploadPath);
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), uploadPath);
        }
        uploadPath = dir.getAbsolutePath() + File.separator;
        log.info("文件上传目录 - {}", uploadPath);
    }

    /**
     * 文件上传
     * @param file 上传的文件（表单字段名固定为 file）
     * @return 统一响应结果，data 为存储的文件名（用于后续下载）
     */
    @PostMapping("/upload") // POST /common/upload
    public R<String> upload(MultipartFile file) {
        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        // 提取后缀（如 .jpg、.png）
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成唯一文件名，防止覆盖
        String filename = UUID.randomUUID() + suffix;
        log.info("文件上传 - 原始名={}, 存储名={}", originalFilename, filename);

        // 确保上传目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            // 将临时文件转存到目标目录
            file.transferTo(new File(uploadPath + filename));
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.error("文件上传失败");
        }

        return R.success(filename);
    }

    /**
     * 文件下载（图片预览）
     * @param name     文件名（从上传接口返回）
     * @param response HTTP 响应，用于返回文件流
     */
    @GetMapping("/download") // GET /common/download?name=xxx.jpg
    public void download(String name, HttpServletResponse response) {
        log.info("文件下载 - name={}", name);

        // 参数校验
        if (name == null || name.isEmpty()) {
            log.warn("文件下载失败 - 文件名为空");
            return;
        }

        // 使用 try-with-resources 自动关闭流
        try (
            FileInputStream fis = new FileInputStream(uploadPath + name);
            ServletOutputStream os = response.getOutputStream()
        ) {
            // 设置响应类型为图片（前端 img 标签可直接预览）
            response.setContentType("image/jpeg");
            // Spring 工具类：将文件输入流拷贝到响应输出流
            FileCopyUtils.copy(fis, os);
        } catch (IOException e) {
            log.error("文件下载失败 - name={}", name, e);
        }
    }
}