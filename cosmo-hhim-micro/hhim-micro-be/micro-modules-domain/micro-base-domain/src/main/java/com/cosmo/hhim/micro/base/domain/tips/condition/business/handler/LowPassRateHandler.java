/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import com.cosmo.hhim.micro.infrastructure.enums.DataWarnTypeEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description: 报工低良品率的判断handler
 * @date 2023/2/10 13:28
 */
@Component
@Slf4j
public class LowPassRateHandler extends AbstractBusinessHandler {

    @Autowired
    private MicroSupportUtil supportUtil;
    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult result = new BusinessHandlerResult();

        // 1. 获取当前员工的最新一条的报工记录信息
        MicroWorkSubmit findParam = new MicroWorkSubmit();
        findParam.setSubmitUser(SecurityUtils.getUserId().toString());
        List<MicroWorkSubmit> workSubmits = microWorkSubmitMapper.selectMicroWorkSubmitList(findParam);
        // 按照id倒序
        workSubmits.sort(Comparator.comparing(MicroWorkSubmit::getId).reversed());

        // 2. 获取最新报工记录的良品率
        MicroWorkSubmit workSubmit = workSubmits.get(0);
        BigDecimal passRate = workSubmit.getPassNum()
                .divide(workSubmit.getPassNum().add(workSubmit.getNgNum()), 4, RoundingMode.DOWN);
        // 获取该产品 + 工序的平均良品率
        Map<String, Object> warningMetricsBySeq = supportUtil.getWarningMetricsBySeq(workSubmit.getProductSeq(), workSubmit.getOperateProcessSeq());
        if (!CheckObjectUtils.isEmpty(warningMetricsBySeq)) {
            // 获取平均良品率
            BigDecimal passRateTemp = new BigDecimal(warningMetricsBySeq.get(DataWarnTypeEnum.PASS_RATE.getCode()).toString());
            if (passRate.compareTo(passRateTemp) < 0) {
                result.setMatched(true);
            }
        }

        return result;
    }
}
