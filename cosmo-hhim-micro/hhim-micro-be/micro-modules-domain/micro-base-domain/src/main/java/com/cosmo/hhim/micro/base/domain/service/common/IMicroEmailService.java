/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/25
 */
public interface IMicroEmailService {

    /**
     * 发送邮件
     *
     * @param mailSubject    邮件主题
     * @param mailTemplateId 平台邮件服务的模版id
     * @param receivedBy     邮件接收人
     * @param dataList       导出的数据
     * @param requiredType   导出对象class
     * @return
     */
    <T> String exportAndSendEmail(String mailSubject, String mailTemplateId, String receivedBy, List<T> dataList, Class<T> requiredType, Map<String, Object> placeholderMap); 

    /**
     * 发送邮件-easyPoi
     *
     * @param mailSubject    邮件主题
     * @param mailTemplateId 平台邮件服务的模版id
     * @param receivedBy     邮件接收人
     * @param dataList       导出的数据
     * @param requiredType   导出对象class
     * @return
     */
    <T> String exportAndSendEmailByEasyPoi(String mailSubject, String mailTemplateId, String receivedBy, List<T> dataList, Class<T> requiredType, Map<String, Object> placeholderMap); 

    /**
     * 多sheet导出
     *
     * @param mailSubject
     * @param mailTemplateId
     * @param receivedBy
     * @param multiSheetList
     * @param placeholderMap
     * @return
     */
    String exportAndSendEmailByMultiSheet(String mailSubject, String mailTemplateId, String receivedBy, List<Map<String, Object>> multiSheetList, Map<String, Object> placeholderMap);
}
