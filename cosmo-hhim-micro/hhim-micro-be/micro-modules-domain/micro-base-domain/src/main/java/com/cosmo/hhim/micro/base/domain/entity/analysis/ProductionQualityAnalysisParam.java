/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * @author cosmo-hhim-open Team
 * @description: 产品质量分析的参数对象
 * @date 2022/10/31 1:27 下午
 */
@Data
public class ProductionQualityAnalysisParam {

    /**
     * 产品编码或者名称
     */
    private String productNameOrCode;

    /**
     * 开始时间参数
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束时间参数
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 按照月份还是按照天数展示 质量分析模块需要
     * <p>
     * 0 - 按照月份 本年
     * 1 - 按照天数 本月或者自定义
     */
    private String type;

    /**
     * 分析首页
     * <p>
     * 0 - 本天
     * 1 - 本月
     * 2 - 本年
     * 3 - 自定义
     */
    private String timeType;

    /**
     * 报工记录审核状态
     */
    private Long submitStatus;

    /**
     * 用户名参数，用于获取员工下面产品的数量信息
     */
    private String userName;

    /**
     * 报工人昵称
     */
    private String submitNickName;

    /**
     * 产品唯一序列码
     */
    private String productSeqList;

    /**
     * 报工方式
     *
     * 1 - Ku易记
     * 2 - 工易派
     */
    private Long submitType;

    /**
     * 操作工序序列码
     */
    private String productSeq;
}
