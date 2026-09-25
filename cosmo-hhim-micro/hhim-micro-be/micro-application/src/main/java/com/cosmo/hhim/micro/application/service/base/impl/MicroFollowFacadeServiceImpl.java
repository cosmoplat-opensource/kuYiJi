/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.base.MicroFollowAssembler;
import com.cosmo.hhim.micro.application.dto.base.MicroFollowDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroFollowFacadeService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroFollowService;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.enums.FollowTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
@Service
public class MicroFollowFacadeServiceImpl implements IMicroFollowFacadeService {
    @Autowired
    private IMicroFollowService followService;

    /**
     * 获取关注对象列表
     *
     * @param followType
     * @return
     */
    @Override
    public List<MicroSelectEntity> findFollow(String followType) {
        validFollowType(followType);
        Long selfId = SecurityUtils.getUserId();
        return followService.selectFollow(selfId, followType);
    }

    /**
     * 关注某对象
     *
     * @param followDTO
     * @return
     */
    @Override
    public int follow(MicroFollowDTO followDTO) {
        validFollowType(followDTO.getFollowType());
        Long selfId = SecurityUtils.getUserId();
        String customerCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        if (CollectionUtils.isEmpty(followDTO.getFollowIds())) {
            return followService.unfollowed(Collections.emptyList(), selfId, followDTO.getFollowType());
        } else {
            return followService.followSomeone(MicroFollowAssembler.toFollowList(followDTO, selfId, customerCode));
        }
    }

    /**
     * 取消关注某对象
     *
     * @param followDTO
     * @return
     */
    @Override
    public int unfollowed(MicroFollowDTO followDTO) {
        validFollowParams(followDTO);
        Long selfId = SecurityUtils.getUserId();
        return followService.unfollowed(followDTO.getFollowIds(), selfId, followDTO.getFollowType());
    }

    private void validFollowParams(MicroFollowDTO followDTO) {
        if (CollectionUtils.isEmpty(followDTO.getFollowIds())) {
            throw new CustomException("无法获取要关注的对象");
        }
        validFollowType(followDTO.getFollowType());
    }

    private static void validFollowType(String followType) {
        if (StringUtils.isEmpty(followType) || FollowTypeEnum.getEnum(followType) == null) {
            throw new CustomException("无法获取要关注的类别");
        }
    }

}
