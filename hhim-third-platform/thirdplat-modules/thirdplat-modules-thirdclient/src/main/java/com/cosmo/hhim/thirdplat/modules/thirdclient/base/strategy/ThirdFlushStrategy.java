/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.base.strategy;


import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdFlushEnum;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.flush.ThirdFlushAbstract;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 第三方系统的存储工厂
 *
 * @author cosmo-hhim-open Team
 */
public class ThirdFlushStrategy {

    private static final Map<String, ThirdFlushAbstract> services = new ConcurrentHashMap<>();

    /**
     * 获取第三方系统的存储执行器
     * 默认是mysql
     *
     * @param strategy 存储策略
     * @return HyzzThirdClient 请求执行器
     */
    public static ThirdFlushAbstract getThirdClient(String strategy) {
        ThirdFlushAbstract service = services.get(strategy);
        return service == null ? services.get(ThirdFlushEnum.HYZZ_COMMON_MYSQL.getStrategy()) : service;
    }

    public static void register(ThirdFlushEnum strategyEnum, ThirdFlushAbstract flushService) {
        services.put(strategyEnum.getStrategy(), flushService);
    }
}