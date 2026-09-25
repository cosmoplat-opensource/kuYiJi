/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.accessauth.filter;

import com.cosmo.hhim.common.security.pojo.LoginUser;

import javax.servlet.http.HttpServletRequest;

/**
 * @author cosmo-hhim-open Team
 * @description 客制化请求访问控制条件
 * @createTime 2022-11-07
 */
public interface CustomizedAccessAuthFilterCondition {


    /**
     * 是否跳过访问权限拦截Filter
     *
     * @param request
     * @return true：跳过，false：不跳过
     */
    default boolean isSkipRequestFilter(HttpServletRequest request){
        return true;
    }

    /**
     * 获取登录用户信息
     * @param request
     * @return
     */
    LoginUser getLoginUser(HttpServletRequest request);

}
