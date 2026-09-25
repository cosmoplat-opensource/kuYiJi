/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-03
 */
@Data
public class MicroProcessBuyInfo {

    // 用户信息 
    private UserInfo data;

    // 数据源信息 
    private List<DataSourceInfo> mqDataSources;

    @Data
    public static class UserInfo {
        // 账号类型
        private String accountType;

        // 创建人
        private String createBy;

        // 创建时间
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date createTime;

        // 租户编码
        private String customerCode;

        // 租户名称
        private String customerName;

        // 应用编码
        private String appId;

        // 部门ID
        private Integer deptId;

        // 昵称
        private String nickName;

        // 手机号
        private String phonenumber;

        // 角色ID
        private String roleIds;

        // 用户ID
        private Integer userId;

        // 用户账号
        private String userName;

        // 用户有效期
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date validDate;

        // UUC用户ID
        private Long uucUserId;

        private Map<String, String> params;
    }


}
