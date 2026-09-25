/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.filter;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.utils.DBControlUtil;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.micro.base.domain.entity.CertificateInfoPC;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.constant.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

/**
 * decouple-from-ops-platform：
 *  - 不再调 im-api-operation / RemoteCustomerService
 *  - token 走 micro_v2: 前缀，老 token 自然失效
 *  - schema/datasource 从登录态缓存直接读，单库场景下为 im_micro/db0
 *
 * @author cosmo-hhim-open Team
 * @createTime 2023/4/14
 */
@Slf4j
@RefreshScope
@Component
public class CertificateInterceptor extends HandlerInterceptorAdapter {

    private static final String HYPHEN = "-";
    private static final String UNDERSCORE = "_";

    @Lazy
    @Value("#{'${certificate.excludedPaths}'.split(',')}")
    private List<String> excludedPaths;

    @Lazy
    @Autowired
    private RedisCache redisCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跳过指定请求
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (excludedPaths.contains(path)) {
            return true;
        }

        String certificate = request.getHeader(CommonConstants.CERTIFICATE);
        if (!StringUtils.hasText(certificate)) {
            log.warn("请求凭证不允许为空！");
            throw new CustomException("请求凭证不允许为空!");
        }

        // 根据请求凭证换取用户信息（micro_v2: 前缀）
        CertificateInfoPC certificateInfo = getCertificateInfo(certificate);
        String tenantCode = certificateInfo.getTenantCode();

        // 设置数据源等信息（不再调 Feign）
        setDataSource(certificateInfo.getId(), tenantCode,
                certificateInfo.getSchema(), certificateInfo.getDatasource());

        ThreadContext.put(CommonConstants.CERTIFICATE, certificate);
        ThreadContext.put(CommonConstants.CERTIFICATE_INFO, JSON.toJSONString(certificateInfo));

        return true;
    }


    /**
     * 通过凭证获取用户信息（micro_v2: 前缀）
     */
    private CertificateInfoPC getCertificateInfo(String certificate) {
        if (!StringUtils.hasText(certificate)) {
            log.warn("请求凭证不允许为空！");
            throw new CustomException("无请求凭证！");
        }

        // 新前缀 micro_v2:login: ；老前缀 micro:basic:importCertificateCode: 不再读
        String certificateKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(RedisKeys.CERTIFICATE_TOKEN + certificate);
        String userInfoStr = redisCache.getCacheObject(certificateKey);
        if (!StringUtils.hasText(userInfoStr)) {
            log.warn("请求凭证无效或已过期！certificate:{}", certificate);
            throw new CustomException("请求凭证无效或已过期！");
        }

        return JSON.parseObject(userInfoStr, CertificateInfoPC.class);
    }


    /**
     * 设置数据源信息（不再调 im-api-operation）
     */
    private void setDataSource(Long userId, String tenantCode, String schema, String datasource) {
        // 单库场景：schema/datasource 直接从登录态取，登录时已锁定
        // 若老登录态未带此字段（来自老 RemoteCustomerService 写入）则用兜底
        if (!StringUtils.hasText(schema)) {
            schema = "im_micro";
        }
        if (!StringUtils.hasText(datasource)) {
            datasource = "db0";
        }
        // decouple-from-ops-platform-cleanup hotfix: 老 im-api-operation 返回的 schema 是 "im-micro"（hyphen），
        // 实际 MySQL schema 是 "im_micro"（underscore）。做归一化避免 SQL 报 Table 'im-micro.xxx' doesn't exist
        if (schema != null && schema.contains(HYPHEN)) {
            String normalized = schema.replace(HYPHEN, UNDERSCORE);
            log.debug("[schema normalize] {} -> {}", schema, normalized);
            schema = normalized;
        }
        DBControlUtil.setDbAndSchema(datasource, schema, tenantCode);
        ThreadContext.put(Constants.TRACEID, UUID.randomUUID().toString());
        ThreadContext.put(CacheConstants.DETAILS_USER_ID, userId);
    }
}
