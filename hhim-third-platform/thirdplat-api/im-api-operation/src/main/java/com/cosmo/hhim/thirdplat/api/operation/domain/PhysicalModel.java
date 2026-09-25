/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;

/**
 * iot 物模型对象 physical_model
 *
 * @author cosmo-hhim-open Team
 * @date 2024-01-29
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhysicalModel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 设备编码 */
    @Excel(name = "设备编码")
    @ApiModelProperty(value = "设备编码", name = "deviceCode", example = "")
    private String deviceCode;

    /** IOT设备编码 */
    @Excel(name = "IOT设备编码")
    @ApiModelProperty(value = "IOT设备编码", name = "iotCode", example = "")
    private String iotCode;

    /** 设备名称 */
    @Excel(name = "设备名称")
    @ApiModelProperty(value = "设备名称", name = "deviceName", example = "")
    private String deviceName;

    /** 设备物联对应的id */
    @Excel(name = "设备物联对应的id")
    @ApiModelProperty(value = "设备物联对应的id", name = "iotId", example = "")
    private String iotId;

    /** 检测项目 */
    @Excel(name = "检测项目")
    @ApiModelProperty(value = "检测项目", name = "detectionProject", example = "")
    private String detectionProject;

    /** $column.columnComment */
    @Excel(name = "检测项目")
    @ApiModelProperty(value = "$column.columnComment", name = "tag", example = "")
    private String tag;

    /** 数据类型 */
    @Excel(name = "数据类型")
    @ApiModelProperty(value = "数据类型", name = "dataType", example = "")
    private String dataType;

    /** 租户 */
    @Excel(name = "租户")
    @ApiModelProperty(value = "租户", name = "tenantCode", example = "")
    private String tenantCode;

    public void setDeviceCode(String deviceCode)
    {
        this.deviceCode = deviceCode;
    }

    public String getDeviceCode()
    {
        return deviceCode;
    }
    public void setIotCode(String iotCode)
    {
        this.iotCode = iotCode;
    }

    public String getIotCode()
    {
        return iotCode;
    }
    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    public String getDeviceName()
    {
        return deviceName;
    }
    public void setIotId(String iotId)
    {
        this.iotId = iotId;
    }

    public String getIotId()
    {
        return iotId;
    }
    public void setDetectionProject(String detectionProject)
    {
        this.detectionProject = detectionProject;
    }

    public String getDetectionProject()
    {
        return detectionProject;
    }
    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public String getTag()
    {
        return tag;
    }
    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getDataType()
    {
        return dataType;
    }
    public void setTenantCode(String tenantCode)
    {
        this.tenantCode = tenantCode;
    }

    public String getTenantCode()
    {
        return tenantCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("deviceCode", getDeviceCode())
            .append("iotCode", getIotCode())
            .append("deviceName", getDeviceName())
            .append("iotId", getIotId())
            .append("detectionProject", getDetectionProject())
            .append("tag", getTag())
            .append("dataType", getDataType())
            .append("tenantCode", getTenantCode())
            .toString();
    }
}
