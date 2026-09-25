/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroTags;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroTagsMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTagsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2022-11-28
 */
@Service
public class MicroTagsServiceImpl implements IMicroTagsService {


    @Autowired
    private MicroTagsMapper microTagsMapper;

    /**
     * 查询标签列表
     *
     * @param microTags 标签
     * @return 标签
     */
    @Override
    public List<MicroTags> selectMicroTagsList(MicroTags microTags) {
        return microTagsMapper.selectMicroTagsList(microTags);
    }

}
