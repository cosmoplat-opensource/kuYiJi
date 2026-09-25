/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.dto.out.stats;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-23
 */
@Data
public class StatsResult {
    /**
     * 可下发数
     */
    @JSONField(name = "msg_num")
    private Integer msgNum;
    /**
     * 下发数
     */
    @JSONField(name = "target_num")
    private Integer targetNum;
    /**
     * 到达数
     */
    @JSONField(name = "receive_num")
    private Integer receiveNum;
    /**
     * 展示数
     */
    @JSONField(name = "display_num")
    private Integer displayNum;
    /**
     * 点击数
     */
    @JSONField(name = "click_num")
    private Integer clickNum;
}
