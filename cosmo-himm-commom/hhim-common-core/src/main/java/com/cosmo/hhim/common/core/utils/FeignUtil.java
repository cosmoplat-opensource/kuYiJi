/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.domain.FeignResult;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.exception.WmsServiceException;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 自定义工具类 方便在非spring管理环境中使用自定义方法
 *
 * @author cosmo-hhim-open Team
 */
@Component
public final class FeignUtil {


    public static <T> T failBack(Throwable throwable) {
        throw new CustomException("服务调用异常");
    }

    public static <T> T failBack(Throwable throwable, String msg) {
        throw new WmsServiceException(msg, throwable);
    }

    /**
     * FeignResult转换成AjaxResult
     *
     */
    public static AjaxResult feignToAjax(FeignResult data){
        AjaxResult ajaxResult = new AjaxResult(data.getCode(),data.getMsg(),data.getData());
        return ajaxResult;
    }

    /**
     * FeignResult转换成TableDataInfo
     *
     */
    public static TableDataInfo feignToTable(FeignResult data){
        TableDataInfo dataInfo = new TableDataInfo(data.getRows(),data.getTotal(),data.getCode(),data.getMsg());
        dataInfo.setData(CheckObjectUtils.isNotEmpty(data.getData()) && data.getData() instanceof Map ? null : JSONObject.parseObject(JSONObject.toJSONString(data.getData()), Map.class));
        return dataInfo;
    }
}
