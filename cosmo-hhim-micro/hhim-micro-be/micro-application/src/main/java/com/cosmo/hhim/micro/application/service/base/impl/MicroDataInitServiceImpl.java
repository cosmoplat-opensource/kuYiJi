/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.micro.application.service.base.IMicroDataInitService;
import com.cosmo.hhim.micro.base.domain.entity.common.DataSourceEntity;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessBuyInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserMapper;
import com.cosmo.hhim.micro.base.domain.util.DBUtils;
import com.cosmo.hhim.micro.infrastructure.config.DataSourceConfig;
import com.cosmo.hhim.micro.infrastructure.enums.ActiveFlagEnum;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.micro.infrastructure.util.SSOUtils;
import com.cosmo.hhim.micro.integration.domain.entity.MicroNoticeConfig;
import com.cosmo.hhim.micro.integration.domain.service.IMicroNoticeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MicroDataInitServiceImpl implements IMicroDataInitService {

    private final DataSourceConfig dataSourceConfig;
    private final IMicroNoticeConfigService microNoticeConfigService;
    private final MicroUserMapper microUserMapper;

    private static final String OPERATOR = "system-sync";

    /**
     * 初始化DB数据
     */
    @Override
    public void initDBData(final String ssoInitDataSqlUrl, final String originalCustomerCode) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("initDataSqlTmpFile", ".sql");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, true));
                 InputStream in = new FileInputStream(tmpFile)) {
                SSOUtils.readUrl(ssoInitDataSqlUrl, originalCustomerCode, (String) ThreadContext.get(Constants.TARGET_CUSTOMER), writer);
                writer.flush();
                DataSourceEntity dataSource = new DataSourceEntity();
                BeanUtils.copyProperties(dataSourceConfig, dataSource);
                DBUtils.execSqlFileByMysql(in, dataSource);
            }
            if (tmpFile != null && !tmpFile.delete()) {
                log.error("临时文件删除失败!");
            }
            log.info("数据初始化完成！");
        } catch (Exception e) {
            log.error("执行初始化sql脚本失败", e);
        }
    }

    /**
     * 初始化业务配置
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void initBusinessConfig(final MicroProcessBuyInfo microProcessBuyInfo, final String appSign) {

        // 查询购买人用户信息
        MicroProcessBuyInfo.UserInfo userInfo = microProcessBuyInfo.getData();
        MicroUser microUserResult = microUserMapper.selectUserInfoByUserNameOrPhoneNumber(null, userInfo.getPhonenumber());

        // 1.初始化通知配置

        // 1.1 生产周报通知配置
        MicroNoticeConfig microNoticeConfig = new MicroNoticeConfig();
        microNoticeConfig.setBusinessSign(NotifyEnums.NoticeBusinessSignEnum.PRODUCE_WEEK_REPORT.getCode()); // 通知业务标识：生产周报
        microNoticeConfig.setAppSign(appSign);
        microNoticeConfig.setNoticeChannels(NotifyEnums.NoticeChannelEnum.SMS.getCode()); // 通知渠道：SMS 
        if (null != microUserResult) {
            String userId = String.valueOf(microUserResult.getId());
            microNoticeConfig.setNoticeUsers(userId); // 通知用户：企业管理员ID 
        }
        microNoticeConfig.setActiveFlag(ActiveFlagEnum.NORMAL.getCode());
        microNoticeConfig.setCreateBy(OPERATOR);
        microNoticeConfig.setCreateTime(DateUtils.getNowDate());

        microNoticeConfigService.insertMicroNoticeConfig(microNoticeConfig);
    }
}
