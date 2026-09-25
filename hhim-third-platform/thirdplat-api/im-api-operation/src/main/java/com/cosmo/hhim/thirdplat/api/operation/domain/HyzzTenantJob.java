/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

import java.util.Date;

/**
 * 【请填写功能名称】对象 hyzz_tenant_job
 *
 * @author cosmo-hhim-open Team
 * @date 2021-12-13
 */
@Data
public class HyzzTenantJob {

    /**
     * $column.columnComment
     */

    private Long id;

    /**
     * 系统编码
     */


    private String systemCode;

    /**
     * 任务描述
     */


    private String taskDesc;

    /**
     * 定时任务表达式
     */


    private String cron;

    /**
     * 下次执行时间
     */


    private Long taskNextTime;

    /**
     * 上次执行时间
     */


    private Date taskLastTime;

    /**
     * mq主题
     */


    private String topic;

    /**
     * mq标签
     */


    private String tag;

    /**
     * 执行参数
     */


    private String taskParam;

    /**
     * 调度状态：0-停止，1-运行
     */


    private Integer taskStatus;


    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;
}
