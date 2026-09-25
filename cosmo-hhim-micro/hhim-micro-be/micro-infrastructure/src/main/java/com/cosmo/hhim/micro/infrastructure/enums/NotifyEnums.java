/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums;

import com.cosmo.hhim.common.core.converter.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/22
 */
public class NotifyEnums {

    /**
     * 业务标识
     */
    @Getter
    @AllArgsConstructor
    public enum NoticeBusinessSignEnum implements BasicEnum<String> { 

        PRODUCE_TODAY_REPORT("10", "生产日报"),
        PRODUCE_WEEK_REPORT("20", "生产周报");


        private String code;
        private String desc;

        public static NoticeBusinessSignEnum getEnum(String code) {
            return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
        }

        public static String getEnumDesc(String code) {
            NoticeBusinessSignEnum e = getEnum(code);
            return e != null ? e.desc : null;
        }
    }

    /**
     * 通知渠道
     */
    @Getter
    @AllArgsConstructor
    public enum NoticeChannelEnum implements BasicEnum<String> { 

        WECHAT_MP("10", "wechatMp", "微信公众号"),
        WECHAT_MINI_PROGRAM("20", "wechatMiniApp", "微信小程序"),
        SMS("30", "SMS", "短信");

        private String code;
        private String sign;
        private String desc;

        public static NoticeChannelEnum getEnum(String code) {
            return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
        }

        public static NoticeChannelEnum getEnumBySign(String sign) {
            return Arrays.stream(values()).filter(b -> Objects.equals(b.sign, sign)).findFirst().orElse(null);
        }

        public static String getEnumDesc(String code) {
            NoticeChannelEnum e = getEnum(code);
            return e != null ? e.desc : null;
        }
    }

//    /**
//     * 服务标识
//     */
//    public enum ServiceSign implements BasicEnum<String> {
//        MICRO_SERVICE("micro", "在制品库存微应用");
//
//        private String code;
//        private String desc;
//
//        ServiceSign(String code, String desc) {
//            this.code = code;
//            this.desc = desc;
//        }
//
//        public String getCode() {
//            return code;
//        }
//
//        public String getDesc() {
//            return desc;
//        }
//    }
//
//    /**
//     * 模板类型
//     */
//    public enum TemplateType implements BasicEnum<String> {
//        WECHAT_MINI_PROGRAM("0", "小程序"),
//        WECHAT_PUBLIC_PLATFORM("1", "公众号");
//
//        private String code;
//        private String desc;
//
//        TemplateType(String code, String desc) {
//            this.code = code;
//            this.desc = desc;
//        }
//
//        public String getCode() {
//            return code;
//        }
//
//        public String getDesc() {
//            return desc;
//        }
//    }
//
//    /**
//     * 消息类型
//     */
//    public enum MessageType {
//        TODAY_REPORT_NOTICE("0", "日报");
//
//        private String code;
//        private String desc;
//
//        MessageType(String code, String desc) {
//            this.code = code;
//            this.desc = desc;
//        }
//
//        public String getCode() {
//            return code;
//        }
//
    /**
     * 模板消息推送状态
     */
    public enum PushStatus { 
        WAITING_PUSH("0", "待推送"),
        FINISHED_PUSH("1", "已推送");

        private String code;
        private String desc;

        PushStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 模板消息推送结果
     */
    public enum PushResult { 
        FAILED_PUSH("0", "失败"),
        SUCCESSED_PUSH("1", "成功");

        private String code;
        private String desc;

        PushResult(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }


    /**
     * 微信模版消息内容key
     */
    public enum TodayReportMsgTemplateContentKey { 

        KEYWORD1("number1", "生产产品"),
        KEYWORD2("number2", "在产数量"),
        KEYWORD3("number3", "记工人数"),
        KEYWORD4("character_string4", "审核进度"),
        KEYWORD5("number5", "不良品数");

        private String key;
        private String desc;

        TodayReportMsgTemplateContentKey(String key, String desc) {
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
