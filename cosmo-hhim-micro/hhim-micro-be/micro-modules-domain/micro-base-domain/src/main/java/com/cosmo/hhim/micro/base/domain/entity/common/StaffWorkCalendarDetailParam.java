/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.cosmo.hhim.common.core.annotation.EnumStringValid;
import com.cosmo.hhim.micro.infrastructure.enums.StaffCalendarStatisticTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-03
 */
@Data
public class StaffWorkCalendarDetailParam extends StaffWorkStatisticParam {

    // 统计类型（10：记工数统计， 20：待审数统计） 
    @NotBlank(message = "统计类型不允许为空！")
    @EnumStringValid(message = "统计类型不合法！", enumClass = StaffCalendarStatisticTypeEnum.class)
    private String statisticType;

}
