/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.storage;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.micro.application.service.storage.IMicroFinishStorageFacadeService;
import com.cosmo.hhim.micro.infrastructure.enums.FinishStorageChangeTypeEnum;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishProductStorageAdjustParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishProductStorageBoundBatchParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishProductStorageBoundParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishStorageChangeHistoryParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.entity.WarnFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.entity.WarnStorageParam;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageHistoryService;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * 成品库存Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
@Api
@RestController
@RequestMapping("/storage/finish")
public class MicroFinishedProductStorageController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroFinishedProductStorageService microFinishedProductStorageService;
    @Autowired
    private IMicroFinishedProductStorageHistoryService microFinishedProductStorageHistoryService;
    @Autowired
    private IMicroFinishStorageFacadeService microFinishStorageFacadeService;

    /**
     * 查询成品库存列表
     */
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(value = "productCodeOrName", required = false) String productCodeOrName) {
        startPage();
        MicroFinishedProductStorage queryParam = new MicroFinishedProductStorage();
        queryParam.setProductCodeOrName(productCodeOrName);
        List<MicroFinishedProductStorage> list = microFinishStorageFacadeService.selectMicroFinishedProductStorageList(queryParam);
        return getDataTable(list);
    }

    /**
     * 导出成品库存列表
     */
    @Log(title = "成品库存模块", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(@RequestParam(value = "productCodeOrName", required = false) String productCodeOrName, @RequestParam(name = "receivedBy") String receivedBy) {
        return AjaxResult.success(microFinishStorageFacadeService.exportFinishStorageInfos(productCodeOrName, receivedBy));
    }

    /**
     * 查询库存变动记录列表
     */
    @GetMapping(value = "/history/list")
    public AjaxResult changeHistoryList(MicroFinishStorageChangeHistoryParam queryParam) {
        if (null != queryParam.getStartDate()) {
            queryParam.setStartDate(DateUtil.beginOfDay(queryParam.getStartDate()).toJdkDate());
        }
        if (null != queryParam.getEndDate()) {
            queryParam.setEndDate(DateUtil.endOfDay(queryParam.getEndDate()).toJdkDate());
        }
        return AjaxResult.success(microFinishStorageFacadeService.changeHistoryList(queryParam));
    }

    /**
     * 导出库存变动记录列表
     */
    @Log(title = "成品库存模块", businessType = BusinessType.EXPORT)
    @GetMapping("/history/export")
    public AjaxResult historyExport(@RequestParam(name = "productSeq") String productSeq,
                                    @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                    @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                    @RequestParam(name = "receivedBy") String receivedBy) {
        if (null != startDate) {
            startDate = DateUtil.beginOfDay(startDate).toJdkDate();
        }
        if (null != endDate) {
            endDate = DateUtil.endOfDay(endDate).toJdkDate();
        }
        return AjaxResult.success(microFinishStorageFacadeService.exportFinishStorageHistoryInfos(startDate, endDate, productSeq, receivedBy));
    }


    /**
     * 编辑库存信息
     */
    @Log(title = "成品库存模块", businessType = BusinessType.UPDATE)
    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody MicroFinishProductStorageAdjustParam param) {
        param.setChangeType(FinishStorageChangeTypeEnum.STORAGE_CHANGE.getCode());
        microFinishedProductStorageService.updateMicroFinishedProductStorage(param);
        return AjaxResult.success();
    }

    /**
     * 批量出入库
     *
     * @return
     */
    @Log(title = "成品库存模块", businessType = BusinessType.UPDATE)
    @PutMapping("/batch/inOrOutBound")
    public AjaxResult inOrOutBoundBatch(@Valid @RequestBody MicroFinishProductStorageBoundBatchParam param) {
        // 参数合法性校验
        for (MicroFinishProductStorageBoundParam outBoundParam : param.getBoundParamList()) {
            outBoundParam.checkParam();
        }
        microFinishStorageFacadeService.batchInOrOutBound(param);
        return AjaxResult.success();
    }

    /**
     * 统计出入库总数、涉及产品数指标
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/history/statistic")
    public AjaxResult statisticStorageHistory(@RequestParam(name = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                              @RequestParam(name = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        if (null != startDate) {
            startDate = DateUtil.beginOfDay(startDate).toJdkDate();
        }
        if (null != endDate) {
            endDate = DateUtil.endOfDay(endDate).toJdkDate();
        }
        return AjaxResult.success(microFinishedProductStorageHistoryService.selectStorageChangedIndexInfo(startDate, endDate));
    }

    /**
     * 库存预警列表
     *
     * @param warnStorageParam
     * @return
     */
    @GetMapping("/warnList")
    public TableDataInfo selectMicroFinishedProductStorageForWarn(WarnStorageParam warnStorageParam) {
        startPage();
        List<WarnFinishedProductStorage> list = microFinishedProductStorageService.selectMicroFinishedProductStorageForWarn(warnStorageParam);
        return getDataTable(list);
    }

    /**
     * 预警列表中不同预警状态
     *
     * @param productNameOrCode
     * @return
     */
    @GetMapping("/countProductNumInDiffWarnStatus")
    public AjaxResult countProductNumInDiffWarnStatus(@RequestParam(required = false) String productNameOrCode) {
        return AjaxResult.success(microFinishedProductStorageService.countProductNumInDiffWarnStatus(productNameOrCode));
    }

    /**
     * 根据产品序列码获取
     *
     * @param productSeq
     * @return
     */
    @GetMapping("/getFinishStockByProductSeq")
    public AjaxResult getFinishStockByProductSeq(@RequestParam String productSeq) {
        return AjaxResult.success(microFinishedProductStorageService.selectMicroFinishedProductStorageByProductSeq(productSeq));
    }
}
