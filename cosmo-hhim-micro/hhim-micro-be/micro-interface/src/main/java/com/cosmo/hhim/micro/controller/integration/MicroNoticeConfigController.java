/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.integration;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.micro.application.dto.integration.MicroNoticeConfigEditParam;
import com.cosmo.hhim.micro.application.service.integration.IMicroNoticeConfigFacadeService;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.micro.integration.domain.entity.MicroNoticeConfig;
import com.cosmo.hhim.micro.integration.domain.service.IMicroNoticeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.Valid;
import java.util.List;

/**
 * 通知配置Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-06
 */
@RestController
@RequestMapping("/notice/config")
public class MicroNoticeConfigController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroNoticeConfigService microNoticeConfigService;
    @Autowired
    private IMicroNoticeConfigFacadeService microNoticeConfigFacadeService;

    /**
     * 查询通知配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroNoticeConfig microNoticeConfig) {
        startPage();
        List<MicroNoticeConfig> list = microNoticeConfigService.selectMicroNoticeConfigList(microNoticeConfig);
        return getDataTable(list);
    }

    /**
     * 获取通知配置
     */
    @GetMapping("/detail")
    public AjaxResult getConfigDetail(@RequestParam("businessSign") NotifyEnums.NoticeBusinessSignEnum businessSign) {
        return AjaxResult.success(microNoticeConfigFacadeService.selectMicroNoticeConfigByBusinessSign(businessSign));
    }

    /**
     * 新增通知配置
     */
    @PostMapping
    public AjaxResult add(@RequestBody MicroNoticeConfig microNoticeConfig) {
        return toAjax(microNoticeConfigService.insertMicroNoticeConfig(microNoticeConfig));
    }

    /**
     * 修改通知配置
     */
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody MicroNoticeConfigEditParam param) {
        microNoticeConfigFacadeService.updateMicroNoticeConfig(param);
        return AjaxResult.success();
    }

    /**
     * 删除通知配置
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(microNoticeConfigService.deleteMicroNoticeConfigByIds(ids));
    }
}
