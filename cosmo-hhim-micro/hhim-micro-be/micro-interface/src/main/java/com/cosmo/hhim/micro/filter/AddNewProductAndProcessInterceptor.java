/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
//package com.cosmo.hhim.micro.filter;
//
//import cn.hutool.core.collection.CollectionUtil;
//import com.alibaba.fastjson.JSONObject;
//import com.cosmo.hhim.common.core.exception.CustomException;
//import com.cosmo.hhim.common.core.utils.StringUtils;
//import com.cosmo.hhim.common.security.utils.SecurityUtils;
//import com.cosmo.hhim.micro.application.dto.submit.BatchSubmitOrQcDto;
//import com.cosmo.hhim.micro.application.dto.submit.MicroWorkSubmitMultiMixedDTO;
//import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
//import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenantIndividuationConfig;
//import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
//import com.cosmo.hhim.micro.base.domain.service.common.IMicroTenantIndividuationConfigService;
//import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
//import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
//import com.cosmo.hhim.micro.infrastructure.util.RequestUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.List;
//
///**
// * @author cosmo-hhim-open Team
// * @description:
// *  1. 新添加产品和工序的拦截器, 拦截的接口请求: 新增产品、新增工序、 报工、编辑报工、质检和批量报工（质检）
// *  2. 前提条件: （1）租户配置了限制新增产品或者工序 (2) 请求人是工人角色
// * @date 2023/5/31 10:19
// */
//@Slf4j
//@Component
//public class AddNewProductAndProcessInterceptor extends HandlerInterceptorAdapter {
//
//    @Autowired
//    private IMicroTenantIndividuationConfigService microTenantIndividuationConfigService;
//    @Autowired
//    private IMicroUserService microUserService;
//
//    /**
//     * 用于区分是正常还是批量
//     */
//    private static final String SUBMIT_WAY = "submitWay";
//
//
//    /**
//     * 新增产品url
//     */
//    private static final String ADD_PRODUCT_URL = "/product/add";
//
//    /**
//     * 新增工序url
//     */
//    private static final String ADD_PROCESS_URL = "/process/add";
//
//    /**
//     * 在业务处理器处理请求之前被调用
//     */
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//
//        // 1. 查询请求人的角色,现阶段一个人就一个角色
//        List<MicroRole> microRoles = microUserService.selectMicroRolesByUserId(SecurityUtils.getUserId());
//        if (CollectionUtil.isEmpty(microRoles)) {
//            throw new CustomException("请求人暂无角色");
//        }
//        String roleCode = microRoles.get(0).getRoleCode();
//
//        // 2. 查询是否配置了限制工人新增产品或者工序
//        MicroTenantIndividuationConfig microTenantIndividuationConfig = microTenantIndividuationConfigService.selectMicroTenantIndividuationConfigByTenant();
//        if (microTenantIndividuationConfig == null) {
//            throw new CustomException("没有进行相关配置");
//        }
//
//        // 3. 员工且配置了限制则进入
//        String able = "0";
//        if (able.equals(microTenantIndividuationConfig.getWorkerBaseDataConfine()) && MicroBusinessConstants.WORKER_ROLE.equals(roleCode)) {
//            // 3.1 单独判断新增工序或者新增产品的接口
//            String url = request.getServletPath();
//            // 不允许调用新增产品或者工序的接口
//            if (url.equals(ADD_PRODUCT_URL) || url.equals(ADD_PROCESS_URL)) {
//                throw new CustomException("已开启限制报工或者编辑报工时新增产品或者工序限制，不可以再新增产品或者工序");
//            }
//
//            // 3.2 根据参数判断报工、编辑报工、质检和批量报工（质检）的接口
//            String requestBody = this.getRequestBody();
//            JSONObject jsonObject = JSONObject.parseObject(requestBody);
//            // 判断不同接口的传递参数
//            if (jsonObject.get(SUBMIT_WAY) != null) {
//                BatchSubmitOrQcDto batchSubmitOrQcDto = JSONObject.parseObject(requestBody, BatchSubmitOrQcDto.class);
//                // 传递的报工参数
//                List<MicroWorkSubmitMultiMixedDTO> multiMixedList = batchSubmitOrQcDto.getMultiMixedList();
//                if (CollectionUtil.isEmpty(multiMixedList)) {
//                    throw new CustomException("批量报工数据不能为空");
//                }
//                multiMixedList.forEach(obj -> {
//                    boolean batchCondition = StringUtils.isEmpty(obj.getProductSeq()) ||
//                            StringUtils.isEmpty(obj.getOperateProcessSeq()) ||
//                            (StringUtils.isNotEmpty(obj.getPreProcessName()) && StringUtils.isEmpty(obj.getPreProcessSeq()));
//                    if (batchCondition) {
//                        throw new CustomException("已开启限制报工或者编辑报工时新增产品或者工序限制，不可以再新增产品或者工序");
//                    }
//                });
//            } else {
//                MicroWorkSubmitDto workSubmitDto = JSONObject.parseObject(requestBody, MicroWorkSubmitDto.class);
//                // 判断ids是否为null，是为了区分单个送检时是报工送检还是审核送检（审核送检只会传递ids）
//                boolean condition = StringUtils.isEmpty(workSubmitDto.getIds()) &&
//                        (StringUtils.isEmpty(workSubmitDto.getProductSeq()) ||
//                                StringUtils.isEmpty(workSubmitDto.getOperateProcessSeq()) ||
//                                (StringUtils.isNotEmpty(workSubmitDto.getPreProcessName()) && StringUtils.isEmpty(workSubmitDto.getPreProcessSeq())));
//                if (condition) {
//                    throw new CustomException("已开启限制报工或者编辑报工时新增产品或者工序限制，不可以再新增产品或者工序");
//                }
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 获取请求体
//     *
//     * @return
//     */
//    public String getRequestBody() throws IOException {
//        String requestBody = RequestUtils.getRequestBody();
//        return requestBody;
//    }
//}
