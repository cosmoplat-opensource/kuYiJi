/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.text.UUID;
import com.cosmo.hhim.common.core.web.domain.BaseTable;
import com.cosmo.hhim.common.core.web.domain.MessageContent;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

/**
 * WMS调用Sop中间件dto转换工具类.
 *
 * @author cosmo-hhim-open Team
 */
public class TransferSopDtoUtils {

    /**
     * 生成BaseTable.
     */
    public static BaseTable convertErpBaseTable(String businessType,Object data) {
        return convertErpBaseTable(businessType,null,data);
    }


    /**
     * 生成BaseTable.
     */
    public static BaseTable convertErpBaseTable(String businessType,String oriBusinessType,Object data) {
        BaseTable baseTable = new BaseTable();
        baseTable.setBusinessType(businessType);
        baseTable.setOriBusinessType(oriBusinessType);
        baseTable.setProvider("WMS");
        baseTable.setConsumer("ERP");
        baseTable.setMessageId(UUID.fastUUID().toString());
        MessageContent messageContent = new MessageContent();
        messageContent.setData(data);
        baseTable.setMessageContent(messageContent);
        return baseTable;
    }

    /**
     * 从BaseTable中的data中得到指定数据类型-对象
     */
    public static <T> T analysisErpBaseTable (BaseTable baseTable,Class<T> clazz) {
        MessageContent messageContent = baseTable.getMessageContent();
        return StringUtils.convertObject(messageContent.getData(),clazz);
    }

    /**
     * 从BaseTable中的data中得到指定数据类型--集合
     */
    public static <T> List<T> analysisErpBaseTableList (BaseTable baseTable,Class<T> clazz) {
        MessageContent messageContent = baseTable.getMessageContent();
        return StringUtils.convertObjectList(messageContent.getData(),clazz);
    }
}
