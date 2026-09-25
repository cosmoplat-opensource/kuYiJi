/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.follow.MicroFollowEntity;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroFollowMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroFollowService;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.enums.FollowTypeEnum;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Service
public class MicroFollowServiceImpl implements IMicroFollowService {

    @Autowired
    private MicroFollowMapper followMapper;

    @Override
    public List<MicroSelectEntity> selectFollow(Long selfId, String followType) {
        if (selfId == null) {
            selfId = SecurityUtils.getUserId();
        }
        if (StringUtils.isEmpty(followType)) {
            followType = FollowTypeEnum.EMPLOYEE.getCode();
        }
        return followMapper.selectListBySelfId(selfId, followType);
    }

    @Override
    public int followSomeone(List<MicroFollowEntity> followEntities) {
        HashMap<String, List<MicroFollowEntity>> followMap = followEntities.stream().collect(
                Collectors.groupingBy(f -> f.getSelfId() + "," + f.getFollowType(), HashMap::new, Collectors.toList()));
        List<MicroFollowEntity> all = new ArrayList<>();
        String[] split;
        for (Map.Entry<String, List<MicroFollowEntity>> entry : followMap.entrySet()) {
            split = entry.getKey().split(",");
            unfollowed(Collections.emptyList(), Long.parseLong(split[0]), split[1]);
            all.addAll(entry.getValue());
        }
        followMapper.insertFollow(all);
        return 1;
    }

    @Override
    public int unfollowed(List<Long> ids, Long selfId, String followType) {
        return followMapper.deleteFollow(ids, selfId, followType);
    }
}
