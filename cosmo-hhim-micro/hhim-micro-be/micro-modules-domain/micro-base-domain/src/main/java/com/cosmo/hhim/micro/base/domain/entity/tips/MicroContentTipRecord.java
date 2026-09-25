/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tips;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 内容提示记录对象 micro_content_tip_record
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-03
 */
@Data
public class MicroContentTipRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    // 用户类型(10：正式，20：试用) 
    private String userType;

    /**
     * 提示内容
     */
    private String tipContent;

    /**
     * 提示所关联的配置ID
     */
    private Long tipConfigId;

    /**
     * 提示时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date tipDate;
}
