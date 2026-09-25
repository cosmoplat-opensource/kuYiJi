/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.annotation;

import java.lang.annotation.*;

/**
 * 该注解用于两个实体对比字段值差异
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface ChangeField { 
    /**
     * 标注的注解名称
     * 如果默认为空,则会判断被注解的字段名是否相同
     * 如果不为空,则会判断name相同的字段的值
     * 例如两个实体均有itemCode字段,则直接标注@ChangeField即可,不需要填写name属性
     * 例如实体A有itemCode,要对应实体B的productCode,则需要标注@ChangeField(name = "xxx_code")来统一作为映射处理
     */
    String name() default "";
}
