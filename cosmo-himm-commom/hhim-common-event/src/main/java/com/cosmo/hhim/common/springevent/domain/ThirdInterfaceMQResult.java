/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.springevent.domain;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description 抽象业务中@EventListener标记的方法的返回对象
 *              对返回信息进行定义
 * @createTime 2022-07-22
 */
@Data
public class ThirdInterfaceMQResult<T> {

    // 方法
    private String method;

    // 方法版本
    private String version;

    // MQ Body内容
    private T bizMqContent;

    // 有序发送遵循字段
    private String orderByKey;

    //延迟消息时间 单位秒
    private long delayTime ;

    // 回调原始信息
    private String callbackRawData;

}
