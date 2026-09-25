/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.utils.DateUtils;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-03
 */
@Data
public class StaffWorkStatisticParam {

    private static final String SPILT_CHAR = "-";

    // 查询日期（格式：yyyy-MM） 
    @NotBlank(message = "查询日期不允许为空！")
    @Pattern(regexp = "^\\d{4}-\\d{1,2}", message = "查询日期格式不合法！")
    private String queryDate;

    // 查询开始时间 
    private Date queryStartDate;

    // 查询结束时间 
    private Date queryEndDate;

    /**
     * 报工类型
     */
    private Long submitType;

    /**
     * 生成查询时间范围
     */
    public void genQueryTimeRange() {
        Date nowDate = DateUtils.getNowDate();

        // 查询日期String转Date
        List<String> queryDateSpiltStr = Arrays.asList(queryDate.split(SPILT_CHAR));
        LocalDate queryLocalDate = LocalDate.of(Integer.parseInt(queryDateSpiltStr.get(0)), Integer.parseInt(queryDateSpiltStr.get(1)), 1);
        Date queryDate = Date.from(queryLocalDate.atStartOfDay().toInstant(ZoneOffset.of("+8")));

        queryStartDate = DateUtil.beginOfMonth(queryDate).toJdkDate();
        queryEndDate = DateUtil.endOfMonth(queryDate).toJdkDate();
        if (queryEndDate.after(nowDate)) {
            queryEndDate = nowDate;
        }
    }

}
