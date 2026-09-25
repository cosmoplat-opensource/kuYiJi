/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 【请填写功能名称】对象 hyzz_custom_job
 * 
 * @author cosmo-hhim-open Team
 * @date 2021-12-06
 */
@ApiModel(value="【请填写功能名称】对象 hyzz_custom_job",description="【请填写功能名称】对象 hyzz_custom_job")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HyzzCustomJob extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    @ApiModelProperty(value = "$column.columnComment", name = "id", example = "")
    private Long id;

    /** 系统编码 */
    @Excel(name = "系统编码")
    @ApiModelProperty(value = "系统编码", name = "systemCode", example = "")
    private String systemCode;

    /** 任务描述 */
    @Excel(name = "任务描述")
    @ApiModelProperty(value = "任务描述", name = "taskDesc", example = "")
    private String taskDesc;

    /** 数据源 */
    @Excel(name = "数据源")
    @ApiModelProperty(value = "数据源", name = "db", example = "")
    private String db;

    /** 数据库 */
    @Excel(name = "数据库")
    @ApiModelProperty(value = "数据库", name = "schema", example = "")
    private String schema;

    /** 租户编码 */
    @Excel(name = "租户编码")
    @ApiModelProperty(value = "租户编码", name = "tenantCode", example = "")
    private String tenantCode;

    /** 定时任务表达式 */
    @Excel(name = "定时任务表达式")
    @ApiModelProperty(value = "定时任务表达式", name = "cron", example = "")
    private String cron;

    /** 提前时间（默认值0）分钟 */
    @Excel(name = "提前时间", readConverterExp = "默=认值0")
    @ApiModelProperty(value = "提前时间（默认值0）分钟", name = "taskAdvanceTime", example = "")
    private Integer taskAdvanceTime;

    /** 下次执行时间 */
    @Excel(name = "下次执行时间")
    @ApiModelProperty(value = "下次执行时间", name = "taskNextTime", example = "")
    private long taskNextTime;

    /** 上次执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上次执行时间", width = 30, dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "上次执行时间", name = "taskLastTime", example = "")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date taskLastTime;

    /** mq主题 */
    @Excel(name = "mq主题")
    @ApiModelProperty(value = "mq主题", name = "topic", example = "")
    private String topic;

    /** mq标签 */
    @Excel(name = "mq标签")
    @ApiModelProperty(value = "mq标签", name = "tag", example = "")
    private String tag;

    /** 执行参数 */
    @Excel(name = "执行参数")
    @ApiModelProperty(value = "执行参数", name = "taskParam", example = "")
    private String taskParam;

    /** 调度状态：0-停止，1-运行 */
    @Excel(name = "调度状态：0-停止，1-运行")
    @ApiModelProperty(value = "调度状态：0-停止，1-运行", name = "taskStatus", example = "")
    private Integer taskStatus;

    /** 错过时间  默认0分钟  */
    @Excel(name = "错过时间  默认0分钟 ")
    @ApiModelProperty(value = "错过时间  默认0分钟 ", name = "missTime", example = "")
    private Integer missTime;

    /** 错过时间  0错过也执行，1错过不执行  默认0分钟  */
    @Excel(name = "错过时间  0错过也执行，1错过不执行  默认0分钟 ")
    @ApiModelProperty(value = "错过时间  0错过也执行，1错过不执行  默认0分钟 ", name = "missStatus", example = "")
    private Integer missStatus;

    private Date planStartTime;

    private Long cronTime;

    public Date getPlanStartTime() {
        return planStartTime;
    }

    public void setPlanStartTime(Date planStartTime) {
        this.planStartTime = planStartTime;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setSystemCode(String systemCode) 
    {
        this.systemCode = systemCode;
    }

    public String getSystemCode() 
    {
        return systemCode;
    }
    public void setTaskDesc(String taskDesc) 
    {
        this.taskDesc = taskDesc;
    }

    public String getTaskDesc() 
    {
        return taskDesc;
    }
    public void setDb(String db) 
    {
        this.db = db;
    }

    public String getDb() 
    {
        return db;
    }
    public void setSchema(String schema) 
    {
        this.schema = schema;
    }

    public String getSchema() 
    {
        return schema;
    }
    public void setTenantCode(String tenantCode) 
    {
        this.tenantCode = tenantCode;
    }

    public String getTenantCode() 
    {
        return tenantCode;
    }
    public void setCron(String cron) 
    {
        this.cron = cron;
    }

    public String getCron() 
    {
        return cron;
    }
    public void setTaskAdvanceTime(Integer taskAdvanceTime) 
    {
        this.taskAdvanceTime = taskAdvanceTime;
    }

    public Integer getTaskAdvanceTime() 
    {
        return taskAdvanceTime;
    }


    public long getTaskNextTime() {
        return taskNextTime;
    }

    public void setTaskNextTime(long taskNextTime) {
        this.taskNextTime = taskNextTime;
    }

    public void setTaskLastTime(Date taskLastTime)
    {
        this.taskLastTime = taskLastTime;
    }

    public Date getTaskLastTime() 
    {
        return taskLastTime;
    }
    public void setTopic(String topic) 
    {
        this.topic = topic;
    }

    public String getTopic() 
    {
        return topic;
    }
    public void setTag(String tag) 
    {
        this.tag = tag;
    }

    public String getTag() 
    {
        return tag;
    }
    public void setTaskParam(String taskParam) 
    {
        this.taskParam = taskParam;
    }

    public String getTaskParam() 
    {
        return taskParam;
    }
    public void setTaskStatus(Integer taskStatus) 
    {
        this.taskStatus = taskStatus;
    }

    public Integer getTaskStatus() 
    {
        return taskStatus;
    }
    public void setMissTime(Integer missTime) 
    {
        this.missTime = missTime;
    }

    public Integer getMissTime() 
    {
        return missTime;
    }
    public void setMissStatus(Integer missStatus) 
    {
        this.missStatus = missStatus;
    }

    public Integer getMissStatus() 
    {
        return missStatus;
    }

    public Long getCronTime() {
        return cronTime;
    }

    public void setCronTime(Long cronTime) {
        this.cronTime = cronTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("systemCode", getSystemCode())
            .append("taskDesc", getTaskDesc())
            .append("db", getDb())
            .append("schema", getSchema())
            .append("tenantCode", getTenantCode())
            .append("cron", getCron())
            .append("taskAdvanceTime", getTaskAdvanceTime())
            .append("taskNextTime", getTaskNextTime())
            .append("taskLastTime", getTaskLastTime())
            .append("topic", getTopic())
            .append("tag", getTag())
            .append("taskParam", getTaskParam())
            .append("taskStatus", getTaskStatus())
            .append("missTime", getMissTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("missStatus", getMissStatus())
            .toString();
    }
}
