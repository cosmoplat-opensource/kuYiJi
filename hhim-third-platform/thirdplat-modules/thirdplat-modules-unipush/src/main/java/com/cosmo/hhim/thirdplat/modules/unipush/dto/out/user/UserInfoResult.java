/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.dto.out.user;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description 用户信息
 * @createTime 2021-09-23
 */
@Data
public class UserInfoResult {

    // 用户信息列表
    private Map<String, UserInfo> validCids;

    // 无效cid列表
    private List<String> invalidCids;

    @Data
    public static class UserInfo{
        // 应用id
        @JSONField(name = "client_app_id")
        private String clientAppId;

        // 包名
        @JSONField(name = "package_name")
        private String packageName;

        // 厂商token
        @JSONField(name = "device_token")
        private String deviceToken;

        // 手机系统 1-安卓 2-ios
        @JSONField(name = "phone_type")
        private Integer phoneType;

        // 机型
        private String phoneModel;

        // 系统通知栏开关
        private boolean notificationSwitch;

        // 首次登录时间
        private String createTime;

        // 登录频次
        private Integer loginFreq;
    }
}
