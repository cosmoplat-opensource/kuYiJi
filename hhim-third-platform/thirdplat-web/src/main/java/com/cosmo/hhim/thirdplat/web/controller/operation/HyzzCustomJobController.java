/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.operation;

import java.text.ParseException;
import java.util.List;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomJob;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzCustomJobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;
import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;

/**
 * 【自定义定时任务】Controller
 * 
 * @author cosmo-hhim-open Team
 * @date 2021-12-06
 */
@Api
@RestController
@RequestMapping("/job")
public class HyzzCustomJobController extends BaseController
{

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IHyzzCustomJobService hyzzCustomJobService;

    /**
     * 查询【自定义定时任务】列表
     */
    @ApiOperation(value="查询【自定义定时任务】列表")
    @GetMapping("/list")
    public APIResponse<TableDataInfo> list(HyzzCustomJob hyzzCustomJob)
    {
        startPage();
        List<HyzzCustomJob> list = hyzzCustomJobService.selectHyzzCustomJobList(hyzzCustomJob);
        return APIResponse.success(getDataTable(list));
    }



    /**
     * 获取【自定义定时任务】详细信息
     */
    @ApiOperation(value="获取【自定义定时任务】详细信息")
    @GetMapping(value = "/{id}")
    public APIResponse<HyzzCustomJob> getInfo(@ApiParam(name="id",value="【请填写功能名称】ID") @PathVariable("id")  Long id)
    {
        return APIResponse.success(hyzzCustomJobService.selectHyzzCustomJobById(id));
    }

    /**
     * 新增【自定义定时任务】
     */
    @ApiOperation(value="新增【自定义定时任务】")
    @PostMapping
    public APIResponse<HyzzCustomJob> add(@RequestBody HyzzCustomJob hyzzCustomJob) throws ParseException {
        hyzzCustomJobService.insertHyzzCustomJob(hyzzCustomJob);
        return APIResponse.success(hyzzCustomJob);
    }

    /**
     * 修改【自定义定时任务】
     */
    @ApiOperation(value="修改【自定义定时任务】")
    @PutMapping
    public APIResponse<Integer> edit(@RequestBody HyzzCustomJob hyzzCustomJob) throws ParseException {
        return APIResponse.success(hyzzCustomJobService.updateHyzzCustomJob(hyzzCustomJob));
    }

    /**
     * 删除【自定义定时任务】
     */
    @ApiOperation(value="删除【自定义定时任务】")
	@DeleteMapping("/{ids}")
    public APIResponse<Integer> remove(@ApiParam(name="id",value="【请填写功能名称】ID")  @PathVariable Long[] ids)
    {
        return APIResponse.success(hyzzCustomJobService.deleteHyzzCustomJobByIds(ids));
    }


    /**
     * 修改【自定义定时任务】
     */
    @ApiOperation(value="批量修改【自定义定时任务】")
    @PutMapping("/edits")
    public APIResponse<Integer> edits(@RequestBody List<HyzzCustomJob> hyzzCustomJob) throws ParseException {
        return APIResponse.success(hyzzCustomJobService.updateHyzzCustomJob(hyzzCustomJob));
    }
}
