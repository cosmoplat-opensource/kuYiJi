/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.check;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 查询工单、工序下的报工记录信息入参实体
 * @date 2023/3/23 18:06
 */
@Data
public class UserSubmitRecordQueryParam {

    /**
     * 工单号
     */
    private String workOrderNo;

    /**
     * 工序序列码
     */
    private String processSeq;

    /**
     * 用户id组合
     */
    private Long[] userIds;

    /**
     * 审核状态
     */
    private String submitStatus;
}
