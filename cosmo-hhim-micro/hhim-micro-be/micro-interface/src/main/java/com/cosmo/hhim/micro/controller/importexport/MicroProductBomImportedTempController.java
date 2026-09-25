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
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBomImportedTemp;
import com.cosmo.hhim.micro.base.domain.service.bom.IMicroProductBomImportedTempService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * BOM导入临时Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-10
 */
@RestController
@RequestMapping("/bom/temp")
public class MicroProductBomImportedTempController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroProductBomImportedTempService microProductBomImportedTempService;

    /**
     * 查询BOM导入临时列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroProductBomImportedTemp microProductBomImportedTemp) {
        startPage();
        List<MicroProductBomImportedTemp> list = microProductBomImportedTempService.selectMicroProductBomImportedTempList(microProductBomImportedTemp);
        return getDataTable(list);
    }

    /**
     * 修改BOM导入临时
     */
    @Log(title = "BOM导入临时数据" , businessType = BusinessType.UPDATE)
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody MicroProductBomImportedTemp microProductBomImportedTemp) {
        return toAjax(microProductBomImportedTempService.updateMicroProductBomImportedTemp(microProductBomImportedTemp));
    }

    /**
     * 删除BOM导入临时
     */
    @Log(title = "BOM导入临时数据" , businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(microProductBomImportedTempService.deleteMicroProductBomImportedTempByIds(ids));
    }
}
