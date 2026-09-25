/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.entity;

import com.cosmo.hhim.micro.infrastructure.enums.ToDoPushStatusEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author cosmo-hhim-open Team
 * 待办推送实体
 */
@Data
public class TodoPushParam implements Serializable {
    /**
     * 业务系统待办id，需要保证待办id和待办人组成唯一联合主键
     */
    private String sourceId;
    /**
     * 待办标题（长度100）
     */
    private String title;
    /**
     * 待办内容（长度500
     */
    private String content;
    /**
     * 待办在客户端显示的图url
     */
    private String headImg;
    /**
     * 跳转地址,需对接单点登录（长度500）
     */
    private String redirectUrl;
    /**
     * 待办发起人的用户id(uuc的手机号)
     */
    private String senderId;
    /**
     * 待办处理人的用户id(uuc的手机号，不是手机号会导致用户在移动端的待办列表查询不到该待办)。
     * 发起人可同时给多个待办人发起待办，多个待办人时，待办人字段参数handlerId用逗号分隔，例如138****8888,139****9999
     */
    private String handlerId;
    /**
     * 代办列表中展示的标签文本
     */
    private String label;
    /**
     * 状态：0完成；1激活；2撤回；3拒绝
     */
    private ToDoPushStatusEnum status;
}
