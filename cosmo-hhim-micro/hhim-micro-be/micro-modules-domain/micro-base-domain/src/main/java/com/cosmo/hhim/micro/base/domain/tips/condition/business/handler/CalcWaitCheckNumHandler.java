/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @desc 计算当前用户有多少条待审核数据
 * @createTime 2023/2/10
 */
@Slf4j
@Component
public class CalcWaitCheckNumHandler extends AbstractBusinessHandler {

    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult result = new BusinessHandlerResult();

        // 查询历史报工记录中未审核的记录
        MicroWorkSubmitDto queryParam = new MicroWorkSubmitDto();
        queryParam.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());
        List<MicroWorkSubmit> microWorkSubmits = microWorkSubmitMapper.selectWorkSubmitInfos(queryParam);
        if (CollectionUtils.isEmpty(microWorkSubmits)) {
            return result;
        }

        result.setMatched(true);

        List<String> placeHolderInfos = Lists.newArrayList();
        placeHolderInfos.add(String.valueOf(microWorkSubmits.size()));
        result.setPlaceHolderInfos(placeHolderInfos);
        return result;
    }
}
