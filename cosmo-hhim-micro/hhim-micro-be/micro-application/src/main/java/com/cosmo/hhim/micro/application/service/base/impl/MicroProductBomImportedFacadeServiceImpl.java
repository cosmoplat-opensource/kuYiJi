/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.service.base.IMicroProductBomImportedFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.CertificateInfoPC;
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBomImportedTemp;
import com.cosmo.hhim.micro.base.domain.entity.task.AsyncImportExportTask;
import com.cosmo.hhim.micro.base.domain.mapper.task.AsyncImportExportTaskMapper;
import com.cosmo.hhim.micro.base.domain.service.bom.IMicroProductBomImportedTempService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.constant.RedisKeys;
import com.cosmo.hhim.micro.infrastructure.enums.AsyncImportExportTaskStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.ImportConfirmFlagEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.cosmo.hhim.micro.base.domain.util.CertificateUtil.extractTaskCodeFromCertificate;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MicroProductBomImportedFacadeServiceImpl implements IMicroProductBomImportedFacadeService {

    private final AsyncImportExportTaskMapper asyncImportExportTaskMapper;
    private final IMicroProductBomImportedTempService microProductBomImportedTempService;
    private final RedisCache redisCache;

    /**
     * 确认导入
     * flag(0:错误数据修复，1:确认导入)
     */
    @Override
    public void importConfirm(String flag) {
        Date nowDate = DateUtils.getNowDate();
        Long userId = SecurityUtils.getUserId();

        String taskCode = extractTaskCodeFromCertificate();
        if(!StringUtils.hasText(taskCode)){
            throw new CustomException("任务编码获取失败！");
        }

        // 1.读取当前租户需要导入的数据
        List<MicroProductBomImportedTemp> importList = microProductBomImportedTempService.selectMicroProductBomImportedTempList(null);
        if (CollectionUtils.isEmpty(importList)) {
            // 更新任务处理状态（已确认）
            AsyncImportExportTask asyncImportExportTask = new AsyncImportExportTask();
            asyncImportExportTask.setTaskCode(taskCode);
            asyncImportExportTask.setTaskStatus(AsyncImportExportTaskStatusEnum.CONFIRMED.getCode());
            asyncImportExportTask.setUpdateTime(nowDate);
            asyncImportExportTaskMapper.updateAsyncImportExportTask(asyncImportExportTask);

            // 删除PC请求临时凭证
            if (ImportConfirmFlagEnum.IMPORTED.getCode().equals(flag)) {
                deletePcRequestCertificate();
            }
            return;
        }

        // TODO 2.

    }


    /**
    /**
     * 取消导入
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importCancel() {
        Date nowDate = DateUtils.getNowDate();

        String taskCode = extractTaskCodeFromCertificate();
        if(!StringUtils.hasText(taskCode)){
            throw new CustomException("任务编码获取失败！");
        }

        // 1.删除导入数据临时表
        List<MicroProductBomImportedTemp> importList = microProductBomImportedTempService.selectMicroProductBomImportedTempList(null);
        if (!CollectionUtils.isEmpty(importList)) {
            deleteImportedTemp(importList);
        }

        // 2.更新任务处理状态（已取消）
        AsyncImportExportTask asyncImportExportTask = new AsyncImportExportTask();
        asyncImportExportTask.setTaskCode(taskCode);
        asyncImportExportTask.setTaskStatus(AsyncImportExportTaskStatusEnum.CANCELED.getCode());
        asyncImportExportTask.setUpdateTime(nowDate);
        asyncImportExportTaskMapper.updateAsyncImportExportTask(asyncImportExportTask);
    }


    /**
     * 删除导入数据的临时表
     *
     * @param importList
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteImportedTemp(List<MicroProductBomImportedTemp> importList) {
        List<Long> importedTempIds = importList.stream().map(MicroProductBomImportedTemp::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(importedTempIds)) {
            Long[] ids = importedTempIds.toArray(new Long[importedTempIds.size()]);
            microProductBomImportedTempService.deleteMicroProductBomImportedTempByIds(ids);
        }
    }

    /**
     * 删除PC请求临时凭证
     */
    private void deletePcRequestCertificate() {
        String certificate = (String) ThreadContext.get(CommonConstants.CERTIFICATE);
        if (StringUtils.hasText(certificate)) {
            String redisKey = RedisKeys.MicroRegion.BASIC_MODULE.value(RedisKeys.IMPORT_CERTIFICATE_CODE + certificate);
            redisCache.deleteObject(redisKey);
        }
    }

}
