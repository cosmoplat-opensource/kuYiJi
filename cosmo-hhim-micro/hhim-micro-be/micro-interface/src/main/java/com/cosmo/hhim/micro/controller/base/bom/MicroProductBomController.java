/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.bom;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.micro.application.dto.base.MicroProductBomDto;
import com.cosmo.hhim.micro.application.service.base.IMicroProductBomFacadeService;
import com.cosmo.hhim.micro.base.domain.service.bom.IMicroProductBomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 产品BOMController
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@RestController
@RequestMapping("/bom")
public class MicroProductBomController extends BaseController
{

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroProductBomFacadeService microProductBomFacadeService;
    @Autowired
    private IMicroProductBomService microProductBomService;

    /**
     * 查询产品BOM列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroProductBomDto microProductBomDto)
    {
        startPage();
        List<MicroProductBomDto> list = microProductBomFacadeService.selectMicroProductBomList(microProductBomDto);
        return getDataTable(list);
    }

    /**
     * 获取产品BOM详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id")  Long id)
    {
        return AjaxResult.success(microProductBomFacadeService.selectMicroProductBomById(id));
    }

    /**
     * 新增产品BOM
     */
    @PostMapping("/save")
    public AjaxResult save(@RequestBody MicroProductBomDto microProductBomDto) {
        microProductBomFacadeService.insertMicroProductBom(microProductBomDto);
        return AjaxResult.success();
    }

    /**
     * 修改产品BOM
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody MicroProductBomDto microProductBomDto)
    {
        microProductBomFacadeService.updateMicroProductBom(microProductBomDto);
        return AjaxResult.success();
    }

    @GetMapping("/isOrNotBelongToParenBom")
    public AjaxResult isOrNotBelongToParenBom(@RequestParam String currentProductSeq,
                                              @RequestParam List<String> addBomProductSeqList) {
        return AjaxResult.success(microProductBomService.isOrNotBelongToParenBom(currentProductSeq, addBomProductSeqList));
    }

}
