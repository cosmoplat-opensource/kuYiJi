/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.mail.MailService;
import com.cosmo.hhim.micro.base.domain.entity.analysis.FinishedProduct;
import com.cosmo.hhim.micro.base.domain.entity.export.QualityTrendsInfo;
import com.cosmo.hhim.micro.base.domain.entity.mail.StockChangeHistoryRecord;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageDto;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageExportModelResult;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageHistory;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.SubmitRecordQueryParam;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroAnalysisMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroEmailService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroExportAndSendEmailService;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitService;
import com.cosmo.hhim.micro.infrastructure.constant.MailConstants;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author cosmo-hhim-open Team
 * @description: 导出并发送邮件的接口实现类
 * @date 2023/1/6 09:49
 */
@Service
@Slf4j
public class MicroExportAndSendEmailServiceImpl implements IMicroExportAndSendEmailService {

    @Autowired
    private MailService mailService;
    @Autowired
    private IMicroEmailService microEmailService;
    @Autowired
    private IMicroWorkSubmitService microWorkSubmitService;
    @Autowired
    private MicroAnalysisMapper microAnalysisMapper;
    @Autowired
    private MicroProcessStorageMapper microProcessStorageMapper;
    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;
    @Autowired
    private MicroProcessStorageHistoryMapper microProcessStorageHistoryMapper;
    @Autowired
    private MicroSupportUtil microSupportUtil;

