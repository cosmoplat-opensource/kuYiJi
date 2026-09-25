/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.importexport;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.micro.application.service.storage.IMicroFinishedStorageImportedFacadeService;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedStorageImportedTemp;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedStorageImportedTempService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/4
 */
@Slf4j
@RestController
@RequestMapping("/storage/finish/temp")
@RequiredArgsConstructor
public class MicroFinishedStorageImportedTempController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    private final IMicroFinishedStorageImportedTempService microFinishedStorageImportedTempService;
    private final IMicroFinishedStorageImportedFacadeService microFinishedStorageImportedFacadeService;

    /**
     * 查询期初产成品库存导入临时列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroFinishedStorageImportedTemp queryParam) {
        startPage();
        List<MicroFinishedStorageImportedTemp> list = microFinishedStorageImportedFacadeService.selectMicroFinishedStorageImportedTempList(queryParam);
        return getDataTable(list);
    }

    /**
     * 修改期初产成品库存导入临时
     */
    @Log(title = "期初产成品库存导入临时模块", businessType = BusinessType.UPDATE)
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody MicroFinishedStorageImportedTemp updateParam) {
        return toAjax(microFinishedStorageImportedTempService.updateMicroFinishedStorageImportedTemp(updateParam));
    }

    /**
     * 删除期初产成品库存导入临时
     */
    @Log(title = "期初产成品库存导入临时模块", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(microFinishedStorageImportedTempService.deleteMicroFinishedStorageImportedTempByIds(ids));
    }

}
