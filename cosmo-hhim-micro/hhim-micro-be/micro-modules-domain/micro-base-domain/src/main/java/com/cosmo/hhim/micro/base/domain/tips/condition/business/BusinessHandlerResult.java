/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business;

import com.cosmo.hhim.micro.base.domain.entity.tips.MicroContentTipConfig;
import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
@Data
public class BusinessHandlerResult {

    // 是否满足业务条件 
    private boolean matched;

    // 提示内容占位符填充信息集合（注意：需按照db中配置的提示占位符顺序排序） 
    private List<String> placeHolderInfos;

    // 内容提示配置 
    private MicroContentTipConfig tipConfig;

}
