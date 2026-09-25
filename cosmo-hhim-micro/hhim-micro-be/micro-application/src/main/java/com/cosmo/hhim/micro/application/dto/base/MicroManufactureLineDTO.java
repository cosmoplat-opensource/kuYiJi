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
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author cosmo-hhim-open Team
 * @description: 工厂产线信息数据传输对象
 * @classname: MicroManufactureLineDTO
 * @date: 2023/3/9 10:42
 * @author cosmo-hhim-open Team
 * @version 1.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroManufactureLineDTO extends BaseEntity {

    private static final long serialVersionUID = 5163255241451811578L;

    /** 主键 */
    private Long id;

    /** 生产线编号 */
    private String mlineCode;

    /** 生产线名称 */
    private String mlineName;

    /** 车间编号 */
    private String wshopCode;

    /** 车间名称 */
    private String wshopName;

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

    private String key;
}
