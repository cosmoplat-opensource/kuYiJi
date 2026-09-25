/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.follow.MicroFollowEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 关注Mapper接口
 *
 * @author cosmo-hhim-open Team
 */
public interface MicroFollowMapper {

    int insertFollow(List<MicroFollowEntity> list); 

    int deleteFollow(@Param("followIds") List<Long> followIds, @Param("selfId") Long selfId, @Param("followType") String followType); 

    List<MicroSelectEntity> selectListBySelfId(@Param("selfId") Long selfId, @Param("followType") String followType); 

}
