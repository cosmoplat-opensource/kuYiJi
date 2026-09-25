/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.controller;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.external.service.ThirdTaskRetryService;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping("/external")
public class HyzzThirdTaskController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(HyzzThirdTaskController.class);
    @Autowired
    private ThirdTaskRetryService retryScheduleService;

    /**
     * 三方平台调用外部系统补偿任务
     *
     * @param shardIndex
     * @param shardTotal
     * @return
     */
    @GetMapping("/retryTask")
    public APIResponse retryTask(@RequestParam("shardIndex") Integer shardIndex, @RequestParam("shardTotal") Integer shardTotal) {
        logger.info("------------三方接口补偿任务开始------------");
        retryScheduleService.taskRetry(shardIndex, shardTotal);
        logger.info("------------三方接口补偿任务结束------------");
        return APIResponse.success();
    }

}
