/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.constant.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @description 通用枚举常量
 * @createTime 2022-03-08
 */
public class CommonConstant {

    /**
     * 登录设备类型
     */
    public enum DeviceType {
        PC("pc", "PC端"),
        APP("mobile", "APP端"),
        WXMP("wechatMp", "微信公众号"),
        SCREEN("screen", "大屏"),
        PAD("pad", "pad"),
        WXPUSH("wechatPush", "微信推送"),
        WXMINIAPP("wechatMiniApp", "微信小程序");

        private String key;
        private String desc;

        DeviceType(String key, String desc) {
            this.key = key;
            this.desc = desc;
        }

        public String getKey() {
            return key;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 应用类型
     */
    public enum ApplicationSignEnum {
        MICRO_PROCESS("micro_process", "KU易记"),
        MICRO_PLAN("micro_plan", "工易派"),
        MICRO_MATERIAL("micro_material", "料易投");

        private String key;
        private String desc;

        ApplicationSignEnum(String key, String desc) {
            this.key = key;
            this.desc = desc;
        }

        public String getKey() {
            return key;
        }

        public String getDesc() {
            return desc;
        }

        public static ApplicationSignEnum getEnum(String key) {
            return Arrays.stream(values()).filter(b -> Objects.equals(b.key, key)).findFirst().orElse(null);
        }

        public static String getEnumDesc(String key) {
            ApplicationSignEnum e = getEnum(key);
            return e != null ? e.desc : null;
        }
    }


    /**
     * 小程序平台类型
     */
    public enum MiniAppPlatformTypeEnum {
        WECHAT("wechat", "微信小程序"),
        COSMO("cosmo", "卡奥斯小程序");

        private String key;
        private String desc;

        MiniAppPlatformTypeEnum(String key, String desc) {
            this.key = key;
            this.desc = desc;
        }

        public String getKey() {
            return key;
        }

        public String getDesc() {
            return desc;
        }
    }

}
