/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.service.impl;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.micro.integration.domain.entity.MicroWechatPushRecord;
import com.cosmo.hhim.micro.integration.domain.mapper.MicroWechatPushRecordMapper;
import com.cosmo.hhim.micro.integration.domain.service.IMicroWechatPushRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 微信消息推送记录Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-23
 */
@Service
public class MicroWechatPushRecordServiceImpl implements IMicroWechatPushRecordService {
    @Autowired
    private MicroWechatPushRecordMapper microWechatPushRecordMapper;

    /**
     * 查询微信消息推送记录
     *
     * @param id 微信消息推送记录ID
     * @return 微信消息推送记录
     */
    @Override
    public MicroWechatPushRecord selectMicroWechatPushRecordById(Long id) {
        return microWechatPushRecordMapper.selectMicroWechatPushRecordById(id);
    }

    /**
     * 查询微信消息推送记录列表
     *
     * @param microWechatPushRecord 微信消息推送记录
     * @return 微信消息推送记录
     */
    @Override
    public List<MicroWechatPushRecord> selectMicroWechatPushRecordList(MicroWechatPushRecord microWechatPushRecord) {
        return microWechatPushRecordMapper.selectMicroWechatPushRecordList(microWechatPushRecord);
    }

    /**
     * 新增微信消息推送记录
     *
     * @param microWechatPushRecord 微信消息推送记录
     * @return 结果
     */
    @Override
    public int insertMicroWechatPushRecord(MicroWechatPushRecord microWechatPushRecord) {
        microWechatPushRecord.setCreateTime(DateUtils.getNowDate());
        return microWechatPushRecordMapper.insertMicroWechatPushRecord(microWechatPushRecord);
    }

    /**
     * 修改微信消息推送记录
     *
     * @param microWechatPushRecord 微信消息推送记录
     * @return 结果
     */
    @Override
    public int updateMicroWechatPushRecord(MicroWechatPushRecord microWechatPushRecord) {
        return microWechatPushRecordMapper.updateMicroWechatPushRecord(microWechatPushRecord);
    }
}
