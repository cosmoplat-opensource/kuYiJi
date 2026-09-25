/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cosmo-hhim-open Team
 * 字段对比差异实体
 */
@Data
public class MicroChangeEntity implements Serializable {
    /**
     * 变更的字段名称
     */
    private String fieldName;
    /**
     * 变更的字段@ChangeField注解name值
     */
    private String fieldDesc;
    /**
     * 旧值
     */
    private Object oldValue;
    /**
     * 新值
     */
    private Object newValue;
}
