/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 报工记录查询参数
 * @date 2023/4/3 13:53
 */
@Data
public class SubmitRecordQueryParam {

    /**
     * 产品序列码
     */
    private String productSeq;

    /**
     * 报工方式
     *
     * 1 - ku易记， 2 - 工易派
     */
    private Long submitType;

    /**
     * 审核状态
     *
     * 0 - 审核通过， 1 - 待审核， 2 - 驳回
     */
    private Long submitStatus;

    /**
     * 报工人的昵称
     */
    private String submitNickName;

    /**
     * 报工人的userId汇总
     */
    private List<Long> submitUserIds;

    /**
     * 产品名称或者编码
     */
    private String productNameOrCode;

    /**
     * 工序名称或者编码
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
     * 取消审核列表查询: 0-展示取消审核  1-不展示取消审核
     * 前端传递参数 : 用于过滤审核时间三十天范围内的
     */
    private Long cancelCheckType;

    /**
     * 产品纬度group之后的多条报工记录id
     */
    private String ids;

    /**
     * 根据备注查询
     */
    private String remark;

    /**
     * 是否质检完成
     *
     * 0 - 待送检， 1 - 待质检， 2 - 质检完成
     */
    private Long checkStatus;
}
