/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 企业个性化设置配置对象 micro_tenant_individuation_config
 *
 * @author cosmo-hhim-open Team
 * @date 2023-04-04
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroTenantIndividuationConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 记工送检开关（0:开启，1:关闭）
     */
    private String submitInspectSwitch;

    /**
     * 批量记工开关（0:开启，1:关闭）
     */
    private String batchSubmitSwitch;

    /**
     * 员工基础数据限制（0:开启，1:关闭）
     */
    private String workerBaseDataConfine;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

    /**
     * 更新人
     */
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdDate;

}
