/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.FileUploadUtils;
import com.cosmo.hhim.common.core.utils.file.MimeTypeUtils;
import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.minio.ISysFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * upload
 */
@RestController
@RequestMapping("/upload")
@Slf4j
public class MicroUploadController extends BaseController {
    /** 上传文件大小上限：20MB */
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024L;
    /** 允许上传的扩展名白名单（与 FileUploadUtils.assertAllowed 一致，显式声明供静态校验识别） */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);

    @Autowired
    private ISysFileService fileService;

    /**
     * 上传文件扩展名白名单与大小校验（显式声明，与 FileUploadUtils.assertAllowed 双重校验）
     */
    private void checkUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.lastIndexOf('.') < 0) {
            throw new CustomException("文件名不合法");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException("文件类型不合法，仅允许：" + String.join(",", ALLOWED_EXTENSIONS));
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException("文件大小不能超过20MB");
        }
    }

    @PostMapping("/picture")
    public AjaxResult testUpload(MultipartFile file) throws Exception {
        // 扩展名白名单 + 大小限制校验，防止上传可执行脚本等危险文件
        checkUploadFile(file);
        FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        return AjaxResult.success(fileService.uploadFile(file, "picture"));
    }

    /**
     * 上传多张照片
     */
    @PostMapping("/multiPicture")
    public AjaxResult uploadMultiPicture(@Param("files") MultipartFile[] files) {
        if (CheckObjectUtils.isEmpty(files)) {
            throw new CustomException("上传图片为空");
        }

        try {
            StringBuilder sb = new StringBuilder();
            for (MultipartFile file : files) {
                // 扩展名白名单 + 大小限制校验，防止上传可执行脚本等危险文件
                checkUploadFile(file);
                FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
                String urlTemp = fileService.uploadFile(file, "picture");
                // 拼接字符串
                sb.append(urlTemp).append(";");
            }
            return AjaxResult.success(sb.deleteCharAt(sb.length() - 1).toString());
        } catch (Exception e) {
            log.error("上传图片异常", e);
            throw new CustomException("上传图片异常", e);
        }
    }
}
