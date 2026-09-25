/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.check;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 到工单、工序维度下面的人员报工记录信息
 * @date 2023/3/23 18:03
 */
@Data
public class UserDetailSubmitRecordInfoByWorkOder {

    /**
     * 报工记录
     */
    private Long id;

    private Long userId;

    private String nickName;

    private BigDecimal passNum;

    private BigDecimal ngNum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;
}
