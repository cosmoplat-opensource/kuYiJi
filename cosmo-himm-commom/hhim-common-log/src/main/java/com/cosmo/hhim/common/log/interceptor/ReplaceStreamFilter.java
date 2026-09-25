/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
//package com.cosmo.hhim.common.log.interceptor;
//
//
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import javax.xml.ws.RequestWrapper;
//import java.io.IOException;
//
///**
// * @author cosmo-hhim-open Team
// * @program wrapper-demo
// * @description 替换HttpServletRequest
// * @create 2018-12-24 21:04
// * @since 1.0
// **/
//public class ReplaceStreamFilter implements Filter {
//    protected final Logger log = LoggerFactory.getLogger(ReplaceStreamFilter.class);
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        log.info("StreamFilter初始化...");
//    }
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        ServletRequest requestWrapper = new AxinHttpServletRequestWrapper((HttpServletRequest) request);
//        chain.doFilter(requestWrapper, response);
//    }
//
//    @Override
//    public void destroy() {
//        log.info("StreamFilter销毁...");
//    }
//}
