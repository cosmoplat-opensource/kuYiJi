/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 报工记录前端入参
 * @date 2022/10/13 4:08 下午
 */
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroWorkSubmitDto extends MicroWorkSubmit {

    /**
     * 产品纬度group之后的多条报工记录id
     */
    private String ids;

    /**
     * 产品名称或编码查询字段
     */
    private String productNameOrCode;

    /**
     * 工序名称或者编码查询字段
     */
    private String processNameOrCode;

    /**
     * 开始时间前端入参
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 结束时间前端入参
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /**
     * 良品率
     */
    private BigDecimal passRate;

    /**
     * 不良品率
     */
    private BigDecimal ngRate;

    /**
     * 取消审核列表查询: 0-展示取消审核  1-不展示取消审核
     * 前端传递参数 : 用于过滤审核时间三十天范围内的
     */
    private Long cancelCheckType;

    /**
     * 负库存风险标示 - 0：yes  1：no
     */
    private String negativeStockFlag;

    /**
     * 低良品率标示 - 0：yes 1:no
     */
    private String lowPassRateFlag;

    /**
     * 超产能标示 - 0: yes 1:no
     */
    private String overProductiveCapacityFlag;

    /**
     * 日均良品率
     */
    private BigDecimal avgPassRateByDay;

    /**
     * 日均产能
     */
    private BigDecimal avgProductionCapacityByDay;

    /**
     * 异常数据条数
     */
    private int exceptionNums;

    /**
     * 前工序库存数量
     */
    private BigDecimal preProcessStockNum;

    /**
     * 前工序未审核数量
     */
    private BigDecimal preProcessWaitCheckPassNum;

    /**
     * 新添加 超产能风险异常字段
     */
    private BigDecimal todaySubmitNum;

    /**
     * 超产能风险异常的 跳转ids, 根据id获取数量
     */
    private String overProductiveCapacityExceptionRecordIds;

    private List<Long> submitUserIds;
}
