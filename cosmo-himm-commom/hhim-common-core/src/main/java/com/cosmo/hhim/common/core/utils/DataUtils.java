/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 数据工具类
 *
 * @author cosmo-hhim-open Team
 */
public class DataUtils {

    /**
     * 返回多个值中最小的BigDecimal
     *
     * @return BigDecimal
     */
    public static BigDecimal getMinBigDecimal(BigDecimal... msgParams) {
        BigDecimal min = msgParams[0];
        for (BigDecimal msgParam : msgParams) {
            if (msgParam.compareTo(min) < 0) {
                min = msgParam;
            }
        }
        return min;
    }

    /**
     * 返回多个值中最大的BigDecimal
     *
     * @return BigDecimal
     */
    public static BigDecimal getMaxBigDecimal(BigDecimal... msgParams) {
        BigDecimal max = msgParams[0];
        for (BigDecimal msgParam : msgParams) {
            if (msgParam.compareTo(max) > 0) {
                max = msgParam;
            }
        }
        return max;
    }
}
