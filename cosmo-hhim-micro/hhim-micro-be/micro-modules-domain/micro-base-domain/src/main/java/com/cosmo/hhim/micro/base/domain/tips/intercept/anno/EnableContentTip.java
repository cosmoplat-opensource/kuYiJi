/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.intercept.anno;

import com.cosmo.hhim.micro.base.domain.tips.intercept.ContentTipConfigurationSelector;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ContentTipConfigurationSelector.class)
public @interface EnableContentTip {

    // 自定义内容提示的注解类型
    Class<? extends Annotation> annotation() default Annotation.class;

    // 切面通知模式：默认动态代理PROXY
    AdviceMode mode() default AdviceMode.PROXY;

}