    /**
     * 报工记录 - 导出发送邮件
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    @Override
    @Deprecated
    public String exportEmployeeSubmitRecord(Date startDate, Date endDate, String receivedBy) {
        log.info("请求参数为:startDate:{}-endDate:{}-receivedBy:{}", startDate, endDate, receivedBy);
        // 校验邮箱格式
        if (!this.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }
        // 构建导出数据
        SubmitRecordQueryParam param = new SubmitRecordQueryParam();
        param.setSubmitStatus(SubmitStatusEnum.APPROVED.getCode());
        param.setStartDate(startDate);
        param.setEndDate(endDate);
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitService.selectSubmitRecordByUser(param);

        // 导出并发送邮件
        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.EMPLOYEE_SUBMIT_EXCEL_NAME);
        return microEmailService.exportAndSendEmail(MailConstants.EMPLOYEE_SUBMIT_MAIL_SUBJECT, MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID,
                receivedBy, microWorkSubmitDtoList, MicroWorkSubmitDto.class, placeholderMap);
    }

    /**
     * 完工产品 - 导出并发送邮件
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    @Override
    public String exportFinishedProduct(Date startDate, Date endDate, String receivedBy) {
        log.info("请求参数为:startDate:{}-endDate:{}-receivedBy:{}", startDate, endDate, receivedBy);
        // 校验邮箱格式
        if (!this.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }

        // 构建数据
        LocalDate start = this.dateToLocalDate(startDate);
        LocalDate end = this.dateToLocalDate(endDate);
        List<FinishedProduct> finishedProducts = microAnalysisMapper.getFinishedProductStatistics(start, end);

        // 添加统计时间范围字段
        String timeRange = start + "~" + end;
        if (!CollectionUtils.isEmpty(finishedProducts)) {
            for (FinishedProduct finishedProduct : finishedProducts) {
                finishedProduct.setTimeRange(timeRange);
            }
        }

        // 导出并发送邮件
        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.FINISHED_PRODUCT_EXCEL_NAME);
        return microEmailService.exportAndSendEmail(MailConstants.FINISHED_PRODUCT_MAIL_SUBJECT, MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID,
                receivedBy, finishedProducts, FinishedProduct.class, placeholderMap);
    }

    /**
     * 车间库存 - 导出excel并发送邮件
     *
     * @param receivedBy
     * @return
     */
    @Override
    public String exportAllProcessStorage(String receivedBy) {
        log.info("请求参数为:receivedBy:{}", receivedBy);
        // 校验邮箱格式
        if (!this.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }

        // 构建数据
        List<MicroProcessStorage> allProcessStorageList = microProcessStorageMapper.selectMicroProcessStorageList(new MicroProcessStorageDto());

        // 导出并发送邮件
        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.PROCESS_STORAGE_EXCEL_NAME);
        return microEmailService.exportAndSendEmail(MailConstants.PROCESS_STORAGE_MAIL_SUBJECT, MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID,
                receivedBy, allProcessStorageList, MicroProcessStorage.class, placeholderMap);
    }

    /**
     * 质量趋势 - 导出excel并发送邮件
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    @Override
    public String exportQualityTrends(Date startDate, Date endDate, String receivedBy) {
        log.info("请求参数为:startDate:{}-endDate:{}-receivedBy:{}", startDate, endDate, receivedBy);
        // 校验邮箱格式
        if (!this.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }

        // 构建数据
        MicroWorkSubmitDto param = new MicroWorkSubmitDto();
        param.setStartDate(startDate);
        param.setEndDate(endDate);
        param.setSubmitStatus(SubmitStatusEnum.APPROVED.getCode());
        param.setSubmitType(microSupportUtil.getSubmitType());
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectCompletedProductListByProductAddProcess(param);
        if (CollectionUtils.isEmpty(microWorkSubmitDtoList)) {
            throw new CustomException("时间范围内没有数据可导出");
        }
        List<QualityTrendsInfo> exportData = new ArrayList<>();
        // 添加统计时间范围字段
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String timeRange = simpleDateFormat.format(startDate) + "~" + simpleDateFormat.format(endDate);
        // 设置良品和不良品率
        microWorkSubmitDtoList.forEach(obj -> {
            BigDecimal totalNums = obj.getCheckNgNum().add(obj.getCheckPassNum());
            // 无质检数（合计=0）的行算不出比率 → 跳过该行（原实现直接 divide(0) → 导出/发邮件 500）
            if (totalNums.signum() == 0) {
                log.warn("[导出] {} 质检数为0，已跳过该行", obj.getProductName());
                return;
            }
            obj.setPassRate(obj.getCheckPassNum().divide(totalNums, 3, RoundingMode.DOWN));
            obj.setNgRate(obj.getCheckNgNum().divide(totalNums, 3, RoundingMode.DOWN));

            // 构建要导出的数据
            QualityTrendsInfo temp = new QualityTrendsInfo();
            BeanUtils.copyProperties(obj, temp);
            temp.setPassNum(obj.getCheckPassNum());
            temp.setNgNum(obj.getCheckNgNum());
            temp.setPassRateString(temp.getPassRate().multiply(BigDecimal.valueOf(100L)).stripTrailingZeros().toPlainString() + "%");
            temp.setTimeRange(timeRange);
            exportData.add(temp);
        });
        // 按照良品率排序 (降序)
        exportData.sort(Comparator.comparing(QualityTrendsInfo::getPassRate).reversed());

        // 导出excel并发送邮件
        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.QUALITY_TRENDS_EXCEL_NAME);
        return microEmailService.exportAndSendEmail(MailConstants.QUALITY_TRENDS_MAIL_SUBJECT, MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID,
                receivedBy, exportData, QualityTrendsInfo.class, placeholderMap);
    }

    @Override
    public String exportStockChangeRecords(String productSeq, String processSeq, Date startDate, Date endDate, String receivedBy) {
        log.info("请求参数为:productSeq:{}-processSeq:{}-startDate:{}-endDate:{}-receivedBy:{}", productSeq, processSeq, startDate, endDate, receivedBy);

        // 校验邮箱格式
        if (!this.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }

        // 构造查询参数
        MicroProcessStorageHistory queryParam = new MicroProcessStorageHistory();
        queryParam.setProductSeq(productSeq);
        queryParam.setProcessSeq(processSeq);
        queryParam.setStartDate(startDate);
        queryParam.setEndDate(endDate);
        List<MicroProcessStorageHistory> processStorageHistoryList = microProcessStorageHistoryMapper.selectMicroProcessStorageHistoryList(queryParam);
        if (CollectionUtils.isEmpty(processStorageHistoryList)) {
            throw new CustomException("未查询到相关库存历史变动记录");
        }

        // 实体转换
        CollectionUtil.reverse(processStorageHistoryList);
        List<StockChangeHistoryRecord> exportData = new ArrayList<>();
        processStorageHistoryList.forEach(obj -> {
            StockChangeHistoryRecord temp = new StockChangeHistoryRecord();
            temp.setCreatedDate(obj.getCreatedDate());
            temp.setProductCode(obj.getProductCode());
            temp.setProductName(obj.getProductName());
            temp.setProcessCode(obj.getProcessCode());
            temp.setProcessName(obj.getProcessName());
            temp.setOperateNode(obj.getOperateNode());
            temp.setOperatorName(obj.getCreatedBy());
            temp.setChangePassNum(obj.getPassToNum().subtract(obj.getPassFromNum()));
            temp.setPassNum(obj.getPassToNum());
            temp.setChangeNgNUm(obj.getNgToNum().subtract(obj.getNgFromNum()));
            temp.setNgNum(obj.getNgToNum());
            temp.setRemark(obj.getRemark());
            exportData.add(temp);
        });

        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.STOCK_CHANGE_EXCEL_NAME);
        return microEmailService.exportAndSendEmail(MailConstants.STOCK_CHANGE_MAIL_SUBJECT, MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID,
                receivedBy, exportData, StockChangeHistoryRecord.class, placeholderMap);
    }

    /**
     * 车间库存 - 导出期初工序库存导入模版
     *
     * @param receivedBy
     * @return
     */
    @Override
    public String exportProcessStorageTemplate(String receivedBy) {
        log.info("请求参数为: receivedBy:{}", receivedBy);

        // 校验邮箱格式
        if (!this.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }

        // 构建导出数据
        List<MicroProcessStorageExportModelResult> resultList = microProcessStorageMapper.selectAllMicroProcessStorageList();

        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.PROCESS_STORAGE_IMPORT_TEMPLATE_EXCEL_NAME);

        // 导出并发送邮件
        return microEmailService.exportAndSendEmailByEasyPoi(MailConstants.PROCESS_STORAGE_IMPORT_TEMPLATE_MAIL_SUBJECT,
                MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID, receivedBy, resultList, MicroProcessStorageExportModelResult.class, placeholderMap);
    }

    /**
     * 邮件格式验证
     *
     * @param receivedBy
     * @return
     */
    public boolean isValidEmail(String receivedBy) {
        if (StringUtils.isNotEmpty(receivedBy)) {
            return Pattern.matches("^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$", receivedBy);
        }
        return false;
    }

    /**
     * date 转 localDate
     *
     * @param date
     * @return
     */
    public LocalDate dateToLocalDate(Date date) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate = date.toInstant().atZone(zoneId).toLocalDate();
        return localDate;
    }
}
