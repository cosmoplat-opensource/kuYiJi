/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * apk版本管理对象 hyzz_apk_version
 *
 * @author cosmo-hhim-open Team
 * @date 2021-09-15
 */
@Data
public class HyzzApkVersion extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @JsonProperty("vId")
    private Long vId;

    /**
     * 版本号
     */
    @JsonProperty("vVersion")
    private String vVersion;

    /**
     * 类型
     */
    @JsonProperty("vType")
    private String vType;

    /**
     * APP描述
     */
    @JsonProperty("vDescribe")
    private String vDescribe;

    /**
     * 是否强制更新（Y：是，N:否）
     */
    @JsonProperty("vForceUpgrade")
    private String vForceUpgrade;

    /**
     * 上传者昵称
     */
    private String createByName;

    /**
     * apk地址
     */
    @JsonProperty("vUrl")
    private String vUrl;


    /**
     * apk大小
     */
    @JsonProperty("vSize")
    private String vSize;

    /**
     * APP类型标签（展示结果用）
     */
    @JsonProperty("vTypeLabel")
    private String vTypeLabel;

}
