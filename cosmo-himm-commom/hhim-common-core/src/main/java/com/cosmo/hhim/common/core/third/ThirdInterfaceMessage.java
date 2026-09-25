/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.third;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 三方平台封装发送mq的实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdInterfaceMessage {
    private ThirdInterfaceMessageHeader messageHeader;
    private ThirdInterfaceMessageContent messageContent;
    private ThirdInterfaceMessageExpand messageExpand;
}
