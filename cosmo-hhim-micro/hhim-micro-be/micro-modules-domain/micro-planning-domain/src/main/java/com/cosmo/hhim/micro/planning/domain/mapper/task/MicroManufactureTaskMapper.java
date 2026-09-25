/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.mapper.task;

import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 生产任务Mapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-09
 */
public interface MicroManufactureTaskMapper 
{
    /**
     * 查询生产任务
     * 
     * @param id 生产任务ID
     * @return 生产任务
     */
    MicroManufactureTask selectMicroManufactureTaskById(Long id);

    /**
     * 查询生产任务列表
     * 
     * @param microManufactureTask 生产任务
     * @return 生产任务集合
     */
    List<MicroManufactureTask> selectMicroManufactureTaskList(MicroManufactureTask microManufactureTask);

    /**
     * 新增生产任务
     * 
     * @param microManufactureTask 生产任务
     * @return 结果
     */
    int insertMicroManufactureTask(MicroManufactureTask microManufactureTask);

    /**
     * 修改生产任务
     * 
     * @param microManufactureTask 生产任务
     * @return 结果
     */
    int updateMicroManufactureTask(MicroManufactureTask microManufactureTask);

    /**
     * 删除生产任务
     * 
     * @param id 生产任务ID
     * @return 结果
     */
    int deleteMicroManufactureTaskById(Long id);

    /**
     * 批量删除生产任务
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroManufactureTaskByIds(Long[] ids);

    /**
     * 批量保存生产任务
     *
     * @param taskList 生产任务集合
     * @return 结果
     */
    void insertList(List<MicroManufactureTask> taskList);

    /**
     * 增加任务完成数与不良数
     * @param userId
     * @param taskList
     */
    void addTaskFinishNum(@Param("userId") String userId, @Param("taskList") List<MicroManufactureTask> taskList);

    /**
     * @author cosmo-hhim-open Team
     * @description 条件查询生产任务
     * @date 2023/3/20 18:00
     * @param microManufactureTask
     * @return java.util.List<com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask>
     **/
    List<MicroManufactureTask> selectMicroManufactureTaskListByCondition(MicroManufactureTask microManufactureTask); 

    /**
     * 工单号获取工序——按序
     *
     * @param workOrderNo
     * @return
     */
    List<String> getProcessSeqByWorkOrderNo(@Param("workOrderNo") String workOrderNo);

    /**
     * 根据工序ID获取工单记录
     *
     * @param ids
     * @return
     */
    List<MicroSelectEntity> selectReProcessByProcessIds(Long[] ids);
}
