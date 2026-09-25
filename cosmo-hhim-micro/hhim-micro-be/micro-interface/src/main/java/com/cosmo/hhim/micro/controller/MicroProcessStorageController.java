/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.application.service.storage.IMicroStorageFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageDto;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageHistory;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageModify;
import com.cosmo.hhim.micro.base.domain.entity.storage.NegativeStockAnalysisTip;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageHistoryService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * 库存Controller
 *
 * @date 2022-10-11
 */
@RestController
@RequestMapping("/storage")
public class MicroProcessStorageController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroProcessStorageService processStorageService;
    @Autowired
    private IMicroProcessStorageHistoryService processStorageHistoryService;
    @Autowired
    private MicroSupportUtil supportUtil;
    @Autowired
    private IMicroStorageFacadeService microStorageFacadeService;

    /**
     * 查询库存列表
     */
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String seqKey,
                              @RequestParam(required = false) String productSeq,
                              @RequestParam(required = false) String processSeq) {
        startPage();
//        List<MicroProcessStorage> list = processStorageService.selectMicroProcessStorageListBySeq(seqKey, productSeq, processSeq); 
        List<MicroProcessStorage> list = microStorageFacadeService.selectMicroProcessStorageListBySeq(seqKey, productSeq, processSeq);
        return getDataTable(list);
    }

    /**
     * 查询产品/工序下拉列表
     */
    @GetMapping("/select")
    public AjaxResult select(@RequestParam String key) {
        return AjaxResult.success(processStorageService.selectProcessStorageCondition(key));
    }

    /**
     * 查询库存变动列表
     */
    @GetMapping("/history")
    public TableDataInfo list(@RequestParam("processSeq") String processSeq,
                              @RequestParam("productSeq") String productSeq,
                              @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                              @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) throws ParseException {
        startPage();
        MicroProcessStorageHistory history = new MicroProcessStorageHistory();
        history.setProcessSeq(processSeq);
        history.setProductSeq(productSeq);
        history.setStartDate(startDate);
        history.setEndDate(endDate);
        List<MicroProcessStorageHistory> list = processStorageHistoryService.selectMicroProcessStorageHistoryList(history);
        return getDataTable(list);
    }

    /**
     * 获取库存详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(processStorageService.selectMicroProcessStorageById(id));
    }

    /**
     * 根据工位/工序获取库存信息(库存手动调整)
     */
    @GetMapping(value = "/detail")
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult getDetail(@RequestParam("processSeq") String processSeq, @RequestParam("productSeq") String productSeq) {
        MicroProcessStorageDto storage = new MicroProcessStorageDto();
        storage.setProcessSeq(processSeq);
        storage.setProductSeq(productSeq);
        return AjaxResult.success(processStorageService.selectMicroProcessStorageByInfo(storage));
    }

    /**
     * 修改库存
     */
    @PostMapping("/edit")
    @Log(title = "库存模块", businessType = BusinessType.UPDATE)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult edit(@RequestBody List<MicroProcessStorageModify> modifyList) {
        return toAjax(processStorageService.updateMicroProcessStorage(modifyList));
    }

    /**
     * 在制品查询 - （产品维度）
     *
     * @param productNameOrCode
     * @return
     */
    @GetMapping("/product/list")
    public TableDataInfo selectMicroProcessStorageListByProduct(@RequestParam(value = "productNameOrCode", required = false) String productNameOrCode,
                                                                @RequestParam(value = "productSeq", required = false) String productSeq) {
        startPage();
        List<MicroProcessStorage> list = processStorageService.selectMicroProcessStorageListByProduct(productNameOrCode, productSeq);
        return getDataTable(list);
    }

    /**
     * 在制品查询 - (产品 + 工序)
     *
     * @param microProcessStorageDto
     * @return
     */
    @GetMapping("/condition/list")
    public TableDataInfo selectMicroProcessStorageListByCondition(MicroProcessStorageDto microProcessStorageDto) {
        startPage();
        List<MicroProcessStorage> list = microStorageFacadeService.selectMicroProcessStorageListByCondition(microProcessStorageDto);
        return getDataTable(list);

    }

    /**
     * 推荐库存调整原因标签
     */
    @GetMapping("/recommendChangeTag")
    public AjaxResult recommendChangeTag() {
        return AjaxResult.success(supportUtil.recommendChangeTag());
    }

    /**
     * 负库存分析
     *
     * @param productSeq
     * @param processSeq
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/negativeStockAnalysis")
    public AjaxResult negativeStockAnalysis(@RequestParam("productSeq") String productSeq,
                                            @RequestParam("processSeq") String processSeq,
                                            @RequestParam(value = "startDate", required = false) Date startDate,
                                            @RequestParam(value = "endDate", required = false) Date endDate) {
        NegativeStockAnalysisTip s = processStorageService.negativeStockAnalysis(productSeq, processSeq, startDate, endDate);
        return AjaxResult.success(s);
    }
}
