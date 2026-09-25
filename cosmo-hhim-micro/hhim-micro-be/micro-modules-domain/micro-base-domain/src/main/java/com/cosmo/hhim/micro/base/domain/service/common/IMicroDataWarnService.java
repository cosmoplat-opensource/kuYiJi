/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.submit.Submitter;
import com.cosmo.hhim.micro.base.domain.entity.warn.DataHealth;
import com.cosmo.hhim.micro.base.domain.entity.warn.ProductWarn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author cosmo-hhim-open Team
 * @description: 工序预警服务接口
 * @date 2022/12/9 13:55
 */
public interface IMicroDataWarnService {

    /**
     * 产品有多个尾序的异常情况
     *
     * @return
     */
    List<ProductWarn> selectMultiLastProcessForProduct();

    /**
     * 没有尾序的产品
     *
     * @return
     */
    List<ProductWarn> selectNotHaveLastProcessForProduct();

    /**
     * 没有首序的产品
     *
     * @return
     */
    List<ProductWarn> selectNotHaveFirstProcessForProduct();

    /**
     * 针对异常情况，为产品指定首序或最后一道工序
     *
     * @param productSeq
     * @param processSeq
     * @param processWarnType
     * @return
     */
    Boolean assignFirstOrLastProcessToProduct(String productSeq, String processSeq, String processWarnType);


    /**
     * 时间范围内报工十次且被纠正过数量的报工人
     *
     * @param startDate
     * @param endDate
     * @return
     */
    Set<Submitter> findModifiedRecordOverThreeTimesEmployees(Date startDate, Date endDate);

    /**
     * 时间范围内连续三天没有报工的工人
     *
     * @param startDate
     * @param endDate
     * @return
     */
    Set<Submitter> findConsecutiveThreeDaysNoRecordEmployees(LocalDate startDate, LocalDate endDate);

    /**
     * 已当前时间为准，连续七天没有报工的员工
     *
     * @return
     */
    Set<Submitter> findConsecutiveSevenDaysNoRecordEmployees();

    /**
     * 超出日产能的员工
     *
     * @return
     */
    List<Submitter> findOverDayCapacityEmployee();

    /**
     * 检查报工数量是否符合规范
     *
     * @param totalSubmitNum
     * @param submitUser
     * @param productSeq
     * @param processSeq
     * @return
     */
    String checkSubmitNum(BigDecimal totalSubmitNum, String submitUser, String productSeq, String processSeq);

    /**
     * 主数据清理（产品或者工序）
     *
     * @param masterType
     * @return
     */
    List checkMasterData(String masterType);

    /**
     * 主数据数量（产品或者工序）
     *
     * @param masterType
     * @return
     */
    Long checkMasterDataCount(String masterType);

    /**
     * 计算数据健康度
     *
     * @return
     */
    DataHealth calculateDataHealth();
}
