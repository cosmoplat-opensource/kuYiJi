/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;


import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqPairRecordEntity;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqQueryEntity;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzInverseIndexEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能客服问答对Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-05
 */
public interface HyzzFaqPairRecordMapper {
    /**
     * 查询智能客服问答对
     *
     * @param id 智能客服问答对ID
     * @return 智能客服问答对
     */
    HyzzFaqPairRecordEntity selectHyzzFaqPairRecordEntityById(Long id);

    /**
     * 查询智能客服问答对列表
     *
     * @param entity 智能客服问答对
     * @return 智能客服问答对集合
     */
    List<HyzzFaqPairRecordEntity> selectFaqListByRoleAndIndex(@Param("entity") HyzzFaqQueryEntity entity);

    /**
     * 根据条件过滤索引
     *
     * @param entity
     * @return
     */
    List<HyzzInverseIndexEntity> filterFaqIndex(@Param("entity") HyzzFaqQueryEntity entity);

}
