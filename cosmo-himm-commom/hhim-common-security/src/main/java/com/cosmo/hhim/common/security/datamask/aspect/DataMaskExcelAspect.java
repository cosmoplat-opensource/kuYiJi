/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.datamask.aspect;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.security.datamask.cache.PricePermissionCache;
import com.cosmo.hhim.common.security.datamask.utils.DataMaskExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * @author cosmo-hhim-open Team
 * @description 数据脱敏Excel AOP切面
 * @createTime 2022/1/12
 */
@Slf4j
@Aspect
@Component
public class DataMaskExcelAspect {

    @Around("@annotation(com.cosmo.hhim.common.security.datamask.annotations.DataMaskExcelApi) || @within(com.cosmo.hhim.common.security.datamask.annotations.DataMaskExcelApi)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object obj = joinPoint.proceed();
        if (obj != null && !DataMaskExcelUtil.isPrimitive(obj.getClass())) {
            StopWatch stopWatch = new StopWatch();
            log.info("[DataMaskAspect][doAround] start==> responseData:{}", JSON.toJSONString(obj));
            stopWatch.start();

            try {
                //进行脱敏处理
                DataMaskExcelUtil.format(obj);
            } finally {
                PricePermissionCache.clearCache();
            }

            stopWatch.stop();
            log.info("[DataMaskAspect][doAround] end==> responseData:{}, useTime:{}s", JSON.toJSONString(obj), stopWatch.getTotalTimeSeconds());
        }
        return obj;
    }
}
