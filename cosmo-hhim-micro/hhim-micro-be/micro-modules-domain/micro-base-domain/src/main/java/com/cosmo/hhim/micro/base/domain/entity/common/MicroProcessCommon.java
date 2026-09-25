/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSortEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * process对象 micro_process_common
 *
 * @date 2022-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroProcessCommon extends MicroSortEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 工序唯一码（以此字段关联）
     */
    private String processSeq;

    /**
     * 工序编码
     */
    private String processCode;

    /**
     * 工序名称
     */
    @NotNull(message = "工序名称不能为空")
    private String processName;

    /**
     * 工序描述
     */
    private String processDesc;

    /**
     * 工序分组
     */
    private String processGroup;

    private String remark;
    /**
     * 创建类型 手动/自动
     */
    private String createdType;
    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdDate;

    /**
     * 最后修改人
     */
    private String lastUpdBy;
    /**
     * 搜索key
     */
    private String key;

    /**
     * 最后修改时间
     */
    private Date lastUpdDate;

    public void setCreateInfo() {
        String userId = String.valueOf(SecurityUtils.getUserId());
        if (StringUtils.hasText(userId)) {
            if (!StringUtils.hasText(this.createdBy)) {
                this.createdBy = userId;
            }
        }
        this.setCreatedDate(DateUtils.getNowDate());
    }
}
