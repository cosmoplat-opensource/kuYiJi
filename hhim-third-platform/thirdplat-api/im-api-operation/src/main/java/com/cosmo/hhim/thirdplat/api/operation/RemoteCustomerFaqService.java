/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqPairRecordEntity;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqQueryEntity;
import com.cosmo.hhim.thirdplat.api.operation.factory.RemoteCustomerFaqFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
@FeignClient(contextId = "remoteCustomerFaqService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteCustomerFaqFallbackFactory.class)
public interface RemoteCustomerFaqService {


    /**
     * 根据索引过滤对应角色的问题列表
     * roleCodeList
     * keyword
     */
    @PostMapping("/operation/api/faq/listByRoleOrIndex")
    APIResponse<List<HyzzFaqPairRecordEntity>> listByRoleOrIndex(@RequestBody HyzzFaqQueryEntity queryEntity);

    /**
     * 根据角色获取当前的索引值
     */
    @PostMapping(value = "/operation/api/faq/getIndexByRole")
    APIResponse<List<String>> getIndexByRole(@RequestBody HyzzFaqQueryEntity entity);


    /**
     * 根据问题ID获取对应答案
     */
    @GetMapping("/operation/api/faq/getFixedAnswer")
    APIResponse<HyzzFaqPairRecordEntity> getFixedAnswer(@RequestParam Long qId);

    /**
     * 根据用户输入的内容获取最相近的答案
     */
    @PostMapping("/operation/api/faq/postQuestion")
    APIResponse<HyzzFaqPairRecordEntity> postQuestion(@RequestBody HyzzFaqQueryEntity entity);

    /**
     * 对用户的内容进行分词
     */
    @PostMapping("/operation/api/faq/segmentContent")
    APIResponse<List<HyzzFaqPairRecordEntity>> segmentContent(@RequestBody HyzzFaqQueryEntity entity);
}
