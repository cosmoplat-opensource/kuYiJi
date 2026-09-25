/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.intercept.anno;

import com.cosmo.hhim.micro.infrastructure.enums.TipTriggerActionEnum;

import java.lang.annotation.*;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ContentTip {

    /**
     * 触发动作集合
     *
     * @return
     */
    TipTriggerActionEnum[] tipTriggerActions();

}
