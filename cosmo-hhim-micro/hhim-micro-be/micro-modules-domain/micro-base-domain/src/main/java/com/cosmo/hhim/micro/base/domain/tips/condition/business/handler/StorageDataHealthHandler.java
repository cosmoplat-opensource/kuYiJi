/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class StorageDataHealthHandler extends AbstractBusinessHandler {

    private static final String RATIO = "ratio";
    private static final String STORAGE_TOTAL = "total";
    private static final String STORAGE_NG_NUM = "ngNum";

    @Autowired
    private IMicroProcessStorageService microProcessStorageService;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult result = new BusinessHandlerResult();

        // 1.获取业务条件配置信息
        Map<String, String> businessConditionConfigMap = super.getBusinessConditionConfigMap();
        if (CollectionUtils.isEmpty(businessConditionConfigMap) || !StringUtils.hasText(businessConditionConfigMap.get(RATIO))) {
            log.warn("业务条件参数未配置或配置格式非法！");
            return result;
        }

        // 2.获取库存健康度
        Map<String, Object> storageHealthMap = microProcessStorageService.storageHealth();
        if (!CollectionUtils.isEmpty(storageHealthMap)) {
            BigDecimal thresholdValue = new BigDecimal(businessConditionConfigMap.get(RATIO)); // 提示阀值 

            BigDecimal storageTotal = new BigDecimal((Long) storageHealthMap.get(STORAGE_TOTAL));
            BigDecimal storageNgNum = (BigDecimal) storageHealthMap.get(STORAGE_NG_NUM);
            if (null == storageNgNum) {
                storageNgNum = BigDecimal.ZERO;
            }
            if (storageTotal.signum() == 0 || (storageTotal.subtract(storageNgNum)).signum() == 0) {
                return result;
            }
            BigDecimal storageHealthRatio = (storageTotal.subtract(storageNgNum)).divide(storageTotal, BigDecimal.ROUND_HALF_UP, RoundingMode.CEILING);
            if (thresholdValue.compareTo(storageHealthRatio) > 0) { // 判断库存健康度是否超过提示阀值 
                result.setMatched(true);
                List<String> placeHolderInfos = Lists.newArrayList();
                placeHolderInfos.add(thresholdValue.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toString());
                result.setPlaceHolderInfos(placeHolderInfos);
            }

        }

        return result;
    }
}
