/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroTags;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTagsService;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectBaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2022-11-28
 */
@RestController
@RequestMapping("/tags")
public class MicroTagsController {

    @Autowired
    private IMicroTagsService microTagsService;

    /**
     * 查询标签列表
     */
    @GetMapping("/select")
    public AjaxResult list(@RequestParam String key, @RequestParam String type) {
        MicroTags microTags = new MicroTags();
        microTags.setTagName(key);
        microTags.setTagType(type);
        microTags.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
        List<MicroTags> data = microTagsService.selectMicroTagsList(microTags);
        if (CollectionUtils.isEmpty(data)) {
            return AjaxResult.success();
        } else {
            MicroSelectBaseEntity select;
            List<MicroSelectBaseEntity> selectList = new ArrayList<>();
            for (MicroTags tag : data) {
                select = new MicroSelectBaseEntity();
                select.setItemCode(tag.getTagName());
                select.setItemName(tag.getTagName());
                selectList.add(select);
            }
            return AjaxResult.success(selectList);
        }
    }
}
