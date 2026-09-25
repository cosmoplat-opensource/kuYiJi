/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.CreateMenuParam;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
public interface IMpMenuService {

    /**
     * 创建自定义菜单
     * @param param
     * @return
     */
    APIResponse<Boolean> createMenu(CreateMenuParam param);

}
