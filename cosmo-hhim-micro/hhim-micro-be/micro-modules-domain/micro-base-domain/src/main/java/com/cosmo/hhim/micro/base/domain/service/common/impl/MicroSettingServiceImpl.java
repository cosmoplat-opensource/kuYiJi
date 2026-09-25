/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroAppConfig;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroAppInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroExperienceGuideNodeMapper;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IAppConfigService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroSettingService;
import com.cosmo.hhim.micro.infrastructure.enums.RoleCodeEnum;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * decouple-from-ops-platform：不再依赖 im-api-operation
 *  - "开启正式使用" 改为写本地 micro_app_config
 *  - "获取正式使用标识" 改为读本地 micro_app_config.status
 *  - "获取租户应用" 改为读本地 micro_app_config
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class MicroSettingServiceImpl implements IMicroSettingService {

    private final MicroUserMapper microUserMapper;
    private final MicroExperienceGuideNodeMapper microExperienceGuideNodeMapper;
    private final IAppConfigService appConfigService;

    @Value("${applicationProductIds.micro_process}")
    private String appId;

    @Value("#{'${settings.cleanTableNames}'.split(',')}")
    private List<String> cleanTableNames;

    /**
     * 开启正式使用：单库场景下视为"启用应用"
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean officialUse(boolean clearData) {
        // 1.管理员校验
        List<MicroRole> microRoles = microUserMapper.selectRoleInfoByUserId(SecurityUtils.getUserId());
        if (CollectionUtils.isEmpty(microRoles)) {
            throw new CustomException("当前用户不存在！");
        }
        List<MicroRole> collect = microRoles.stream()
                .filter(e -> e.getRoleCode().equals(RoleCodeEnum.MANAGER.getCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            throw new CustomException("当前用户无权操作！");
        }

        // 2.把对应 app_code 的 status 改为 1（启用）
        MicroAppConfig cfg = appConfigService.getByAppCode(appId);
        if (cfg == null) {
            throw new CustomException("本地应用配置不存在：appCode=" + appId);
        }
        if (cfg.getStatus() != null && cfg.getStatus() == 1) {
            throw new CustomException("已开启正式使用，无需重复开启！");
        }
        cfg.setStatus(1);
        appConfigService.updateById(cfg);

        // 3.清理业务表数据
        if (clearData && !CollectionUtils.isEmpty(cleanTableNames)) {
            for (String cleanTableName : cleanTableNames) {
                // 表名白名单校验：仅允许字母/数字/下划线，防止配置注入任意 SQL
                if (!cleanTableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                    throw new CustomException("非法表名，禁止动态删除: " + cleanTableName);
                }
                log.info("清理表：{}", cleanTableName);
                microExperienceGuideNodeMapper.dynamicDeleteData(cleanTableName);
            }
        }
        return true;
    }

    /**
     * 获取正式使用标识
     */
    @Override
    public Boolean getOfficialUseFlag() {
        String tenantCode = (String) ThreadContext.get(com.cosmo.hhim.common.core.constant.Constants.TARGET_CUSTOMER);
        MicroAppConfig cfg = appConfigService.getByAppCode(appId);
        if (cfg == null) {
            return false;
        }
        log.debug("租户 [{}] 正式使用标识 = status:{}", tenantCode, cfg.getStatus());
        return cfg.getStatus() != null && cfg.getStatus() == 1;
    }

    /**
     * 获取租户所有应用信息（读本地 micro_app_config）
     */
    @Override
    public List<MicroAppInfo> getAllAppInfosForCurrentTenant() {
        List<MicroAppInfo> resultList = Lists.newArrayList();
        MicroAppConfig query = new MicroAppConfig();
        query.setStatus(1);
        List<MicroAppConfig> apps = appConfigService.list(query);
        for (MicroAppConfig applicationConfig : apps) {
            MicroAppInfo microAppInfo = new MicroAppInfo();
            microAppInfo.setAppCode(applicationConfig.getAppCode());
            microAppInfo.setAppSign(applicationConfig.getAppSign());
            resultList.add(microAppInfo);
        }
        return resultList;
    }
}
