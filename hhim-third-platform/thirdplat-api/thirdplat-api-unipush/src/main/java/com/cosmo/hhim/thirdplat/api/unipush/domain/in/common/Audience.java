/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description 推送目标用户设置
 *  推送目标用户，表示一条推送将要被推送到哪些用户列表。设置目标用户的方式：cid、别名(alias)，tag等等。
 * @createTime 2021-09-22
 */
@Data
public class Audience {
    // 根据cid选择推送目标用户时使用
    private List<String> cid;

    // 根据别名选择推送目标用户时使用，绑定别名请参考接口
    private List<String> alias;

    // 为用户自定义标签，根据fast_custom_tag选择推送用户时使用，绑定标签请参考接口
    @JSONField(name = "fast_custom_tag")
    private String fastCustomTag;

    // tag 指群推接口的用户筛选条件(包含多种类型条件：phone_type 手机类型; region 省市; custom_tag 用户标签)，详细格式见接口中参数说明
    private List<TagInfo> tag;

    @Data
    public static class TagInfo {
        /**
         * 查询条件(phone_type 手机类型; region 省市; custom_tag 用户标签; portrait，个推用户画像使用编码，点击下载文件portrait.data。
         * 设置用户标签(custom_tag)请见接口)
         */
        private String key;

        /**
         * 查询条件值列表，其中
         * 手机型号使用如下参数android和ios；
         * 省市使用编号，点击下载文件region_code.data；
         */
        private List<String> values;

        // or(或),and(与),not(非)，values间的交并补操作
        @JSONField(name = "opt_type")
        private String optType;
    }

}
