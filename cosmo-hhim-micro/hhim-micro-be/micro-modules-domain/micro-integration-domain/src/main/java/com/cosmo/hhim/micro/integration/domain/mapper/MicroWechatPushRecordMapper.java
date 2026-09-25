/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.mapper;


import com.cosmo.hhim.micro.integration.domain.entity.MicroWechatPushRecord;

import java.util.List;

/**
 * 微信消息推送记录Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-22
 */
public interface MicroWechatPushRecordMapper {
    /**
     * 查询微信消息推送记录
     *
     * @param id 微信消息推送记录ID
     * @return 微信消息推送记录
     */
    public MicroWechatPushRecord selectMicroWechatPushRecordById(Long id);

    /**
     * 查询微信消息推送记录列表
     *
     * @param microWechatPushRecord 微信消息推送记录
     * @return 微信消息推送记录集合
     */
    public List<MicroWechatPushRecord> selectMicroWechatPushRecordList(MicroWechatPushRecord microWechatPushRecord);

    /**
     * 新增微信消息推送记录
     *
     * @param microWechatPushRecord 微信消息推送记录
     * @return 结果
     */
    public int insertMicroWechatPushRecord(MicroWechatPushRecord microWechatPushRecord);

    /**
     * 修改微信消息推送记录
     *
     * @param microWechatPushRecord 微信消息推送记录
     * @return 结果
     */
    public int updateMicroWechatPushRecord(MicroWechatPushRecord microWechatPushRecord);

    /**
     * 删除微信消息推送记录
     *
     * @param id 微信消息推送记录ID
     * @return 结果
     */
    public int deleteMicroWechatPushRecordById(Long id);

    /**
     * 批量删除微信消息推送记录
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroWechatPushRecordByIds(Long[] ids);
}
