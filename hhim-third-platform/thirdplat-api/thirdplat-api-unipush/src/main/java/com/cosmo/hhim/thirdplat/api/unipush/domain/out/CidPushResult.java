/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.out;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-26
 */
@Data
public class CidPushResult {
    // 设备ID
    private String cid;

    /**
     * 推送结果
     * successed_offline: 离线下发(包含厂商通道下发)，
     * successed_online: 在线下发，
     * successed_ignore: 最近90天内不活跃用户不下发
     */
    private String result;
}
