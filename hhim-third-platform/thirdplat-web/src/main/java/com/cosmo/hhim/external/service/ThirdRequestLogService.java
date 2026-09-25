/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.service;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.third.ThirdInterfaceTenant;
import com.cosmo.hhim.common.core.utils.bean.BeanUtils;
import com.cosmo.hhim.external.mapper.HyzzThirdInterfaceLogMapper;
import com.cosmo.hhim.thirdplat.api.thirdclient.common.ThirdParamConstant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceLog;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceLogDetail;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class ThirdRequestLogService {

    @Resource
    private HyzzThirdInterfaceLogMapper thirdInterfaceLogMapper;

    /**
     * 存储非错误请求类日志, 只存入log表 没有detail表
     *
     * @param thirdInterfaceLog 日志实体
     */
    public void saveNonRequestErrorLog(HyzzThirdInterfaceLog thirdInterfaceLog) {
        flushDb(thirdInterfaceLog);
    }

    /**
     * 存储错误请求类日志, 同时存入log和detail表
     *
     * @param thirdInterfaceLog 日志实体
     * @param res               请求返回体 要记录到detail表中
     */
    @Transactional
    public void saveRequestErrorLog(HyzzThirdInterfaceLog thirdInterfaceLog, ThirdResponse res) {
        flushDb(thirdInterfaceLog, res);
    }

    @Transactional
    public void saveRequestLog(HyzzThirdInterfaceLog thirdInterfaceLog, ThirdResponse res) {
        flushDb(thirdInterfaceLog, res);
    }

    /**
     * 刷新到数据库
     *
     * @param thirdInterfaceLog 日志实体
     */
    private void flushDb(HyzzThirdInterfaceLog thirdInterfaceLog) {
        thirdInterfaceLogMapper.insertLog(thirdInterfaceLog);
    }

    /**
     * 刷新到数据库并存入detail表
     *
     * @param thirdInterfaceLog 日志实体
     * @param res               响应请求
     */
    private void flushDb(HyzzThirdInterfaceLog thirdInterfaceLog, ThirdResponse res) {
        thirdInterfaceLog.setClientStatus(ThirdParamConstant.THIRD_RESPONSE_SUCCESS.equals(res.getCode()) ? "1" : "0");
        thirdInterfaceLogMapper.insertLog(thirdInterfaceLog);
        HyzzThirdInterfaceLogDetail detail = convertDetail(thirdInterfaceLog, res);
        thirdInterfaceLogMapper.insertDetailLog(detail);
    }

    private HyzzThirdInterfaceLogDetail convertDetail(HyzzThirdInterfaceLog log, ThirdResponse res) {
        HyzzThirdInterfaceLogDetail detail = new HyzzThirdInterfaceLogDetail();
        BeanUtils.copyProperties(log, detail);
        detail.setResponseBody(JSONObject.toJSONString(res));
        return detail;
    }

    /**
     * 根据requestId验证是否幂等消费
     *
     * @param requestId 请求ID
     * @return 是否幂等
     */
    public boolean verifyOnlyOnce(String requestId, String clientSupport) {
        return thirdInterfaceLogMapper.selectByRequestId(requestId, clientSupport) > 0;
    }

    public ThirdInterfaceTenant getTenantInfo(String tenantCode, String strategy) {
        return thirdInterfaceLogMapper.getTenantInfo(tenantCode, strategy);
    }
}
