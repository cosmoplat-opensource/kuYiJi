/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.follow.MicroFollowEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
public interface IMicroFollowService {

    List<MicroSelectEntity> selectFollow(Long selfId, String followType); 

    /**
     * 关注某些人
     */
    int followSomeone(List<MicroFollowEntity> followEntityList); 

    /**
     * 取关某些人
     */
    int unfollowed(List<Long> ids, Long selfId, String followType); 
}
