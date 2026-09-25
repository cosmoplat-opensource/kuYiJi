/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation;

import com.cosmo.hhim.thirdplat.api.operation.factory.RemoteThirdTaskFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 自定义定时任务
 *
 * @author cosmo-hhim-open Team
 * @version 1.0
 * @restApi feign接口类上要加上这个标签, 否则无法抽取
 */
@FeignClient(contextId = "RemoteThirdTaskService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteThirdTaskFallbackFactory.class)
public interface RemoteThirdTaskService {

    /**
     * 三方平台调用外部系统补偿任务
     *
     * @param shardIndex
     * @param shardTotal
     * @return
     */
    @GetMapping("/external/retryTask")
    APIResponse retryTask(@RequestParam("shardIndex") Integer shardIndex, @RequestParam("shardTotal") Integer shardTotal);

}
