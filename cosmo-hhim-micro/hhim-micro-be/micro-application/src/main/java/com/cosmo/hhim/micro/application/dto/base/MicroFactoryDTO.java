/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.base;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author cosmo-hhim-open Team
 * @description: 工厂信息数据传输对象
 * @classname: MicroFactoryDTO
 * @date: 2023/3/7 17:54
 * @author cosmo-hhim-open Team
 * @version 1.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroFactoryDTO extends BaseEntity {
    private static final long serialVersionUID = 3223123117558342568L;

    /** $column.columnComment */
    private Long id;

    /** 激活标记 1是 0否 */
    private String activeFlag;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createdDate;

    /** 最后修改人 */
    private String lastUpdBy;

    /** 最后修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lastUpdDate;

    /** 租户编码 */
    private String tenantCode;

    /** 车间  */
    List<MicroWorkShopDTO> workShopList;

    boolean isLazy; 
}
