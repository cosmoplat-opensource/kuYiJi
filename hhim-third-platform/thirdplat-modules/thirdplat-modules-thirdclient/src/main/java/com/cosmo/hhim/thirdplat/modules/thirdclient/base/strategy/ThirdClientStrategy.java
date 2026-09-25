/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.base.strategy;

import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdClientEnum;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.client.ThirdClientAbstract;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 第三方系统的渠道工厂
 *
 * @author cosmo-hhim-open Team
 */
public class ThirdClientStrategy {

    private static final Map<String, ThirdClientAbstract> services = new ConcurrentHashMap<>();

    /**
     * 获取第三方系统的渠道执行器
     *
     * @param strategy 渠道策略
     * @return HyzzThirdClient 请求执行器
     */
    public static ThirdClientAbstract getThirdClient(String strategy) {
        return services.get(strategy);
    }

    public static void register(ThirdClientEnum strategyEnum, ThirdClientAbstract thirdClient) {
        services.put(strategyEnum.getStrategy(), thirdClient);
    }
}