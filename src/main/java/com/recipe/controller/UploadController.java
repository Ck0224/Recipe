package com.recipe.controller;

import com.recipe.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload") // 适配你的双/api前缀
@RequiredArgsConstructor
public class UploadController {
    // 配置文件上传路径（绝对路径）
    @Value("${file.upload.path}")
    private String uploadPath;

    // 配置图片访问前缀
    @Value("${file.access.prefix:/images/}")
    private String accessPrefix;

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        // 1. 参数校验
        if (file.isEmpty()) {
            return Result.error("上传失败：文件为空");
        }

        // 2. 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches("^.+\\.(jpg|jpeg|png|gif)$")) {
            return Result.error("上传失败：仅支持jpg/jpeg/png/gif格式");
        }

        // 3. 关键修复：强制创建多级目录（mkdirs()会创建所有不存在的父目录）
        File uploadDir = new File(uploadPath);
        // 打印路径调试（方便排查）
        System.out.println("文件上传目录：" + uploadDir.getAbsolutePath());
        // 强制创建目录（包括所有父目录）
        if (!uploadDir.exists()) {
            boolean mkdirSuccess = uploadDir.mkdirs(); // 用mkdirs()而非mkdir()！！！
            System.out.println("目录创建结果：" + (mkdirSuccess ? "成功" : "失败"));
            // 强制设置权限（Windows/Linux通用）
            uploadDir.setWritable(true, false);
            uploadDir.setReadable(true, false);
            uploadDir.setExecutable(true, false);
        }

        // 4. 生成唯一文件名
        String fileName = UUID.randomUUID().toString() +
                originalFilename.substring(originalFilename.lastIndexOf("."));
        // 拼接绝对路径的文件对象
        File destFile = new File(uploadDir, fileName);
        System.out.println("文件保存路径：" + destFile.getAbsolutePath());

        // 5. 保存文件
        try {
            // 关键：transferTo支持绝对路径，避免Tomcat临时目录问题
            file.transferTo(destFile);
            // 6. 返回图片访问URL（适配双/api前缀）
            String imageUrl = "/api" + accessPrefix + fileName;
            return Result.success(imageUrl);
        } catch (IOException e) {
            // 打印详细异常（方便排查）
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }


}
