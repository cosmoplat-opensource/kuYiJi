/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.datamask.cache;

import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description 价格权限ThreadLocal缓存
 * @createTime 2021/12/3
 */
public class PricePermissionCache {

    private static ThreadLocal<List<String>> userPricePermissionThreadLocal = new ThreadLocal<>();

    public static void saveCache(List<String> pricePermissions) {
        if (!CollectionUtils.isEmpty(pricePermissions)) {
            userPricePermissionThreadLocal.set(pricePermissions);
        }
    }

    public static List<String> getCache() {
        return userPricePermissionThreadLocal.get();
    }

    public static void clearCache() {
        userPricePermissionThreadLocal.remove();
    }

}
