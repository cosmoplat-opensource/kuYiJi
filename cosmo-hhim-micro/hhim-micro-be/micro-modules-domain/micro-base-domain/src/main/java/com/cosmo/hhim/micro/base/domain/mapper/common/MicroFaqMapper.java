/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroFaq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * FAQ Mapper
 *
 * @author cosmo-hhim-open Team
 */
public interface MicroFaqMapper {

    List<MicroFaq> selectByCategory(@Param("category") String category); 

    List<MicroFaq> selectList(MicroFaq query); 

    int insert(MicroFaq record); 

    int updateById(MicroFaq record); 
}
