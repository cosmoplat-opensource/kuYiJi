/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.cosmo.hhim.common.core.annotation.Excel;
import lombok.Data;

import java.util.List;

/**
 * 大屏配置对象 hyzz_apps_large_screen
 * 
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzAppsLargeScreen extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 应用编码 */
    @Excel(name = "应用编码")
    private String appsCode;

    /** 应用名称 */
    @Excel(name = "应用名称")
    private String appsName;

    /** 大屏名称 */
    @Excel(name = "大屏名称")
    private String screenName;

    /** 缩略图 */
    @Excel(name = "缩略图")
    private String screenImage;

    /** 大屏地址 */
    @Excel(name = "大屏地址")
    private String screenPath;

    /** 参数 */
    @Excel(name = "参数")
    private String parameter;

    /** 排序 */
    @Excel(name = "排序")
    private Long sort;

    /** 在用标志（1在用；0停用） */
    @Excel(name = "在用标志", readConverterExp = "1=在用；0停用")
    private String activeFlag;

    /** 更新者名称 */
    @Excel(name = "更新者名称")
    private String updateByName;

    /**
     *状态（0显示 1隐藏）
     */
    private String visible;

    private List<String> appsCodes;
}
