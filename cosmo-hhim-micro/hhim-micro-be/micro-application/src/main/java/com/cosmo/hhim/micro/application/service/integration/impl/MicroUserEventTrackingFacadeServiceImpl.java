/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.integration.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.ip.IpUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.integration.MicroUserEventTrackingDTO;
import com.cosmo.hhim.micro.application.service.integration.IMicroUserEventTrackingFacadeService;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserEventTrackingEntity;
import com.cosmo.hhim.micro.integration.domain.service.IMicroUserEventTrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户行为事件埋点服务
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroUserEventTrackingFacadeServiceImpl implements IMicroUserEventTrackingFacadeService {

    @Autowired
    private IMicroUserEventTrackingService trackingService;

    /**
     * 收集用户行为埋点
     *
     * @param trackingDTO
     * @param request
     */
    @Override
    public int collectUserEvent(MicroUserEventTrackingDTO trackingDTO, HttpServletRequest request) {
        MicroUserEventTrackingEntity trackingEntity = BeanUtil.copyProperties(trackingDTO, MicroUserEventTrackingEntity.class);
        this.setTrackingExternalProperties(trackingEntity, request);
        try {
            trackingService.insertMicroUserEventTracking(trackingEntity);
        } catch (Exception e) {
            log.warn("采集用户行为埋点事件失败:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            return 1;
        }
        return 1;
    }

    /**
     * 设置额外属性
     *
     * @param trackingEntity
     * @param request
     */
    private void setTrackingExternalProperties(MicroUserEventTrackingEntity trackingEntity, HttpServletRequest request) {
        String userAgentHeader = request.getHeader(Header.USER_AGENT.toString());
        String uaStr = "UNKNOWN";
        if (!StringUtils.isEmpty(userAgentHeader)) {
            try {
                UserAgent ua = UserAgentUtil.parse(userAgentHeader);
                uaStr = ua.getPlatform().getName();
            } catch (Exception e) {
                log.warn("解析user-agent失败;{}", userAgentHeader);
            }
        }
        trackingEntity.setUserAgent(uaStr);
        String customerCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        String customerName = (String) ThreadContext.get(Constants.TARGET_CUSTOMER_NAME);
        trackingEntity.setTenantCode(customerCode);
        trackingEntity.setTenantName(customerName);
        long today = Long.parseLong(DateUtil.today().replace("-", ""));
        trackingEntity.setCreatedDay(today);
        int thisMonth = DateUtil.thisMonth() + 1;
        String monthStr = thisMonth < 10 ? ("0" + thisMonth) : String.valueOf(thisMonth);
        trackingEntity.setCreatedMonth(Long.parseLong(DateUtil.thisYear() + monthStr));
        trackingEntity.setUserIp(IpUtils.getIpAddr(request));
        trackingEntity.setApplicationSign(SecurityUtils.getApplicationSign());
    }

}
