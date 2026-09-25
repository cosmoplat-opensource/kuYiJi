/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroRecommendHit;

import java.util.List;

/**
 * 推荐数据命中信息表Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-10-26
 */
public interface MicroRecommendHitMapper {

    /**
     * 查询推荐数据命中信息表列表
     *
     * @param microRecommendHit 推荐数据命中信息表
     * @return 推荐数据命中信息表集合
     */
    List<MicroRecommendHit> selectMicroRecommendHitList(MicroRecommendHit microRecommendHit);

    /**
     * 新增推荐数据命中信息表
     *
     * @param microRecommendHit 推荐数据命中信息表
     * @return 结果
     */
    int insertMicroRecommendHit(MicroRecommendHit microRecommendHit);

    int insertMicroRecommendHitBatch(List<MicroRecommendHit> list); 

    /**
     * 清理数据
     *
     * @return
     */
    int deleteAllMicroRecommendHit();


}
