/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
//package com.cosmo.hhim.common.log.interceptor;
//
//import org.springframework.web.servlet.DispatcherServlet;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
///**
// * @author cosmo-hhim-open Team
// * @summary 自定义 DispatcherServlet 来分派 AxinHttpServletRequestWrapper
// */
//public class AxinDispatcherServlet extends DispatcherServlet {
//
//    /**
//     * 包装成我们自定义的request
//     * @param request
//     * @param response
//     * @throws Exception
//     */
//    @Override
//    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        super.doDispatch(new AxinHttpServletRequestWrapper(request), response);
//    }
//}
