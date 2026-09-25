/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/4/26
 */
public class ProductNameChangeEvent extends ApplicationEvent {

    public ProductNameChangeEvent(Object source) {
        super(source);
    }
}
