/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums;

import com.cosmo.hhim.common.core.validation.EnumValidate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-03
 */
@Getter
@AllArgsConstructor
public enum StaffCalendarStatisticTypeEnum implements EnumValidate<String>, BaseEnum<String> { 
    WORK_STATISTIC("10", "记工统计"),
    WAIT_CHECK_STATISTIC("20", "待审统计");

    private String code;
    private String desc;

    public static StaffCalendarStatisticTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    @Override
    public boolean existValidate(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        for (StaffCalendarStatisticTypeEnum staffCalendarStatisticTypeEnum : StaffCalendarStatisticTypeEnum.values()) {
            if (staffCalendarStatisticTypeEnum.getCode().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
