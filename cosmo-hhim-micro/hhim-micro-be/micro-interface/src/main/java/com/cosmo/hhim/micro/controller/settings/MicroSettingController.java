/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.settings;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenantIndividuationConfig;
import com.cosmo.hhim.micro.base.domain.entity.common.OfficialUseParam;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroSettingService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTenantIndividuationConfigService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-02
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/setting")
public class MicroSettingController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    private final IMicroSettingService microSettingService;
    private final IMicroTenantIndividuationConfigService microTenantIndividuationConfigService;

    /**
     * 开启正式使用
     *
     * @return
     */
    @Log(title = "系统设置", businessType = BusinessType.UPDATE)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE})
    @PostMapping("/officialUse")
    public AjaxResult officialUse(@RequestBody OfficialUseParam param) {
        log.info("正式使用--------->param:{}", JSON.toJSONString(param));
        Boolean result = microSettingService.officialUse(param.isClearData());
        log.info("正式使用--------->result:{}", result);
        return AjaxResult.success(result);
    }

    /**
     * 获取是否正式使用的标识
     *
     * @return
     */
    @GetMapping("/getOfficialUseFlag")
    public AjaxResult getOfficialUseFlag() {
        return AjaxResult.success(microSettingService.getOfficialUseFlag());
    }

    /**
     * 获取企业个性化设置配置详细信息(按租户查询)
     */
    @GetMapping("/getIndividuationConfig")
    public AjaxResult getSettingInfo() {
        return AjaxResult.success(microTenantIndividuationConfigService.selectMicroTenantIndividuationConfigByTenant());
    }

    /**
     * 更新企业个性化配置
     * @return
     */
    @Log(title = "系统个性化设置", businessType = BusinessType.UPDATE)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE})
    @PutMapping("/updateIndividuationConfig")
    public AjaxResult updateSettingInfo(@RequestBody MicroTenantIndividuationConfig param){
        microTenantIndividuationConfigService.updateMicroTenantIndividuationConfig(param);
        return AjaxResult.success();
    }

    /**
     * 获取当前租户所购买的所有应用信息
     * @return
     */
    @GetMapping("/getAllAppInfos")
    public AjaxResult getAllAppInfoForCurrentTenant(){
        return AjaxResult.success(microSettingService.getAllAppInfosForCurrentTenant());
    }

}
