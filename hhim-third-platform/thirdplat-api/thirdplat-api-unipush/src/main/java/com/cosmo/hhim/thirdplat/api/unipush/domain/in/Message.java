/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-28
 */
@Data
public class Message {

    /**
     * 通知标题
     * 长度<=32
     */
    @NotBlank(message = "通知标题不允许为空")
    @Length(max = 32)
    private String title;

    /**
     * 消息通知内容
     * 长度<=50
     */
    @NotBlank(message = "消息通知内容不允许为空")
    @Length(max = 50)
    private String content;

    /**
     * 消息承载的数据
     */
    private Payload payload;

    @Data
    public static class Payload {
        /**
         * 承载数据类型（1：链接消息，2：文本消息，3：图文消息）
         */
        private Integer dataType;
        /**
         * 承载数据
         */
        private String data;
    }
}
