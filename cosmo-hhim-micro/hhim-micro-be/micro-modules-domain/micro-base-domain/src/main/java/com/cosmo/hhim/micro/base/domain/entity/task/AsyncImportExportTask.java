/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.task;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


/**
 * 【请填写功能名称】对象 async_import_export_task
 *
 * @author cosmo-hhim-open Team
 * @date 2021-12-15
 */
@Data
public class AsyncImportExportTask extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */

    private int taskId;

    /**
     * 任务名称
     */


    private String taskName;

    /**
     * 公司id
     */


    private String tenantCode;

    /**
     * 用户名
     */


    private String userName;

    /**
     * 请求参数
     */


    private String reqParam;

    /**
     * 任务状态(默认0执行中 成功1 失败2)
     */


    private Integer taskStatus;

    /**
     * 处理时间(毫秒)
     */


    private Long dealTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")


    private Date finishTime;

    /**
     * 下载地址
     */


    private String url;

    /**
     * 任务编码 bean
     */


    private String taskCode;

    /**
     * 任务类型
     * 1：上传（导入）
     * 2：下载（导出）
     */

    private Integer taskType;


    private String mqKeys;

    private String sysCode;

    private String objectName;

    private String msg;

    private String exceptionMsg;

    private String exportParams;

    private Long userId;

    private String authorization;

    //导入的成功数据 
    private String successData;


}
