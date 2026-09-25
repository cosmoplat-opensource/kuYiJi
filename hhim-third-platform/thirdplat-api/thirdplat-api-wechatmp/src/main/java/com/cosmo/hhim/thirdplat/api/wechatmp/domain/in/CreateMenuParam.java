/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.domain.in;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.WxMpMenu;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-12
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMenuParam {
    // 菜单按钮信息列表
    @NotNull(message = "菜单按钮列表不允许为空！")
    private List<WxMpMenu> button;
}
