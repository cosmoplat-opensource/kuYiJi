/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation;

import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzPortalSuggestion;
import com.cosmo.hhim.thirdplat.api.operation.factory.RemoteCustomerSuggestFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-10-26
 */
@FeignClient(contextId = "remoteCustomerSuggestService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteCustomerSuggestFallbackFactory.class)
public interface RemoteCustomerSuggestService {


    /**
     * 查询意见反馈列表
     *
     * @param hyzzPortalSuggestion
     * @return
     */

    @GetMapping("/operation/api/suggestion/list")
    public APIResponse<TableDataInfo> list(@SpringQueryMap HyzzPortalSuggestion hyzzPortalSuggestion);

    /**
     * 获取意见反馈详细信息
     */
    @GetMapping(value = "/operation/api/suggestion/{id}")
    public APIResponse<HyzzPortalSuggestion> getInfo(@ApiParam(name = "id", value = "【请填写功能名称】ID") @PathVariable("id") Long id);

    /**
     * 查询意见反馈列表(以问题/回答方式返回)
     */
    @GetMapping("/operation/api/suggestion/chatHistory")
    APIResponse<TableDataInfo> chatHistory(HyzzPortalSuggestion hyzzPortalSuggestion);

    /**
     * 新增意见反馈
     */
    @ApiOperation(value = "新增【意见反馈】")
    @PostMapping("/operation/api/suggestion")
    public APIResponse<HyzzPortalSuggestion> add(@RequestBody HyzzPortalSuggestion hyzzPortalSuggestion);

}
