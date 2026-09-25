/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * 客户（企业）对象 hyzz_customer
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzCustomer extends BaseEntity{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long rowId;

    /** 客户编码 */
    private String customerCode;

    /** 客户名称 */
    private String customerName;

    /** 客户简称 */
    private String customerShortName;

    /** 行业类型（机械加工、机械设备、家电....） */
    private String tradeType;

    /** 所属省id */
    private Long provinceId;

    /** 所属省名称 */
    private String provinceName;

    /** 所属市id */
    private Long cityId;

    /** 所属市名称 */
    private String cityName;

    /** 所属区县id */
    private Long countyId;

    /** 所属区县名称 */
    private String countyName;

    /** 所属街道id */
    private Long streetId;

    /** 所属街道名称 */
    private String streetName;

    /** 详细地址 */
    private String customerAddress;

    /** 联系人 */
    private String contactName;

    /** 联系人电话 */
    private String contactPhone;

    /** 主营业务 */
    private String mainBusiness;

    /** 渠道分类（内部、外部） */
    private String channelType;

    /** 渠道商编码 */
    private String channelCode;

    /** 渠道商名称 */
    private String channelName;

    /** 客户状态（预留） */
    private String customerStatus;

    /** 在用标志（1在用；0停用） */
    private String activeFlag;

    /** 授权类型（0-本地部署；1-pssa部署） */
    private String impowerType;

    /** 数据库 */
    private String databaseName;

    /** 数据源 */
    private String datasource;

    /** 扩展字段1 */
    private String ext1;

    /** 扩展字段2 */
    private String ext2;

    /**
     * 客户搜索
     */
    private String  customerQuery;

    /**
     * 验证重复
     */
    private Long validRowId;

    /** 用户账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 密码 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JSONField(serialize = false)
    private transient String password;
    private HyzzCustomerUser user;

    private HyzzInvoiceInfo invoice;

    private String logo;


    /** 是否下发（Y/N） */
    private String sendFlag;

    /** 下发时间 */
    private Date sendTime;

    /** 下发人 */
    private String sender;

    /** 规模 */
    private String scope;

    /** 授权用户数 */
    private Integer authUserNum;

    /** 销售经理 */
    private String salesManager;

    /** 销售电话 */
    private String salesPhone;

    private String key;

    private String secret;

    /**
     * 用户有效期
     */
    private Date userValidDate;

    /**
     * 建库状态0：未建库，1:建库中，2：建库成功，3：建库失败
     */
    private Integer createDbState;

    /**
     * 建库时间
     */
    private Date createDbTime;

    /**
     * 建库人
     */
    private String createDbBy;

    /**
     * 建库失败原因
     */
    private String createDbeException;

    /**
     * 所属版本ID
     */
    private Long vId;

    private int useState;

    private String playUser;

    private String accountType;

    // 用户手机号
    private String phoneNumber;

    //用户是否删除
    private String delFlag;

    //客户来源
    private String customerSource;

    // 客户产品类型(0:MOM,1:微应用)
    private String customerProductType;

    // 天云cubaId
    private String cubaId;

    // 应用使用状态(0:试用，1：正式使用)
    private String useStatus;

    /**
     * 邀请码
     */
    private String inviteCode;


}
