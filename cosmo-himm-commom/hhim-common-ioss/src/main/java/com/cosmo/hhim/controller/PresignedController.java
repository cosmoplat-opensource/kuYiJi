/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.controller;

import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.domin.SysFilePreSign;
import com.cosmo.hhim.minio.ISysFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RestController
public class PresignedController {

    @Autowired
    private ISysFileService iSysFileService;

    /**
     * 校验文件路径参数：仅允许常规文件名字符（字母数字 . _ - / @），
     * 禁止 HTML 特殊字符与空白，防止路径/URL 注入（反射型 XSS 与资源注入防护）
     */
    private static void checkFileName(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            throw new CustomException("文件名不允许为空");
        }
        if (fileName.length() > 255 || !fileName.matches("^[a-zA-Z0-9._/@-]+$")) {
            throw new CustomException("文件名包含非法字符");
        }
    }

    /**
     * 获取上传文件预签名
     *
     * @param
     * @return
     * @throws Exception
     */
    @GetMapping("/getPresignedPutUrl")
    public AjaxResult getPresignedPutUrl(
            @RequestParam @Pattern(regexp = "^[a-zA-Z0-9._/@-]{1,255}$", message = "文件名包含非法字符") String fileName,
            @RequestParam(required = false) @Pattern(regexp = "^[a-zA-Z0-9._/@-]{1,255}$", message = "目录名包含非法字符") String folder) throws Exception {
        checkFileName(fileName);
        if (StringUtils.isNotEmpty(folder)) {
            checkFileName(folder);
        }
        return AjaxResult.success(StringUtils.isEmpty(folder) ? iSysFileService.getPresignedPutUrl(fileName) : iSysFileService.getPresignedPutUrl(fileName, folder));
    }

    /**
     * 获取下载文件预签名
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    @GetMapping("/getPresignedGetUrl")
    public AjaxResult getPresignedGetUrl(
            @RequestParam @Pattern(regexp = "^[a-zA-Z0-9._/@-]{1,255}$", message = "文件名包含非法字符") String fileName) throws Exception {
        checkFileName(fileName);
        String getUrl = iSysFileService.getPresignedGetUrl(fileName);
        return AjaxResult.success(getUrl);
    }

    /**
     * 批量获取下载地址
     * @param sysFilePreSigns
     * @return
     * @throws Exception
     */
    @PostMapping("/getPreSignedGetUrlBatch")
    public AjaxResult getPreSignedGetUrl(@RequestBody @Valid List<SysFilePreSign> sysFilePreSigns) throws Exception {

        if(CheckObjectUtils.isNotEmpty(sysFilePreSigns)){
            sysFilePreSigns.forEach(sysFilePreSign -> {
                try {
                    checkFileName(sysFilePreSign.getFileName());
                    sysFilePreSign.setPath(iSysFileService.getPresignedGetUrl(sysFilePreSign.getFileName()));
                } catch (Exception e) {
                    log.error("批量获取下载地址失败: {}", sysFilePreSign.getFileName(), e);
                }
            });
        }

        return AjaxResult.success(sysFilePreSigns);
    }

    /**
     * 批量获取下载地址（无过期失效）
     * @param sysFilePreSigns
     * @return
     * @throws Exception
     */
    @PostMapping("/getPreSignedGetUrlBatchNoExpiry")
    public AjaxResult getPreSignedGetUrlNoExpiry(@RequestBody @Valid List<SysFilePreSign> sysFilePreSigns) throws Exception {

        if(CheckObjectUtils.isNotEmpty(sysFilePreSigns)){
            sysFilePreSigns.forEach(sysFilePreSign -> {
                try {
                    checkFileName(sysFilePreSign.getFileName());
                    sysFilePreSign.setPath(iSysFileService.getPresignedGetUrlNoExpiry(sysFilePreSign.getFileName()));
                } catch (Exception e) {
                    log.error("批量获取下载地址失败(无过期): {}", sysFilePreSign.getFileName(), e);
                }
            });
        }

        return AjaxResult.success(sysFilePreSigns);
    }
}
