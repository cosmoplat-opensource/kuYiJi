/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.intercept.advisor;

import com.cosmo.hhim.common.core.converter.BasicEnum;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.redis.distributedlock.utils.RedisLockHelper;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.tips.MicroContentTipExecuteParam;
import com.cosmo.hhim.micro.base.domain.entity.tips.TipResult;
import com.cosmo.hhim.micro.base.domain.tips.TipsManager;
import com.cosmo.hhim.micro.base.domain.tips.intercept.anno.ContentTip;
import com.cosmo.hhim.micro.infrastructure.constant.RedisKeys;
import com.cosmo.hhim.micro.infrastructure.enums.TipTriggerActionEnum;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
@Slf4j
public class ContentTipAnnotationInterceptor implements MethodInterceptor {

    private static final String TIP_KEY = "tip";
    private static final String TIP_WAY = "tipWay";

    private final TipsManager tipsManager;
    private final RedisLockHelper redisLockHelper;

    public ContentTipAnnotationInterceptor(TipsManager tipsManager, RedisLockHelper redisLockHelper) {
        this.tipsManager = tipsManager;
        this.redisLockHelper = redisLockHelper;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        // 1.执行原始方法
        Object result = methodInvocation.proceed();

        if (!(result instanceof AjaxResult)) {
            return result;
        }

        // 2.解析所拦截方法上的注解信息
        Class<?> targetClass = (methodInvocation.getThis() != null ? AopUtils.getTargetClass(methodInvocation.getThis()) : null);
        // 拿到最终要执行的方法
        Method specificMethod = ClassUtils.getMostSpecificMethod(methodInvocation.getMethod(), targetClass);
        // 桥接方法
        final Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

        // 获取最终执行方法上的ContentTip注解属性信息
        ContentTip contentTip = AnnotatedElementUtils.findMergedAnnotation(userDeclaredMethod, ContentTip.class);
        if (contentTip == null) {
            contentTip = AnnotatedElementUtils.findMergedAnnotation(userDeclaredMethod.getDeclaringClass(), ContentTip.class);
        }

        List<String> triggerActionCodes = Arrays.stream(contentTip.tipTriggerActions()).map(TipTriggerActionEnum::getCode).collect(Collectors.toList());

        // 解析请求参数中的动作类型
        Object[] arguments = methodInvocation.getArguments();
        Object actionType = Arrays.stream(arguments).filter(e -> e instanceof BasicEnum).map(e -> ((BasicEnum<?>) e).getCode()).findFirst().orElse(null);

        // 3.执行提示动作
        MicroContentTipExecuteParam executeParam = new MicroContentTipExecuteParam();
        executeParam.setTriggerActions(triggerActionCodes);
        if (null != actionType) {
            executeParam.setActionType(actionType.toString());
        }

        TipResult tipResult = null;
        String lockKey = RedisKeys.MicroRegion.CONTENT_TIP_MODULE.value(RedisKeys.LOCK) + SecurityUtils.getUserId();
        String uuid = UUID.randomUUID().toString();
        try {
            final boolean success = redisLockHelper.lock(lockKey, uuid, 40L, TimeUnit.SECONDS);
            if (!success) {
                // 加锁失败，不进行内容提示，直接返回
                return result;
            }
            tipResult = tipsManager.executeTip(executeParam);
        } finally {
            // 业务处理完成后不再主动释放锁
            redisLockHelper.unlock(lockKey, uuid);
        }

        // 4.包装返回对象
        AjaxResult resultWrapper = (AjaxResult) result;
        if (null != tipResult && StringUtils.hasText(tipResult.getTipContent())) {
            resultWrapper.put(TIP_KEY, tipResult.getTipContent());
            resultWrapper.put(TIP_WAY, tipResult.getTipWay());
        }
        return resultWrapper;
    }
}
