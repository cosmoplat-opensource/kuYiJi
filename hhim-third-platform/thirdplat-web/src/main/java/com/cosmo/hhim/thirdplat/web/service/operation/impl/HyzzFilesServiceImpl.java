/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation.impl;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFiles;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzFilesMapper;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzFilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 附件Service业务层处理
 *
 * @author cosmo-hhim-open Team
 */
@Service
public class HyzzFilesServiceImpl implements IHyzzFilesService
{
    @Autowired
    private HyzzFilesMapper hyzzFilesMapper;

    /**
     * 查询附件
     *
     * @param rowId 附件ID
     * @return 附件
     */
    @Override
    public HyzzFiles selectHyzzFilesById(Long rowId)
    {
        return hyzzFilesMapper.selectHyzzFilesById(rowId);
    }

    /**
     * 查询附件列表
     *
     * @param hyzzFiles 附件
     * @return 附件
     */
    @Override
    public List<HyzzFiles> selectHyzzFilesList(HyzzFiles hyzzFiles)
    {
        return hyzzFilesMapper.selectHyzzFilesList(hyzzFiles);
    }

    /**
     * 新增附件
     *
     * @param hyzzFiles 附件
     * @return 结果
     */
    @Override
    public int insertHyzzFiles(HyzzFiles hyzzFiles)
    {
        hyzzFiles.setCreateTime(DateUtils.getNowDate());
        return hyzzFilesMapper.insertHyzzFiles(hyzzFiles);
    }

    /**
     * 修改附件
     *
     * @param hyzzFiles 附件
     * @return 结果
     */
    @Override
    public int updateHyzzFiles(HyzzFiles hyzzFiles)
    {
        hyzzFiles.setUpdateTime(DateUtils.getNowDate());
        return hyzzFilesMapper.updateHyzzFiles(hyzzFiles);
    }

    /**
     * 批量删除附件
     *
     * @param rowIds 需要删除的附件ID
     * @return 结果
     */
    @Override
    public int deleteHyzzFilesByIds(Long[] rowIds)
    {
        return hyzzFilesMapper.deleteHyzzFilesByIds(rowIds);
    }

    /**
     * 删除附件信息
     *
     * @param rowId 附件ID
     * @return 结果
     */
    @Override
    public int deleteHyzzFilesById(Long rowId)
    {
        return hyzzFilesMapper.deleteHyzzFilesById(rowId);
    }

    /**
     * 查询最新的附件信息
     * @param tableName
     * @return
     */
    @Override
    public HyzzFiles selectHyzzFilesListByTable(String tableName) {
        return hyzzFilesMapper.selectHyzzFilesListByTable(tableName);
    }

    @Override
    public int deleteByTableNameAndId(String fileTableName, String fileTableId) {
        return hyzzFilesMapper.deleteByTableNameAndId(fileTableName,fileTableId);
    }

    /**
     * 根据主键和表名获取附件信息
     * @param id
     * @param fileTableName
     * @return
     */
    @Override
    public List<HyzzFiles> selectHyzzFilesListByTableAndId(Long id, String fileTableName) {
        HyzzFiles hyzzFiles=new HyzzFiles();
        hyzzFiles.setFileTableId(id.toString());
        hyzzFiles.setFileTableName(fileTableName);
        return hyzzFilesMapper.selectHyzzFilesList(hyzzFiles);
    }
}
