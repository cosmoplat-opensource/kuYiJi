/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.out;

import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-26
 */
@Data
public class PushDetailResult {
    // 推送详情结果列表
    private List<PushDetail> pushDetails;

    @Data
    public static class PushDetail{
        // 时间，格式：yyyy-MM-dd HH:mm:ss
        private String time;

        // 事件
        private String event;
    }
}
