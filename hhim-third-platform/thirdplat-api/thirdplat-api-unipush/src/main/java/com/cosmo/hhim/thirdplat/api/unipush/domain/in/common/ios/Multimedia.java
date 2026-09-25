/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.ios;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-24
 */
@Data
public class Multimedia {
    /**
     * 多媒体资源地址
     */
    private String url;
    /**
     * 资源类型（1.图片，2.音频，3.视频）
     */
    private Integer type;
    /**
     * 是否只在wifi环境下加载，如果设置成true,但未使用wifi时，会展示成普通通知
     */
    @JSONField(name = "only_wifi")
    private boolean onlyWifi;
}
