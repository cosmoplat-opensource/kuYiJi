/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.service;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.utils.bean.BeanUtils;
import com.cosmo.hhim.external.mapper.HyzzThirdInterfaceLogMapper;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.flush.FlushService;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceLog;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceLogDetail;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdResponse;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdStatusEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ThirdLogServiceImpl implements FlushService {

    @Resource
    private HyzzThirdInterfaceLogMapper thirdInterfaceLogMapper;

    @Override
    public void saveLog(ThirdStatusEnum statusEnum, HyzzThirdInterfaceLog interfaceLog, ThirdResponse response) {
        thirdInterfaceLogMapper.insertLog(interfaceLog);
        HyzzThirdInterfaceLogDetail detail = convertDetail(interfaceLog, response);
        thirdInterfaceLogMapper.insertDetailLog(detail);
    }

    private HyzzThirdInterfaceLogDetail convertDetail(HyzzThirdInterfaceLog log, ThirdResponse res) {
        HyzzThirdInterfaceLogDetail detail = new HyzzThirdInterfaceLogDetail();
        BeanUtils.copyProperties(log, detail);
        detail.setResponseBody(JSONObject.toJSONString(res));
        return detail;
    }
}
