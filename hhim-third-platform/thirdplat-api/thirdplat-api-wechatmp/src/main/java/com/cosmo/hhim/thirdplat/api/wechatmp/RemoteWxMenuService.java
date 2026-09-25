/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.factory.RemoteWxMenuFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@FeignClient(contextId = "RemoteWxMenuService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteWxMenuFallbackFactory.class)
public interface RemoteWxMenuService {

}
