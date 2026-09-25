/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.planning;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 生产订单进度追踪入参
 * @date 2023/6/8 10:13
 */
@Data
public class OrderProgressParam {

    /**
     * 订单编号
     */
    private String orderNo;

    private String productSeq;

    /**
     * 订单状态集合，主要是为了适用于已完成和关闭两种状态联合查询
     */
    private List<String> orderStatusList;

    /**
     * 客户编码
     */
    private String owCode;

    /**
     * 开始日期
     */
    private Date startDate;

    /**
     * 结束日期
     */
    private Date endDate;
}
