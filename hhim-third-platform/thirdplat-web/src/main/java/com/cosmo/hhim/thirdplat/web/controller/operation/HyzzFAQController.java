/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.operation;


import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqPairRecordEntity;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqQueryEntity;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzFaqPairRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 门户的意见反馈Controller
 *
 * @author cosmo-hhim-open Team
 */
@RestController
@RequestMapping("/operation/api/faq")
public class HyzzFAQController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IHyzzFaqPairRecordService recordService;

    /**
     * 根据索引过滤对应角色的问题列表
     * roleCodeList
     * keyword
     */
    @PostMapping("/listByRoleOrIndex")
    public APIResponse<List<HyzzFaqPairRecordEntity>> listByRoleOrIndex(@RequestBody HyzzFaqQueryEntity queryEntity) {
        return APIResponse.success(recordService.selectHyzzFaqPairRecordEntityList(queryEntity));
    }


    /**
     * 根据角色获取当前的索引值
     */
    @PostMapping(value = "/getIndexByRole")
    public APIResponse<List<String>> getIndexByRole(@RequestBody HyzzFaqQueryEntity entity) {
        return APIResponse.success(recordService.selectIndexByRoleList(entity));
    }

    /**
     * 根据问题ID获取对应答案
     */
    @GetMapping("getFixedAnswer")
    public APIResponse<HyzzFaqPairRecordEntity> getFixedAnswer(@RequestParam Long qId) {
        return APIResponse.success(recordService.selectHyzzFaqPairRecordEntityByQuestionId(qId));
    }

    /**
     * 根据用户输入的内容获取最相近的答案
     */
    @PostMapping("/postQuestion")
    public APIResponse<HyzzFaqPairRecordEntity> postQuestion(@RequestBody HyzzFaqQueryEntity entity) {
        return APIResponse.success(recordService.getMostSimilarAnswer(entity));
    }

    /**
     * 对用户的内容进行分词
     */
    @PostMapping("/segmentContent")
    public APIResponse<List<HyzzFaqPairRecordEntity>> segmentContent(@RequestBody HyzzFaqQueryEntity entity) {
        return APIResponse.success(recordService.segmentContent(entity));
    }

}
