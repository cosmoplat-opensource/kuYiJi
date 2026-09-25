/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.planning;

import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureTaskDto;

import java.util.List;

/**
 * 生产任务Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-09
 */
public interface IMicroManufactureTaskFacadeService {

    List<MicroManufactureTaskDto> selectMicroManufactureTaskList(MicroManufactureTaskDto condition); 
    /**
     * 生产报工
     */
    void productionReporting(MicroManufactureTaskDto microManufactureTaskDto); 

}
