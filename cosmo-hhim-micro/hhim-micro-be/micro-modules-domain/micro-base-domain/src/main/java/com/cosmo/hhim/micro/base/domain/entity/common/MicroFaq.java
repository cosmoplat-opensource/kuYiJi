/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;

/**
 * FAQ 对象 micro_faq
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroFaq implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 分类
     */
    private String category;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 1=启用 0=停用
     */
    private Integer status;
}
