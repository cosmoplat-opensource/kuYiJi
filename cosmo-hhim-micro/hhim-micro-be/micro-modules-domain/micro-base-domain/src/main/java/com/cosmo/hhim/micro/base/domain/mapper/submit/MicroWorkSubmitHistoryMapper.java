/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.submit;

import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistory;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistoryDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报工记录历史数据库访问层
 *
 * @author cosmo-hhim-open Team
 * @since 2022-10-26 13:56:27
 */
public interface MicroWorkSubmitHistoryMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    MicroWorkSubmitHistory selectMicroWorkSubmitHistoryById(Long id);

    /**
     * 查询报工记录变动历史
     *
     * @param microWorkSubmitHistory
     * @return
     */
    List<MicroWorkSubmitHistory> selectMicroWorkSubmitHistoryList(MicroWorkSubmitHistory microWorkSubmitHistory);

    /**
     * 查询扩展的报工记录变动历史
     *
     * @param microWorkSubmitHistory
     * @return
     */
    List<MicroWorkSubmitHistoryDto> selectMicroWorkSubmitHistoryExList(MicroWorkSubmitHistory microWorkSubmitHistory);

    /**
     * 新增数据
     *
     * @param microWorkSubmitHistory
     * @return 影响行数
     */
    int insertMicroWorkSubmitHistory(MicroWorkSubmitHistory microWorkSubmitHistory);

    /**
     * 批量新增报工历史记录
     *
     * @param microWorkSubmitHistories
     * @return
     */
    int insertMicroWorkSubmitHistoryBatch(@Param("list") List<MicroWorkSubmitHistory> microWorkSubmitHistories);

    /**
     * 修改数据
     *
     * @param microWorkSubmitHistory 实例对象
     * @return 影响行数
     */
    int updateMicroWorkSubmitHistory(MicroWorkSubmitHistory microWorkSubmitHistory);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteMicroWorkSubmitHistoryById(Long id);

    /**
     * 根据报工记录号删除报工历史
     *
     * @param submitNo
     * @return
     */
    int deleteMicroWorkSubmitHistoryBySubmitNo(String submitNo);

    /**
     * 通过报工号删除数据
     *
     * @param submitNos
     * @return
     */
    int deleteMicroWorkSubmitHistoryBySubmitNos(String[] submitNos);

    /**
     * 清理数据
     *
     * @return
     */
    int deleteAllMicroWorkSubmitHistory();
}

