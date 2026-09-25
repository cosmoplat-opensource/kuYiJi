/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation;

import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomJob;
import com.cosmo.hhim.thirdplat.api.operation.factory.RemoteJobFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自定义定时任务
 *
 * @author cosmo-hhim-open Team
 * @version 1.0
 * @restApi feign接口类上要加上这个标签, 否则无法抽取
 */
@FeignClient(contextId = "RemoteJobService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteJobFallbackFactory.class)
public interface RemoteJobService {

    /**
     * 查询【自定义定时任务】列表
     *
     * @param hyzzCustomJob
     * @return
     */

    @GetMapping("/job/list")
    public APIResponse<TableDataInfo> list(HyzzCustomJob hyzzCustomJob);

    /**
     * 获取【自定义定时任务】详细信息
     */
    @GetMapping(value = "/job/{id}")
    public APIResponse<HyzzCustomJob> getInfo(@ApiParam(name = "id", value = "【请填写功能名称】ID") @PathVariable("id") Long id);


    /**
     * 新增【自定义定时任务】
     */
    @ApiOperation(value = "新增【自定义定时任务】")
    @PostMapping("/job")
    public APIResponse<HyzzCustomJob> add(@RequestBody HyzzCustomJob hyzzCustomJob);

    /**
     * 修改【自定义定时任务】
     */
    @ApiOperation(value = "修改【自定义定时任务】")
    @PutMapping("/job")
    public APIResponse<Integer> edit(@RequestBody HyzzCustomJob hyzzCustomJob);

    /**
     * 删除【自定义定时任务】
     */
    @ApiOperation(value = "删除【自定义定时任务】")
    @DeleteMapping("/job/{ids}")
    public APIResponse<Integer> remove(@ApiParam(name = "id", value = "【请填写功能名称】ID") @PathVariable Long[] ids);


    /**
     * 批量修改【自定义定时任务】
     */
    @ApiOperation(value = "修改【自定义定时任务】")
    @PutMapping("/job/edits")
    public APIResponse<Integer> edit(@RequestBody List<HyzzCustomJob> hyzzCustomJob);

}
