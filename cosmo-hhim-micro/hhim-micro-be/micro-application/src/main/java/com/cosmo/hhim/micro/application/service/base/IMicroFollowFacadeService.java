/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.micro.application.dto.base.MicroFollowDTO;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
public interface IMicroFollowFacadeService {
    List<MicroSelectEntity> findFollow(String followType); 

    int follow(MicroFollowDTO followDTO); 

    int unfollowed(MicroFollowDTO followDTO); 

}
