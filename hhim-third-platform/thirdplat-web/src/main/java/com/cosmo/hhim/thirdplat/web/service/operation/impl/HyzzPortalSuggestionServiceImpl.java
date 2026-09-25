/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation.impl;

import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.bean.BeanUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.thirdplat.api.operation.domain.*;
import com.cosmo.hhim.thirdplat.web.constants.CustomerSuggestStatusEnum;
import com.cosmo.hhim.thirdplat.web.constants.FileTableNameConstants;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzCustomerMapper;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzPortalSuggestionMapper;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzFilesService;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzPortalSuggestionLogService;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzPortalSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 门户的意见反馈Service业务层处理
 *
 * @author cosmo-hhim-open Team
 */
@Service
public class HyzzPortalSuggestionServiceImpl implements IHyzzPortalSuggestionService {
    @Autowired
    private HyzzPortalSuggestionMapper hyzzPortalSuggestionMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IHyzzFilesService hyzzFilesService;

    @Autowired
    private IHyzzPortalSuggestionLogService hyzzPortalSuggestionLogService;

    @Autowired
    private HyzzCustomerMapper hyzzCustomerMapper;

    /**
     * 查询门户的意见反馈
     *
     * @param id 门户的意见反馈ID
     * @return 门户的意见反馈
     */
    @Override
    public HyzzPortalSuggestion selectHyzzPortalSuggestionById(Long id) {
        HyzzPortalSuggestion hyzzPortalSuggestion = hyzzPortalSuggestionMapper.selectHyzzPortalSuggestionById(id);
        //查询附件信息
        List<HyzzFiles> hyzzFiles = hyzzFilesService.selectHyzzFilesListByTableAndId(id, FileTableNameConstants.HYZZ_CUSTOMER_SUGGESTION);
        hyzzPortalSuggestion.setHyzzFiles(hyzzFiles);
        return hyzzPortalSuggestion;
    }

    /**
     * 查询门户的意见反馈列表
     *
     * @param hyzzPortalSuggestion 门户的意见反馈
     * @return 门户的意见反馈
     */
    @Override
    public List<HyzzPortalSuggestion> selectHyzzPortalSuggestionList(HyzzPortalSuggestion hyzzPortalSuggestion) {
        return hyzzPortalSuggestionMapper.selectHyzzPortalSuggestionList(hyzzPortalSuggestion);
    }

    /**
     * 新增门户的意见反馈
     *
     * @param hyzzPortalSuggestion 门户的意见反馈
     * @return 结果
     */
    @Override
    public int insertHyzzPortalSuggestion(HyzzPortalSuggestion hyzzPortalSuggestion) {
        hyzzPortalSuggestion.setCreateTime(DateUtils.getNowDate());
        return hyzzPortalSuggestionMapper.insertHyzzPortalSuggestion(hyzzPortalSuggestion);
    }

    /**
     * 修改门户的意见反馈
     *
     * @param hyzzPortalSuggestion 门户的意见反馈
     * @return 结果
     */
    @Override
    public int updateHyzzPortalSuggestion(HyzzPortalSuggestion hyzzPortalSuggestion) {
        hyzzPortalSuggestion.setUpdateTime(DateUtils.getNowDate());
        return hyzzPortalSuggestionMapper.updateHyzzPortalSuggestion(hyzzPortalSuggestion);
    }

    /**
     * 批量删除门户的意见反馈
     *
     * @param ids 需要删除的门户的意见反馈ID
     * @return 结果
     */
    @Override
    public int deleteHyzzPortalSuggestionByIds(Long[] ids) {
        return hyzzPortalSuggestionMapper.deleteHyzzPortalSuggestionByIds(ids);
    }

    /**
     * 删除门户的意见反馈信息
     *
     * @param id 门户的意见反馈ID
     * @return 结果
     */
    @Override
    public int deleteHyzzPortalSuggestionById(Long id) {
        return hyzzPortalSuggestionMapper.deleteHyzzPortalSuggestionById(id);
    }

    /**
     * 新增意见反馈
     *
     * @param hyzzPortalSuggestion
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addHyzzPortalSuggestion(HyzzPortalSuggestion hyzzPortalSuggestion) {

        HyzzCustomer hyzzCustomer = hyzzCustomerMapper.selectDataByCustomerCode(hyzzPortalSuggestion.getTenantCode());
        if (CheckObjectUtils.isNotEmpty(hyzzCustomer)) {
            hyzzPortalSuggestion.setTenantName(hyzzCustomer.getCustomerName());
        }
        //1、生成意见反馈编码
        hyzzPortalSuggestion.setOrderNo(getOrderNo());
        hyzzPortalSuggestion.setStatus(CustomerSuggestStatusEnum.CREATE.getCode());
        //2、保存信息
        hyzzPortalSuggestionMapper.insertHyzzPortalSuggestion(hyzzPortalSuggestion);
        //3、保存附件
        saveFile(hyzzPortalSuggestion.getHyzzFiles(), hyzzPortalSuggestion.getId());
        //4、保存日志
        HyzzPortalSuggestionLog hyzzPortalSuggestionLog = new HyzzPortalSuggestionLog();
        BeanUtils.copyProperties(hyzzPortalSuggestion, hyzzPortalSuggestionLog);
        hyzzPortalSuggestionLog.setCreateTime(DateUtils.getNowDate());
        hyzzPortalSuggestionLog.setOperateName(CustomerSuggestStatusEnum.CREATE.getName());
        hyzzPortalSuggestionLog.setOperateResult("创建完成");
        hyzzPortalSuggestionLogService.insertHyzzPortalSuggestionLog(hyzzPortalSuggestionLog);

        return 1;
    }

    /**
     * 根据条件获取问答列表
     *
     * @param hyzzPortalSuggestion
     * @return
     */
    @Override
    public List<HyzzFaqHistoryEntity> selectChatHistory(HyzzPortalSuggestion hyzzPortalSuggestion) {
        return hyzzPortalSuggestionMapper.selectQAList(hyzzPortalSuggestion);
    }


    /**
     * 附件保存
     *
     * @param hyzzFiles
     * @param id
     */
    private void saveFile(List<HyzzFiles> hyzzFiles, Long id) {
        if (CheckObjectUtils.isEmpty(hyzzFiles)) {
            return;
        }
        for (HyzzFiles hyzzFile : hyzzFiles) {
            //设置表名
            hyzzFile.setFileTableName(FileTableNameConstants.HYZZ_CUSTOMER_SUGGESTION);
            hyzzFile.setFileTableId(id.toString());
            hyzzFilesService.insertHyzzFiles(hyzzFile);
        }

    }

    private String getOrderNo() {
        String prefixStr = "im-customer-suggestion";
        String codeByDay = redisCache.incrByDay(prefixStr, 1, 6);
        prefixStr = "";
        String result = codeByDay.substring(0, prefixStr.length()) + codeByDay.substring(codeByDay.length() - 12, codeByDay.length() - 6) + "." + codeByDay.substring(codeByDay.length() - 6);
        return result;
    }
}
