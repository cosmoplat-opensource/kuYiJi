/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import com.cosmo.hhim.micro.base.domain.entity.submit.SubmitRecordQueryParam;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitService;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author cosmo-hhim-open Team
 * @desc 校验待审核记工记录是否存在预警标识
 * @createTime 2023/2/17
 */
@Slf4j
@Component
public class WarnFlagForCheckHandler extends AbstractBusinessHandler {

    @Autowired
    private IMicroWorkSubmitService microWorkSubmitService;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult result = new BusinessHandlerResult();

        // 查询待审核的记工记录是否存在预警标识
        SubmitRecordQueryParam queryParam = new SubmitRecordQueryParam();
        queryParam.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());
        if (microWorkSubmitService.isHaveWarnExceptionForWorkSubmits(queryParam)) {
            result.setMatched(true);
        }

        return result;
    }
}
