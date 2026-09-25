/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.redis.service.RedisCacheCustomer;
import com.cosmo.hhim.micro.base.domain.entity.tips.MicroContentTipConfig;
import com.cosmo.hhim.micro.infrastructure.constant.RedisKeys;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.cosmo.hhim.micro.infrastructure.constant.ContentTipConstants.FIRST_ENTER_FLAG;
import static com.cosmo.hhim.micro.infrastructure.constant.RedisKeys.TODAY_FIRST_ENTER_PROGRAM;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
@Slf4j
public abstract class AbstractBusinessHandler implements IBusinessHandler {

    @Autowired
    private RedisCacheCustomer redisCacheCustomer;

    private static final String DATE_FORMAT = "yyyyMMdd";

    // 内容提示优先级 
    private int order;

    // 内容提示配置信息 
    private MicroContentTipConfig tipConfig;

    // 业务条件配置 
    private final Map<String, String> businessConditionConfigMap = Maps.newHashMap();

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public MicroContentTipConfig getTipConfig() {
        return tipConfig;
    }

    public void setTipConfig(MicroContentTipConfig tipConfig) {
        this.tipConfig = tipConfig;

        // 解析业务条件参数配置 -> Map
        if (null != tipConfig && StringUtils.hasText(tipConfig.getBusinessConditionConfig())) {
            List<String> businessConditionConfigs = Arrays.asList(tipConfig.getBusinessConditionConfig().split(","));
            for (String businessConditionConfig : businessConditionConfigs) {
                List<String> singleConfig = Arrays.asList(businessConditionConfig.split(":"));
                if (!CollectionUtils.isEmpty(singleConfig)) {
                    businessConditionConfigMap.put(singleConfig.get(0), singleConfig.get(1));
                }
            }
        }
    }

    public Map<String, String> getBusinessConditionConfigMap() {
        return businessConditionConfigMap;
    }

    /**
     * 模版方法控制是否调用下一个过滤器
     *
     * @param param
     * @param filterChain
     * @return
     */
    @Override
    public BusinessHandlerResult doHandler(BusinessHandlerParam param, IBusinessHandlerChain filterChain) {
        BusinessHandlerResult businessHandlerResult = executeHandler(param);

        String triggerBusinessBean = this.getClass().getSimpleName();
        if (!businessHandlerResult.isMatched()) { // 业务条件不满足则继续执行下一个业务条件满足性判断 
            log.info("3.内容提示>>> 不满足业务条件处理Bean：{} ，继续执行下一个业务条件...", triggerBusinessBean);
            BusinessHandlerResult result = filterChain.executeNextHandler(param);
            if (null != result && result.isMatched()) {
                return result;
            }
        } else {
            log.info("3.内容提示>>> 已满足业务条件处理Bean：{}", triggerBusinessBean);
            businessHandlerResult.setTipConfig(tipConfig);
            return businessHandlerResult;
        }

        return null;
    }


    /**
     * 判断指定角色是否是在某一天首次进入小程序
     *
     * @param param
     * @param nowDate
     * @param filterRole
     * @return
     */
    protected Boolean isFirstEnterProgramForWorkerRole(BusinessHandlerParam param, Date nowDate, String filterRole) {
        String nowDateFormat = DateUtil.format(nowDate, DATE_FORMAT);

        if (param.getRoleCodes().stream().anyMatch(e -> e.equals(filterRole))) {
            String redisKey = RedisKeys.MicroRegion.CONTENT_TIP_MODULE.value(TODAY_FIRST_ENTER_PROGRAM) + nowDateFormat + ":" + param.getUserId();
            if (null == redisCacheCustomer.getCacheObject(redisKey)) { // 当天首次进入小程序 
                redisCacheCustomer.setCacheObject(redisKey, FIRST_ENTER_FLAG, 1, TimeUnit.DAYS);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 执行业务
     *
     * @param param
     * @return
     */
    protected abstract BusinessHandlerResult executeHandler(BusinessHandlerParam param);
}
