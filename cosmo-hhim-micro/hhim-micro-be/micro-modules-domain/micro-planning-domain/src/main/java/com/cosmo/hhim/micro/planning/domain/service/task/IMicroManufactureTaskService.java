/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.service.task;

import com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask;

import java.util.List;

/**
 * 生产任务Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-09
 */
public interface IMicroManufactureTaskService 
{
    /**
     * 查询生产任务
     * 
     * @param id 生产任务ID
     * @return 生产任务
     */
    public MicroManufactureTask selectMicroManufactureTaskById(Long id);

    /**
     * 查询生产任务列表
     * 
     * @param microManufactureTask 生产任务
     * @return 生产任务集合
     */
    public List<MicroManufactureTask> selectMicroManufactureTaskList(MicroManufactureTask microManufactureTask);

    /**
     * 新增生产任务
     * 
     * @param microManufactureTask 生产任务
     * @return 结果
     */
    public int insertMicroManufactureTask(MicroManufactureTask microManufactureTask);

    /**
     * 修改生产任务
     * 
     * @param microManufactureTask 生产任务
     * @return 结果
     */
    public int updateMicroManufactureTask(MicroManufactureTask microManufactureTask);

    /**
     * 批量删除生产任务
     * 
     * @param ids 需要删除的生产任务ID
     * @return 结果
     */
    public int deleteMicroManufactureTaskByIds(Long[] ids);

    /**
     * 删除生产任务信息
     * 
     * @param id 生产任务ID
     * @return 结果
     */
    public int deleteMicroManufactureTaskById(Long id);

    /**
     * 报工记录变更
     */
    void recordChange(Boolean operateFlag,List<MicroManufactureTask> microManufactureTaskList); 
}
