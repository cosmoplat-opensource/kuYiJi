/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.WxMpMenu;
import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-12
 */
@Data
public class CreateMenuInfoInDto {

    // 菜单按钮信息列表
    private List<WxMpMenu> button;

}
