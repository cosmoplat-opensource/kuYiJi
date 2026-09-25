/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroTags;

import java.util.List;

/**
 * 标签Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-11-28
 */
public interface MicroTagsMapper {

    /**
     * 查询标签列表
     *
     * @param microTags 标签
     * @return 标签集合
     */
    List<MicroTags> selectMicroTagsList(MicroTags microTags);

    int insertMicroTagBatch(List<MicroTags> list); 

}
