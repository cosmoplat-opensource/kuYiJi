/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.importexport.common;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.application.service.base.IMicroImportedService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import com.cosmo.hhim.minio.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/6
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
public class MicroImportedController {

    private final IMicroImportedService microImportedService;
    private final ISysFileService sysFileService;


    /**
     * 下载导入模版
     */
    @Log(title = "导入模块", businessType = BusinessType.EXPORT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/importTemplate")
    public void getImportTemplate(HttpServletResponse response, @RequestParam("certificate") String certificate) {
        microImportedService.getImportTemplate(response, certificate);
    }

    /**
     * 获取文件预上传地址
     *
     * @param fileName
     * @return
     */
    @GetMapping("/getPreSignedPutUrl")
    public AjaxResult getPreSignedPutUrl(
            @RequestParam @Pattern(regexp = "^[a-zA-Z0-9._/@-]{1,255}$", message = "文件名包含非法字符") String fileName) {
        // 文件名校验（JSR303 @Pattern 参数校验）：仅允许常规文件名字符，防止路径注入与反射型 XSS
        return AjaxResult.success(sysFileService.getPreSignedFilePutUrl(fileName));
    }

    /**
     * 确认导入
     * flag(0:错误数据修复，1:确认导入)
     *
     * @return
     */
    @Log(title = "导入模块-确认", businessType = BusinessType.INSERT)
    @GetMapping("/confirm")
    public AjaxResult importConfirm(@RequestParam("flag") String flag) {
        microImportedService.importConfirm(flag);
        return AjaxResult.success();
    }

    /**
     * 取消导入
     *
     * @return
     */
    @Log(title = "导入模块-取消", businessType = BusinessType.DELETE)
    @DeleteMapping("/cancel")
    public AjaxResult importCancel() {
        microImportedService.importCancel();
        return AjaxResult.success();
    }


}
