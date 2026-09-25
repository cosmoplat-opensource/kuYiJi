/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

/**
 * 客户（企业）对象 hyzz_customer
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzCustomerApi extends BaseEntity{
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long rowId;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 客户简称
     */
    private String customerShortName;

    /**
     * 行业类型（机械加工、机械设备、家电....）
     */
    private String tradeType;

    /**
     * 所属省id
     */
    private Long provinceId;

    /**
     * 所属省名称
     */
    private String provinceName;

    /**
     * 所属市id
     */
    private Long cityId;

    /**
     * 所属市名称
     */
    private String cityName;

    /**
     * 所属区县id
     */
    private Long countyId;

    /**
     * 所属区县名称
     */
    private String countyName;

    /**
     * 所属街道id
     */
    private Long streetId;

    /**
     * 所属街道名称
     */
    private String streetName;

    /**
     * 详细地址
     */
    private String customerAddress;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 主营业务
     */
    private String mainBusiness;

    /**
     * 渠道分类（内部、外部）
     */
    private String channelType;

    /**
     * 渠道商编码
     */
    private String channelCode;

    /**
     * 渠道商名称
     */
    private String channelName;

    /**
     * 客户状态（预留）
     */
    private String customerStatus;

    /**
     * 在用标志（1在用；0停用）
     */
    private String activeFlag;

    /**
     * 授权类型（0-本地部署；1-pssa部署）
     */
    private String impowerType;

    /**
     * 数据库
     */
    private String databaseName;

    /**
     * 数据源
     */
    private String datasource;

    /**
     * 扩展字段1
     */
    private String ext1;

    /**
     * 扩展字段2
     */
    private String ext2;

    /**
     * 企业LOGO
     */
    private String logo;


}
