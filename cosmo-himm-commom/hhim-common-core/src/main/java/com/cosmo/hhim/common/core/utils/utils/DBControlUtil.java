/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils.utils;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;


/**
 * @description:
 * @params:
 */
public class DBControlUtil {

    /**
     * 一次性更改数据源（一次查询后回归默认）
     *
     * @param db
     * @param schema
     */
    public  static void setDbAndSchema(String db, String schema, String customerCode) {
        ThreadContext.put(Constants.TARGET_DS, db);
        ThreadContext.put(Constants.TARGET_SCHEMA, schema);
        ThreadContext.put(Constants.TARGET_CUSTOMER, customerCode);

    }

    /**
     * 一次性更改数据源（一次查询后回归默认）
     *
     * @param db
     * @param schema
     */
    public static void setDbAndSchema(
            String db,
            String schema,
            String customerCode,
            String userName,
            Long userId,
            String authorization
    ) {

        ThreadContext.put(Constants.TARGET_DS, db);
        ThreadContext.put(Constants.TARGET_SCHEMA, schema);
        ThreadContext.put(CacheConstants.AUTHORIZATION_HEADER, authorization);
        ThreadContext.put(Constants.TARGET_CUSTOMER, customerCode);
        ThreadContext.put(CacheConstants.DETAILS_USERNAME, userName);
        ThreadContext.put(CacheConstants.DETAILS_USER_ID, userId);

    }


    public static void setDbAndSchema(
            String db,
            String schema,
            String customerCode,
            String userName,
            Long userId,
            String authorization,
            String nickname) {

        ThreadContext.put(Constants.TARGET_DS, db);
        ThreadContext.put(Constants.TARGET_SCHEMA, schema);
        ThreadContext.put(CacheConstants.AUTHORIZATION_HEADER, authorization);
        try{
            if(StringUtils.isNotEmpty(nickname)){
                ThreadContext.put(CacheConstants.NICK_NAME, URLDecoder.decode(nickname,"UTF-8"));
            }
        }catch (Exception e){

        }
        ThreadContext.put(Constants.TARGET_CUSTOMER, customerCode);
        ThreadContext.put(CacheConstants.DETAILS_USERNAME, userName);
        ThreadContext.put(CacheConstants.DETAILS_USER_ID, userId);

    }

    public static void setDbAndSchema(String db, String schema, String customerCode, String username) {
        ThreadContext.put(Constants.TARGET_DS, db);
        ThreadContext.put(Constants.TARGET_SCHEMA, schema);
        ThreadContext.put(CacheConstants.DETAILS_USERNAME, username);
        ThreadContext.put(Constants.TARGET_CUSTOMER, customerCode);
    }

}
