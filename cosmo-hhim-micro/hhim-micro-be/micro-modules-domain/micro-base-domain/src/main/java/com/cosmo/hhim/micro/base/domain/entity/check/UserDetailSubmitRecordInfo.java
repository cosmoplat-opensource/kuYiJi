/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.check;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 存储产品维度用户报工记录的信息
 * @date 2023/2/9 11:26
 */
@Data
public class UserDetailSubmitRecordInfo {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 该员工下的报工记录是否有预警标示
     * <p>
     * 0 - 是, 1 - 否
     */
    private String warnFlag;

    /**
     * 良品数量
     */
    private BigDecimal passNum;

    /**
     * 不良品数量
     */
    private BigDecimal ngNum;

    /**
     * 异常记录数
     */
    private int exceptionRecordNum;

    /**
     * 产品维度下各用户汇总之后的报工记录id
     */
    private List<Long> ids;

    /**
     * 待检测标示
     *
     * 0 - 是， 1 - 否
     */
    private Long waitQcFlag;
}