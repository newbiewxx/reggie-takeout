package com.wxx.controller;

import com.wxx.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

// import static org.springframework.util.FileCopyUtils.copy;

@RestController
@Slf4j
@RequestMapping("common")
public class CommonController {

    @Value("${reggie.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        // 提取后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成唯一文件名，防止覆盖
        String filename = UUID.randomUUID() + suffix;
        log.info("文件上传 - 原始名={}, 存储名={}", originalFilename, filename);

        // 确保目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(uploadPath + filename));
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.error("文件上传失败");
        }

        return R.success(filename);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        log.info("文件下载 - name={}", name);

        if (name == null || name.isEmpty()) {
            log.warn("文件下载失败 - 文件名为空");
            return;
        }

        try (
            FileInputStream fis = new FileInputStream(uploadPath + name);
            ServletOutputStream os = response.getOutputStream()
        ) {
            response.setContentType("image/jpeg");
            // copy(fis, os);
            FileCopyUtils.copy(fis, os);
        } catch (IOException e) {
            log.error("文件下载失败 - name={}", name, e);
        }
    }
}