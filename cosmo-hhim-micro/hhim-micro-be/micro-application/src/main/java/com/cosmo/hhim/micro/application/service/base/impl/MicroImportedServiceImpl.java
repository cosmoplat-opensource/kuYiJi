/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.utils.DBControlUtil;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.handler.importexport.MicroImportedHandler;
import com.cosmo.hhim.micro.application.router.importexport.MicroImportedHandlerRouter;
import com.cosmo.hhim.micro.application.service.base.IMicroImportedService;
import com.cosmo.hhim.micro.base.domain.entity.CertificateInfoPC;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenant;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTenantService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.constant.RedisKeys;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.ImportTaskCodeEnums;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.cosmo.hhim.micro.base.domain.util.CertificateUtil.extractTaskCodeFromCertificate;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/6
 */
@Slf4j
@Service
@RequiredArgsConstructor
@RefreshScope
public class MicroImportedServiceImpl implements IMicroImportedService {

    private final IMicroTenantService microTenantService;
    private final RedisCache redisCache;
    private final MicroImportedHandlerRouter microImportedHandlerRouter;
    private final MicroSupportUtil microSupportUtil;

    @Value("${settings.importexport.importLink}")
    private String importLink;
    @Value("${settings.importexport.linkEffectiveTime}")
    private Integer linkEffectiveTime;

    /**
     * 生成导入临时链接
     * @return
     */
    @Override
    public String genImportLinkTemp(String taskType) {

        // 1.生成导入凭证码
        String uuId = UUID.randomUUID().toString().replaceAll("-", "");
        String redisKey = RedisKeys.MicroRegion.BASIC_MODULE.value(RedisKeys.IMPORT_CERTIFICATE_CODE + uuId);

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new CustomException("用户未登录或登录已过期，请重新登录");
        }

        String tenantCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        String appSign = SecurityUtils.getApplicationSign();
        log.info("genImportLinkTemp - userId: {}, tenantCode: {}, appSign: {}", userId, tenantCode, appSign);

        MicroUser userInfo = (MicroUser) microSupportUtil.getUserCache(userId.toString());
        if (userInfo == null) {
            log.error("genImportLinkTemp - user cache is null, userId: {}, tenantCode: {}, appSign: {}, redisKey: {}{}:{}:{}",
                    userId, tenantCode, appSign, CommonConstants.USER_CACHE_INFO, appSign, tenantCode, userId);
            throw new CustomException("用户信息获取失败，请重新登录");
        }

        CertificateInfoPC certificateInfoPC = new CertificateInfoPC();
        BeanUtils.copyProperties(userInfo, certificateInfoPC);
        certificateInfoPC.setTaskCode(ImportTaskCodeEnums.getEnumByKey(taskType).getCode());
        certificateInfoPC.setSysCode(SecurityUtils.getApplicationSign());
        redisCache.setCacheObject(redisKey, JSON.toJSONString(certificateInfoPC), linkEffectiveTime, TimeUnit.SECONDS);

        // 2.拼接临时导入链接
        return importLink + "?certificateCode=" + uuId + "&taskType=" + taskType;
    }

    /**
     * 下载导入模版
     *
     * @param response
     * @param certificate
     */
    @Override
    public void getImportTemplate(HttpServletResponse response, String certificate) {
        // 获取请求凭证详细信息
        CertificateInfoPC certificateInfo = getCertificateInfo(certificate);
        String tenantCode = certificateInfo.getTenantCode();
        String taskCode = certificateInfo.getTaskCode();

        setDataSource(certificateInfo.getId(), tenantCode);

        MicroImportedHandler<HttpServletResponse> handler = microImportedHandlerRouter.getHandler(taskCode + CommonConstants.IMPORT_TEMPLATE);

        // 执行处理器
        handler.doHandler(response);
    }

    /**
     * 确认导入
     * flag(0:错误数据修复，1:确认导入)
     *
     * @param flag
     */
    @Override
    public void importConfirm(String flag) {
        String taskCode = extractTaskCodeFromCertificate();
        if(!StringUtils.hasText(taskCode)){
            throw new CustomException("任务编码获取失败！");
        }

        MicroImportedHandler<String> handler = microImportedHandlerRouter.getHandler(taskCode + CommonConstants.IMPORT_CONFIRM);

        // 执行处理器
        handler.doHandler(flag);
    }

    /**
     * 取消导入
     */
    @Override
    public void importCancel() {
        String taskCode = extractTaskCodeFromCertificate();
        if(!StringUtils.hasText(taskCode)){
            throw new CustomException("任务编码获取失败！");
        }

        MicroImportedHandler handler = microImportedHandlerRouter.getHandler(taskCode + CommonConstants.IMPORT_CANCEL);

        // 执行处理器
        handler.doHandler(null);
    }

    /**
     * 获取凭证信息
     *
     * @param certificate
     * @return
     */
    private CertificateInfoPC getCertificateInfo(String certificate) {
        if (!StringUtils.hasText(certificate)) {
            log.warn("请求凭证不允许为空！");
            throw new CustomException("无请求凭证！");
        }

        String certificateKey = RedisKeys.MicroRegion.BASIC_MODULE.value(RedisKeys.IMPORT_CERTIFICATE_CODE + certificate);
        String userInfoStr = redisCache.getCacheObject(certificateKey);
        if (!StringUtils.hasText(userInfoStr)) {
            log.warn("请求凭证无效或已过期！certificate:{}", certificate);
            throw new CustomException("请求凭证无效或已过期！");
        }

        return JSON.parseObject(userInfoStr, CertificateInfoPC.class);
    }


    /**
     * 设置数据源信息
     *
     * @param userId
     * @param tenantCode
     */
    private void setDataSource(Long userId, String tenantCode) {
        MicroTenant tenant = microTenantService.getByCode(tenantCode);
        if (tenant == null) {
            log.error("通过租户编码查询本地租户信息失败！tenantCode:{}", tenantCode);
            throw new CustomException("请求凭证无效");
        }
        String customerName = tenant.getCustomerName();
        // 单库场景，datasource 固定 db0，schema 固定 im_micro
        DBControlUtil.setDbAndSchema("db0", "im_micro", tenantCode);
        ThreadContext.put(Constants.TARGET_CUSTOMER_NAME, customerName);
        ThreadContext.put(Constants.TRACEID, UUID.randomUUID().toString());
        ThreadContext.put(CacheConstants.DETAILS_USER_ID, userId);
    }
}
