/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.ios;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-24
 */
@Data
public class Alert {
    /**
     * 通知消息标题
     */
    private String title;
    /**
     * 通知消息内容
     */
    private String body;
    /**
     * (用于多语言支持)指定执行按钮所使用的Localizable.strings
     */
    @JSONField(name = "action-loc-key")
    private String actionLocKey;
    /**
     * (用于多语言支持)指定Localizable.strings文件中相应的key
     */
    @JSONField(name = "loc-key")
    private String locKey;
    /**
     * 如果loc-key中使用了占位符，则在loc-args中指定各参数
     */
    @JSONField(name = "loc-args")
    private List<String> locArgs;
    /**
     * 指定启动界面图片名
     */
    @JSONField(name = "launch-image")
    private String launchImage;
    /**
     * (用于多语言支持）对于标题指定执行按钮所使用的Localizable.strings,仅支持iOS8.2以上版本
     */
    @JSONField(name = "title-loc-key")
    private String titleLocKey;
    /**
     * 对于标题,如果loc-key中使用的占位符，则在loc-args中指定各参数,仅支持iOS8.2以上版本
     */
    @JSONField(name = "title-loc-args")
    private List<String> titleLocArgs;
    /**
     * 通知子标题,仅支持iOS8.2以上版本
     */
    private String subtitle;
    /**
     * 当前本地化文件中的子标题字符串的关键字,仅支持iOS8.2以上版本
     */
    @JSONField(name = "subtitle-loc-key")
    private String subtitleLocKey;
    /**
     * 当前本地化子标题内容中需要置换的变量参数 ,仅支持iOS8.2以上版本
     */
    @JSONField(name = "subtitle-loc-args")
    private List<String> subtitleLocArgs;
}
