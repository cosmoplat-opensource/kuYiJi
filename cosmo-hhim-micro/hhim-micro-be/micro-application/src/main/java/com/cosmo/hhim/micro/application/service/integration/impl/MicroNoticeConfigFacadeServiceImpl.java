/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.integration.impl;

import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.integration.MicroNoticeConfigDetailInfoResult;
import com.cosmo.hhim.micro.application.dto.integration.MicroNoticeConfigEditParam;
import com.cosmo.hhim.micro.application.service.integration.IMicroNoticeConfigFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroRoleService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.micro.infrastructure.util.MapStrUtil;
import com.cosmo.hhim.micro.integration.domain.entity.MicroNoticeConfig;
import com.cosmo.hhim.micro.integration.domain.service.IMicroNoticeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/6/8
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MicroNoticeConfigFacadeServiceImpl implements IMicroNoticeConfigFacadeService {

    private final IMicroNoticeConfigService microNoticeConfigService;
    private final IMicroUserService microUserService;
    private final IMicroRoleService microRoleService;


    /**
     * 根据业务标识查询通知配置
     *
     * @param businessSign
     * @return
     */
    @Override
    public MicroNoticeConfigDetailInfoResult selectMicroNoticeConfigByBusinessSign(NotifyEnums.NoticeBusinessSignEnum businessSign) {
        MicroNoticeConfigDetailInfoResult result = new MicroNoticeConfigDetailInfoResult();
        MicroNoticeConfig microNoticeConfig = microNoticeConfigService.selectMicroNoticeConfigByBusinessSign(businessSign);
        if (null == microNoticeConfig) {
            return result;
        }
        BeanUtils.copyProperties(microNoticeConfig, result);

        // 通知用户信息查询
        if (StringUtils.hasText(microNoticeConfig.getNoticeUsers())) {
            List<String> userIds = Arrays.asList(microNoticeConfig.getNoticeUsers().split(","));
            List<MicroUser> microUserList = userIds.stream().map(Long::parseLong).distinct()
                    .map(microUserService::selectMicroUserById)
                    .filter(Objects::nonNull).collect(Collectors.toList());
            result.setNoticeUserList(microUserList);
        }

        // 通知角色信息查询
        if (StringUtils.hasText(microNoticeConfig.getNoticeRoles())) {
            List<String> roleCodes = Arrays.asList(microNoticeConfig.getNoticeRoles().split(","));
            List<MicroRole> noticeRoleList = roleCodes.stream().map(microRoleService::selectMicroRoleByRoleCode)
                    .filter(Objects::nonNull).collect(Collectors.toList());
            result.setNoticeRoleList(noticeRoleList);
        }

        // 通知渠道信息查询
        if (StringUtils.hasText(microNoticeConfig.getNoticeChannels())) {
            List<String> channels = Arrays.asList(microNoticeConfig.getNoticeChannels().split(","));
            List<NotifyEnums.NoticeChannelEnum> noticeChannelEnumList = channels.stream()
                    .map(NotifyEnums.NoticeChannelEnum::getEnum)
                    .filter(Objects::nonNull).collect(Collectors.toList());
            result.setNoticeChannelEnumList(noticeChannelEnumList);
        }

        // 通知业务条件
        if (StringUtils.hasText(microNoticeConfig.getNoticeConditions())) {
            result.setNoticeConditionMap(MapStrUtil.stringToMap(microNoticeConfig.getNoticeConditions()));
        }

        return result;
    }

    /**
     * 更新通知配置
     *
     * @param editParam
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMicroNoticeConfig(MicroNoticeConfigEditParam editParam) {
        String businessSign = editParam.getBusinessSign().getCode();
        String appSign = SecurityUtils.getApplicationSign();

        MicroNoticeConfig updateParam = new MicroNoticeConfig();
        updateParam.setBusinessSign(businessSign);
        updateParam.setAppSign(appSign);

        // 通知用户
        if (!CollectionUtils.isEmpty(editParam.getNoticeUserIdList())) {
            String noticeUsers = StringUtils.collectionToDelimitedString(editParam.getNoticeUserIdList(), ",");
            updateParam.setNoticeUsers(noticeUsers);
        } else {
            updateParam.setNoticeUsers(null);
        }

        // 通知角色
        if (!CollectionUtils.isEmpty(editParam.getNoticeRoleCodeList())) {
            String noticeRoles = StringUtils.collectionToDelimitedString(editParam.getNoticeRoleCodeList(), ",");
            updateParam.setNoticeRoles(noticeRoles);
        } else {
            updateParam.setNoticeRoles(null);
        }

        // 通知渠道
        if (!CollectionUtils.isEmpty(editParam.getNoticeChannelList())) {
            List<String> noticeChannelCodes = editParam.getNoticeChannelList().stream().map(NotifyEnums.NoticeChannelEnum::getCode).collect(Collectors.toList());
            String noticeChannels = StringUtils.collectionToDelimitedString(noticeChannelCodes, ",");
            updateParam.setNoticeChannels(noticeChannels);
        } else {
            updateParam.setNoticeChannels(null);
        }

        microNoticeConfigService.updateMicroNoticeConfig(updateParam);
    }
}
