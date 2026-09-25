/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tips;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 内容提示配置对象 micro_content_tip_config
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-06
 */
@Data
public class MicroContentTipConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 触发角色条件集合（角色编码是micro_role表的role_code，多个用,分割）
     */
    private String triggerRoles;

    /**
     * 触发动作条件集合（TipTriggerActionEnum，多个用,分割）
     */
    private String triggerActions;

    /**
     * 动作类型（参考：ActionTypeEnums）
     */
    private String actionType;

    /**
     * 触发业务条件beanName
     */
    private String triggerBusinessBean;

    /**
     * 业务条件参数配置（格式：key:value，多个用,分割）
     */
    private String businessConditionConfig;

    /**
     * 触发优先级
     */
    private Long priority;

    /**
     * 提示内容（使用{0}{1}作为占位符）
     */
    private String tipContent;

    // 提示方式-供前端显示标识（10:状态栏显示，20:弹窗显示） 
    private String tipWay;

    /**
     * 周期类型（ClockTipHandlerTypeEnum）
     */
    private String periodType;

    /**
     * 周期时间
     */
    private Long periodTime;

    /**
     * 周期时间单位（PeriodTimeUnitEnum）
     */
    private String periodUnit;

    /**
     * 间隔时长
     */
    private Long intervalTime;

    /**
     * 间隔时长单位（PeriodTimeUnitEnum）
     */
    private String intervalUnit;

    /**
     * 周期内触发次数上限
     */
    private Long periodMaxNum;

    /**
     * 可用标识（0：正常，1：停用）
     */
    private String activeFlag;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    /**
     * 更新人
     */
    @Excel(name = "更新人")
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdDate;
}
