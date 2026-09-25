/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.base;

import com.cosmo.hhim.micro.application.dto.base.MicroFollowDTO;
import com.cosmo.hhim.micro.base.domain.entity.follow.MicroFollowEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
public class MicroFollowAssembler {

    public static List<MicroFollowEntity> toFollowList(MicroFollowDTO followDTO, Long selfId, String customerCode) {
        List<MicroFollowEntity> followEntityList = new ArrayList<>();
        MicroFollowEntity follow;
        String followType = followDTO.getFollowType();
        for (Long followId : followDTO.getFollowIds()) {
            follow = new MicroFollowEntity();
            follow.setSelfId(selfId);
            follow.setFollowId(followId);
            follow.setFollowType(followType);
            follow.setTenantCode(customerCode);
            followEntityList.add(follow);
        }
        return followEntityList;
    }
}
