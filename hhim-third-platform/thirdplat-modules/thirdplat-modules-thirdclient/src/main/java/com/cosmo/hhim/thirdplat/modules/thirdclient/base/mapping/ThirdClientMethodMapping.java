/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.base.mapping;

import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdClientEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * 海云智造提供的基础方法与第三方开放平台/系统的映射关系
 */
public class ThirdClientMethodMapping {


    private static final Map<String, String> METHOD_MAPPING = new HashMap<>();

    /**
     * 海云智造提供的基础方法与第三方开放平台/系统的映射关系
     * key:method&&version&&client_support
     * value:client_plat method(第三方平台/系统的方法)
     *
     * @param hyzzMethod        海云智造提供的方法名
     * @param hyzzMethodVersion 海云智造提供的方法版本
     * @param clientSupport     客户系统/平台
     * @param clientMethod      客户系统对应的请求方法
     */
    public static void setClientMethodMapping(String hyzzMethod, String hyzzMethodVersion, ThirdClientEnum clientSupport, String clientMethod) {
        METHOD_MAPPING.put(getJoinKey(hyzzMethod, hyzzMethodVersion, clientSupport), clientMethod);
    }

    public static String getClientMethod(String hyzzMethod, String hyzzMethodVersion, ThirdClientEnum clientSupport) {
        String clientMethod = METHOD_MAPPING.get(getJoinKey(hyzzMethod, hyzzMethodVersion, clientSupport));
        //这里无法获取对应方法,直接返回原方法名
        if (clientMethod == null) {
            clientMethod = hyzzMethod.startsWith("/") ? hyzzMethod : "/" + hyzzMethod;
        }
        return clientMethod;
    }

    private static String getJoinKey(String hyzzMethod, String hyzzMethodVersion, ThirdClientEnum clientSupport) {
        return String.join("&&", hyzzMethod, hyzzMethodVersion, clientSupport.getStrategy());
    }
}
