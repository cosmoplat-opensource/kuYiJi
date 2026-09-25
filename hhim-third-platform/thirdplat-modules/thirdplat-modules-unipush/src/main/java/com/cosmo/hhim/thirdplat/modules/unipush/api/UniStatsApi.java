/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.api;

import com.cosmo.hhim.thirdplat.modules.unipush.response.UniPushResponseResult;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.stats.StatsResult;
import com.cosmo.hhim.thirdplat.modules.unipush.proxy.annotation.UniPushResponseException;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.http.ForestResponse;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description 统计API
 * @createTime 2021-09-22
 */
@BaseRequest(
        baseURL = "${uniBaseUrl}${uniAppId}",
        headers = {
                "Accept-Charset: ${uniEncoding}",
                "Content-Type: ${uniContentType}"
        }
)
@UniPushResponseException
public interface UniStatsApi {

    /**
     * 【推送】获取推送结果
     * 查询推送数据，可查询消息可下发数、下发数，接收数、展示数、点击数等结果。支持单个taskId查询和多个taskId查询。
     *
     * @param taskIds 任务id，推送时返回，多个taskId以英文逗号隔开，一次最多传200个
     * @return
     */
    @Get(url = "/report/push/task/${taskids}")
    ForestResponse<UniPushResponseResult<Map<String, Map<String, StatsResult>>>> queryPushResult(@Var("taskids") String taskIds);


}
