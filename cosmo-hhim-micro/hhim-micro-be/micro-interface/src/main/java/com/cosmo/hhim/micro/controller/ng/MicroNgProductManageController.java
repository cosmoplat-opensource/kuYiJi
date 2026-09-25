/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.ng;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.micro.application.dto.ng.QualityControlOfSubmitRecordDto;
import com.cosmo.hhim.micro.application.dto.ng.QualityControlOfSubmitRecordInfo;
import com.cosmo.hhim.micro.application.dto.ng.RepairAndThenQualityControlParam;
import com.cosmo.hhim.micro.application.service.ng.IMicroNgProductManageFacadeService;
import com.cosmo.hhim.micro.application.service.submit.IMicroSubmitFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.ng.NgProductByProductFromAlreadyCheck;
import com.cosmo.hhim.micro.base.domain.entity.ng.NgProductByUserFromAlreadyCheck;
import com.cosmo.hhim.micro.base.domain.entity.ng.NgQueryParam;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.SubmitRecordQueryParam;
import com.cosmo.hhim.micro.ng.domain.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 质检记录Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-31
 */
@RestController
@RequestMapping("/ngProduct/manage")
public class MicroNgProductManageController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroNgProductManageFacadeService microNgProductManageFacadeService;
    @Autowired
    private IMicroSubmitFacadeService microSubmitFacadeService;

    /**
     * 送检接口
     *
     * @param microWorkSubmitDto
     * @return
     */
    @PostMapping("/submitForInspection")
    public AjaxResult submittalForInspection(@RequestBody MicroWorkSubmitDto microWorkSubmitDto) {
        return AjaxResult.success(microSubmitFacadeService.submittalForInspection(microWorkSubmitDto));
    }

    /**
     * 质检员 - 报工记录的质检列表
     *
     * @param submitRecordQueryParam
     * @return
     */
    @GetMapping("/qc/list")
    public TableDataInfo selectQualityControlOfSubmitRecordList(SubmitRecordQueryParam submitRecordQueryParam) {
        startPage();
        List<QualityControlOfSubmitRecordInfo> list = microNgProductManageFacadeService.selectQualityControlOfSubmitRecordList(submitRecordQueryParam);
        return getDataTable(list);
    }

    /**
     * 质检记录历史
     *
     * @param qualityControlHistoryQueryParam
     * @return
     */
    @GetMapping("/qc/history/list")
    public TableDataInfo selectQualityControlHistoryList(QualityControlHistoryQueryParam qualityControlHistoryQueryParam) {
        startPage();
        List<QualityControlHistoryInfo> list = microNgProductManageFacadeService.selectQualityControlHistoryInfoList(qualityControlHistoryQueryParam);
        return getDataTable(list);
    }

    /**
     * 质检操作
     *
     * @param qualityControlOfSubmitRecordDto
     * @return
     */
    @PostMapping("/qc")
    public AjaxResult qualityControlOfSubmitRecord(@RequestBody QualityControlOfSubmitRecordDto qualityControlOfSubmitRecordDto) {
        return AjaxResult.success(microNgProductManageFacadeService.qualityControlOfRecord(qualityControlOfSubmitRecordDto));
    }

    /**
     * 批量质检操作
     *
     * @param ids
     * @return
     */
    @GetMapping("/batchQc")
    public AjaxResult batchQualityControlOfSubmitRecord(@RequestParam Long[] ids) {
        return AjaxResult.success(microNgProductManageFacadeService.batchQualityControlOfRecord(ids));
    }

    /**
     * 不良类型
     *
     * @param ngTypeQueryParam
     * @return
     */
    @GetMapping("/ngType/list")
    public AjaxResult selectNgTypeList(NgTypeQueryParam ngTypeQueryParam) {
        return AjaxResult.success(microNgProductManageFacadeService.selectNgTypeList(ngTypeQueryParam));
    }

    /**
     * 根据报工记录id获取质检明细
     *
     * @param id
     * @return
     */
    @GetMapping("/info/{id}")
    public AjaxResult getDetailQcInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(microNgProductManageFacadeService.getDetailQcInfo(id));
    }

    /**
     * 不良品清单列表 - 产品维度
     * 
     * @param productNameOrCode
     * @return
     */
    @GetMapping("/list/product")
    public TableDataInfo selectNgList(@RequestParam(value = "productNameOrCode", required = false) String productNameOrCode) {
        startPage();
        List<NgProductByProductFromAlreadyCheck> ngProductList = microNgProductManageFacadeService.selectNgProductListFromAlreadyCheck(productNameOrCode);
        return getDataTable(ngProductList);
    }

    /**
     * 获取不良品清单到 产品 + 工序 + 人 维度
     *
     * @param productSeq
     * @param processSeq
     * @return
     */
    @GetMapping("/list/product/user")
    public AjaxResult selectNgProductAndProcessByUserFromAlreadyCheck(@RequestParam(value = "productSeq") String productSeq,
                                                                      @RequestParam(value = "processSeq") String processSeq) {
        return AjaxResult.success(microNgProductManageFacadeService.selectNgProductAndProcessByUserFromAlreadyCheck(productSeq, processSeq));
    }

    /**
     * 不良品清单列表 - 员工维度
     *
     * @param submitNickName
     * @return
     */
    @GetMapping("/list/user")
    public TableDataInfo selectNgProductListByUserFromAlreadyCheck(@RequestParam(value = "submitNickName", required = false) String submitNickName) {
        startPage();
        List<NgProductByUserFromAlreadyCheck> list = microNgProductManageFacadeService.selectNgProductListByUserFromAlreadyCheck(submitNickName);
        return getDataTable(list);
    }

    /**
     * 返修复核时的页面信息
     *
     * @param ngQueryParam
     * @return
     */
    @GetMapping("/infoWhenRepair")
    public AjaxResult infoWhenRepair(NgQueryParam ngQueryParam) {
        return AjaxResult.success(microNgProductManageFacadeService.selectQualityControlInfoWhenRepair(ngQueryParam));
    }

    /**
     * 返修复核接口
     *
     * @param repairAndThenQualityControlParam
     * @return
     */
    @PostMapping("/repairAndThenQualityControl")
    public AjaxResult repairAndThenQualityControl(@RequestBody RepairAndThenQualityControlParam repairAndThenQualityControlParam) {
        return AjaxResult.success(microNgProductManageFacadeService.repairAndThenQualityControl(repairAndThenQualityControlParam));
    }

    /**
     * 返修记录列表
     *
     * @param queryKey
     * @return
     */
    @GetMapping("/repairRecord/list")
    public TableDataInfo selectMicroRepairRecordListByQueryKey(@RequestParam(value = "queryKey", required = false) String queryKey) {
        startPage();
        List<RepairRecordInfo> repairRecordInfos = microNgProductManageFacadeService.selectMicroRepairRecordListByQueryKey(queryKey);
        return getDataTable(repairRecordInfos);
    }

    /**
     * 根据返修单号获取返修记录信息
     *
     * @param repairNo
     * @return
     */
    @GetMapping("/detailRepairInfo")
    public AjaxResult getDetailRepairInfo(String repairNo) {
        return AjaxResult.success(microNgProductManageFacadeService.getDetailRepairAndThenQualityControlInfo(repairNo));
    }

    /**
     * 不良类型统计分析 - 饼状图
     *
     * @param ngTypeStatisticQueryParam
     * @return
     */
    @GetMapping("/ngTypeAnalysis")
    public AjaxResult ngTypeAnalysis(NgTypeStatisticQueryParam ngTypeStatisticQueryParam) {
        return AjaxResult.success(microNgProductManageFacadeService.obtainedNgTypeStatisticAnalysis(ngTypeStatisticQueryParam));
    }

    /**
     * 工作台不良品管理模块外部展示数量
     *
     * @return
     */
    @GetMapping("/obtainedNgProductTotalNumAndUser")
    public AjaxResult obtainedNgProductTotalNumAndUser() {
        return AjaxResult.success(microNgProductManageFacadeService.obtainedNgProductTotalNumAndUser());
    }

    /**
     * 不良品清单导出发送邮件
     *
     * @param receivedBy
     * @return
     */
    @Log(title = "不良品模块", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult exportNgProductStatisticsReport(@RequestParam("receivedBy") String receivedBy) {
        return AjaxResult.success(microNgProductManageFacadeService.exportNgProductStatisticsReport(receivedBy));
    }
}
