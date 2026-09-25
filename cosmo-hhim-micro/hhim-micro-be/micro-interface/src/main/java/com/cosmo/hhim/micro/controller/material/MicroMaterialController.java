/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.material;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.application.dto.material.MicroMaterialAnalysisDTO;
import com.cosmo.hhim.micro.application.dto.material.MicroMaterialAnalysisQueryDTO;
import com.cosmo.hhim.micro.application.service.material.IMicroMaterialFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 料易投物料
 *
 * @author cosmo-hhim-open Team
 */
@RestController
@RequestMapping("/material")
public class MicroMaterialController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroMaterialFacadeService materialFacadeService;

    /**
     * 查询产品BOM列表
     */
    @PostMapping("/analysis/list")
    public AjaxResult getAnalysisList(@Validated @RequestBody MicroMaterialAnalysisQueryDTO queryDTO) {
        List<MicroMaterialAnalysisDTO> list = materialFacadeService.selectMicroMaterialAnalysisList(queryDTO);
        return AjaxResult.success(list);
    }

    /**
     * 获取某个缺料物料下所有工单信息
     */
    @PostMapping(value = "/analysis/detail")
    public AjaxResult getAnalysisDetail(@Validated @RequestBody MicroMaterialAnalysisQueryDTO queryDTO) {
        return AjaxResult.success(materialFacadeService.selectAnalysisDetail(queryDTO));
    }

}
