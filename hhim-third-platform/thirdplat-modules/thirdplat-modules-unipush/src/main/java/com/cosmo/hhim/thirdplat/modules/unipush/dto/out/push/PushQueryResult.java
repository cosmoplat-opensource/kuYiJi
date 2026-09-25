/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.dto.out.push;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-23
 */
@Data
public class PushQueryResult {

    // 请求返回详细数据
    private List<PushDetail> detail;

    @Data
    public static class PushDetail {
        // 时间，格式：yyyy-MM-dd HH:mm:ss
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private String time;

        // 事件
        private String event;
    }
}
