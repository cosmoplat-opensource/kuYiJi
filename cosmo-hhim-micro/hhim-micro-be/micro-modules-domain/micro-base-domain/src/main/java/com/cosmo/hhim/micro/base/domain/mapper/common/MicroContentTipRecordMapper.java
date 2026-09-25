/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.tips.MicroContentTipRecord;
import com.cosmo.hhim.micro.base.domain.entity.tips.MicroContentTipRecordParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 内容提示记录Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-03
 */
public interface MicroContentTipRecordMapper {
    /**
     * 查询内容提示记录
     *
     * @param userId 内容提示记录ID
     * @return 内容提示记录
     */
    MicroContentTipRecord selectLastMicroContentTipRecord(@Param("userId") Long userId, @Param("userType") String userType, @Param("tipConfigId") Long tipConfigId); 

    /**
     * 查询内容提示记录列表
     *
     * @param param
     * @return 内容提示记录集合
     */
    List<MicroContentTipRecord> selectMicroContentTipRecordList(MicroContentTipRecordParam param);

    /**
     * 新增内容提示记录
     *
     * @param microContentTipRecord 内容提示记录
     * @return 结果
     */
    public int insertMicroContentTipRecord(MicroContentTipRecord microContentTipRecord);

    /**
     * 修改内容提示记录
     *
     * @param microContentTipRecord 内容提示记录
     * @return 结果
     */
    public int updateMicroContentTipRecord(MicroContentTipRecord microContentTipRecord);

    /**
     * 删除内容提示记录
     *
     * @param id 内容提示记录ID
     * @return 结果
     */
    public int deleteMicroContentTipRecordById(Long id);

    /**
     * 批量删除内容提示记录
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroContentTipRecordByIds(Long[] ids);
}
