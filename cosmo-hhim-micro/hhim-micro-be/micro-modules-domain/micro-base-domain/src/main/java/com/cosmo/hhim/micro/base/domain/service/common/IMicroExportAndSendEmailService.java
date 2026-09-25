/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 导出并发送邮件服务接口
 * @date 2023/1/6 09:48
 */
public interface IMicroExportAndSendEmailService {

    /**
     * 报工记录 - 导出excel并发送邮件
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    String exportEmployeeSubmitRecord(Date startDate, Date endDate, String receivedBy);

    /**
     * 完工产品 - 导出excel并发送邮件
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    String exportFinishedProduct(Date startDate, Date endDate, String receivedBy);

    /**
     * 车间库存 - 导出excel并发送邮件
     *
     * @param receivedBy
     * @return
     */
    String exportAllProcessStorage(String receivedBy);

    /**
     * 质量趋势 - 导出Excel并发送邮件
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    String exportQualityTrends(Date startDate, Date endDate, String receivedBy);

    /**
     * 库存变动记录 - 导出excel并发送邮件
     *
     * @param productSeq
     * @param processSeq
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    String exportStockChangeRecords(String productSeq, String processSeq, Date startDate, Date endDate, String receivedBy);

    /**
     * 车间库存 - 导出期初工序库存导入模版
     * @param receivedBy
     * @return
     */
    String exportProcessStorageTemplate(String receivedBy);
}
