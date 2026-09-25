/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import com.cosmo.hhim.micro.base.domain.entity.warn.DataHealth;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroDataWarnService;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @desc 判断数据健康度是否低于指定阀值
 * @createTime 2023/2/10
 */
@Slf4j
@Component
public class DataHealthHandler extends AbstractBusinessHandler {

    public static final String RATIO = "ratio";

    @Autowired
    private IMicroDataWarnService microDataWarnService;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        StopWatch stopWatch = new StopWatch();
        BusinessHandlerResult result = new BusinessHandlerResult();

        // 1.获取业务条件配置信息
        stopWatch.start("获取配置");
        Map<String, String> businessConditionConfigMap = super.getBusinessConditionConfigMap();
        if (CollectionUtils.isEmpty(businessConditionConfigMap) || !StringUtils.hasText(businessConditionConfigMap.get(RATIO))) {
            log.warn("业务条件参数未配置或配置格式非法！");
            return result;
        }
        stopWatch.stop();

        // 2.查询当前数据健康度
        stopWatch.start("查健康度");
        DataHealth dataHealth = microDataWarnService.calculateDataHealth();
        stopWatch.stop();
        log.info("内容提示健康度Handler用时信息---->{}", stopWatch.prettyPrint());

        if (null != dataHealth && null != dataHealth.getDataHealth()) {
            BigDecimal thresholdValue = new BigDecimal(businessConditionConfigMap.get(RATIO));
            if (thresholdValue.compareTo(dataHealth.getDataHealth()) > 0) {
                result.setMatched(true);
                List<String> placeHolderInfos = Lists.newArrayList();
                placeHolderInfos.add(thresholdValue.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toString());
                result.setPlaceHolderInfos(placeHolderInfos);
            }

        }
        return result;
    }
}
