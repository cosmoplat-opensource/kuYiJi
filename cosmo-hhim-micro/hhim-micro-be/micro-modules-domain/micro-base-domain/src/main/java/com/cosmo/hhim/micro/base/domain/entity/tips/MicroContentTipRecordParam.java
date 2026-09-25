/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tips;

import lombok.Data;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-02-03
 */
@Data
public class MicroContentTipRecordParam {

    // 用户ID 
    private Long userId;

    // 用户类型(10：正式，20：试用) 
    private String userType;

    // 开始时间 
    private Date startDate;

    // 结束时间 
    private Date endDate;

    // 提示配置ID 
    private Long tipConfigId;

}
