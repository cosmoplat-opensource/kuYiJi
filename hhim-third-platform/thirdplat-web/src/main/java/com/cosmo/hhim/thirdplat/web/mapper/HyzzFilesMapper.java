/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFiles;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 附件Mapper接口
 *
 * @author cosmo-hhim-open Team
 */
public interface HyzzFilesMapper
{
    /**
     * 查询附件
     *
     * @param rowId 附件ID
     * @return 附件
     */
    public HyzzFiles selectHyzzFilesById(Long rowId);

    /**
     * 查询附件列表
     *
     * @param hyzzFiles 附件
     * @return 附件集合
     */
    public List<HyzzFiles> selectHyzzFilesList(HyzzFiles hyzzFiles);

    /**
     * 新增附件
     *
     * @param hyzzFiles 附件
     * @return 结果
     */
    public int insertHyzzFiles(HyzzFiles hyzzFiles);

    /**
     * 修改附件
     *
     * @param hyzzFiles 附件
     * @return 结果
     */
    public int updateHyzzFiles(HyzzFiles hyzzFiles);

    /**
     * 删除附件
     *
     * @param rowId 附件ID
     * @return 结果
     */
    public int deleteHyzzFilesById(Long rowId);

    /**
     * 批量删除附件
     *
     * @param rowIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteHyzzFilesByIds(Long[] rowIds);

    /**
     *查询最新的附件信息
     * @param tableName
     * @return
     */
    HyzzFiles selectHyzzFilesListByTable(String tableName);


    public int deleteByTableNameAndId(@Param("fileTableName") String fileTableName, @Param("fileTableId") String fileTableId);
}
