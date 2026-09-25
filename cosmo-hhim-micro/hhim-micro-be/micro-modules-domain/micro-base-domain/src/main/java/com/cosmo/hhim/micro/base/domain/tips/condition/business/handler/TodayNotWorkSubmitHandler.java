/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @desc 检查当天是否未记工
 * @createTime 2023/2/8
 */
@Slf4j
@Component
public class TodayNotWorkSubmitHandler extends AbstractBusinessHandler {

    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";

    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult result = new BusinessHandlerResult();

        // 获取业务条件配置
        Map<String, String> businessConditionConfigMap = super.getBusinessConditionConfigMap();
        if (CollectionUtils.isEmpty(businessConditionConfigMap)
                || !StringUtils.hasText(businessConditionConfigMap.get(START_TIME))
                || !StringUtils.hasText(businessConditionConfigMap.get(END_TIME))) {
            log.warn("业务条件参数未配置或配置格式非法！");
            return result;
        }

        // 检查当前是否是否在17:00～19:00之间
        Date nowDate = DateUtils.getNowDate();
        DateTime beginOfDay = DateUtil.beginOfDay(nowDate);
        Date beginDate = DateUtils.setHours(beginOfDay, Integer.parseInt(businessConditionConfigMap.get(START_TIME)));
        Date endDate = DateUtils.setHours(beginOfDay, Integer.parseInt(businessConditionConfigMap.get(END_TIME)));

        // 查询当前员工今天的记工记录
        MicroWorkSubmitDto queryParam = new MicroWorkSubmitDto();
        queryParam.setSubmitUser(param.getUserId().toString());
        queryParam.setStartDate(DateUtil.beginOfDay(nowDate));
        List<MicroWorkSubmit> microWorkSubmits = microWorkSubmitMapper.selectWorkSubmitInfos(queryParam);
        if (CollectionUtils.isEmpty(microWorkSubmits) && nowDate.after(beginDate) && nowDate.before(endDate)) {
            result.setMatched(true);
        }

        return result;
    }
}
