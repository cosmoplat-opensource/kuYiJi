/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
//package com.cosmo.hhim.micro.config;
//
//import com.cosmo.hhim.micro.filter.AddNewProductAndProcessInterceptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.util.List;
//
///**
// * @author cosmo-hhim-open Team
// * @description: 添加新产品和新工序拦截器配置
// * @date 2023/5/31 10:16
// */
//@Configuration
//public class AddNewProductAndProcessInterceptorConfig implements WebMvcConfigurer {
//
//    @Value("#{'${newAddProductAndProcess.includedPaths}'.split(',')}")
//    private List<String> includedPaths;
//
//    @Autowired
//    private AddNewProductAndProcessInterceptor addNewProductAndProcessInterceptor;
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(addNewProductAndProcessInterceptor)
//                .addPathPatterns(includedPaths)
//                .order(100);
//    }
//}
