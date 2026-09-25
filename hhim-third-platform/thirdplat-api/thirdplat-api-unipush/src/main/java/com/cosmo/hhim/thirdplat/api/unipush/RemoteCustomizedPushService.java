/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush;

import com.cosmo.hhim.thirdplat.api.unipush.domain.in.StandardMessage;
import com.cosmo.hhim.thirdplat.api.unipush.factory.RemoteCustomizedPushFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-28
 */
@FeignClient(contextId = "remoteCustomizedPushService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteCustomizedPushFallbackFactory.class)
public interface RemoteCustomizedPushService {

    @PostMapping("/uniapp/push/standard")
    APIResponse<Boolean> userStandardPush(StandardMessage message);

}
