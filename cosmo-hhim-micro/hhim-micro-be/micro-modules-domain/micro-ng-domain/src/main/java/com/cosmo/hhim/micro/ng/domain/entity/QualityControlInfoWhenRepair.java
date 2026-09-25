/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 返修复核时获取的质检记录返回信息
 * @date 2023/4/6 11:23
 */
@Data
public class QualityControlInfoWhenRepair {

    /**
     * 质检人昵称
     */
    private String qcNickName;

    /**
     * 质检时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date qcDate;

    /**
     * 不良品数量
     */
    private BigDecimal totalNgNum;

    List<NgProductDetailInfo> ngProductDetailInfoList;
}
