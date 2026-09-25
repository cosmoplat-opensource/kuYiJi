/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Data
public class GetMsgTemplateIdInDto {

    // 模板库中模板的编号，有“TM**”和“OPENTMTM**”等形式(Y)
    private String template_id_short;
}
