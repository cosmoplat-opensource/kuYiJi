/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户个性化配置对象 micro_user_individuation_config
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-01
 */
@Data
public class MicroUserIndividuationConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 个性化推荐(0关闭,1开始)
     */
    private Integer personalized;


    /**
     * 统计数据开始时间,如果为空,说明全量统计,如果有值,那从这个时间节点向后统计,为了角色发生变化导致的推荐不准确问题
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date statisticsBeginTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

}
