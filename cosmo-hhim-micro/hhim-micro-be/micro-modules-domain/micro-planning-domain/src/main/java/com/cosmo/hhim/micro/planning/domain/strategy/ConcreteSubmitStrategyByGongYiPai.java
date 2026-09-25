/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.strategy;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.infrastructure.designPattern.factory.SubmitStrategyFactory;
import com.cosmo.hhim.micro.infrastructure.designPattern.strategy.SubmitStrategy;
import com.cosmo.hhim.micro.infrastructure.enums.ApplicationTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.TaskSubmitStatusEnum;
import com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.mapper.task.MicroManufactureTaskMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.work.MicroManufactureWorkOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 涉及工易派的业务抽取
 * @date 2023/3/24 13:46
 */
@Service
@Slf4j
public class ConcreteSubmitStrategyByGongYiPai implements SubmitStrategy<MicroManufactureTask, MicroManufactureTask>, InitializingBean {

    @Autowired
    private MicroManufactureTaskMapper microManufactureTaskMapper;
    @Autowired
    private MicroManufactureWorkOrderMapper microManufactureWorkOrderMapper;

    /**
     * Bean初始化
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        SubmitStrategyFactory.register(ApplicationTypeEnum.GONG_YI_PAI.getCode(),this);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public MicroManufactureTask submit(MicroManufactureTask manufactureTask) {
        Date nowDate = DateUtils.getNowDate();
        String userId = SecurityUtils.getUserId().toString();

        MicroManufactureTask queryParam = new MicroManufactureTask();
        queryParam.setTaskNo(manufactureTask.getTaskNo());
        List<MicroManufactureTask> microManufactureTaskList = microManufactureTaskMapper.selectMicroManufactureTaskListByCondition(queryParam);
        if (!CollectionUtils.isEmpty(microManufactureTaskList)) {
            MicroManufactureTask microManufactureTaskTemp = microManufactureTaskList.get(0);

            // 1.更新任务单状态
            if (microManufactureTaskTemp.getSubmitStatus().equals(TaskSubmitStatusEnum.WORK_REPORTING.getCode())){
                microManufactureTaskTemp.setSubmitStatus(TaskSubmitStatusEnum.WORK_REPORTING.getCode());
                microManufactureTaskTemp.setLastUpdBy(userId);
                microManufactureTaskTemp.setLastUpdDate(nowDate);
                microManufactureTaskMapper.updateMicroManufactureTask(microManufactureTaskTemp);
            }

            // 2.更新工单生产开始时间
            if(StringUtils.hasText(microManufactureTaskTemp.getWorkOrderNo())) {
                MicroManufactureWorkOrder workOrderInfo = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderByWorkOrderNo(microManufactureTaskTemp.getWorkOrderNo());
                if(null != workOrderInfo && null == workOrderInfo.getProduceStartDate()){
                    MicroManufactureWorkOrder updateParam = new MicroManufactureWorkOrder();
                    updateParam.setId(workOrderInfo.getId());
                    updateParam.setProduceStartDate(nowDate);
                    updateParam.setLastUpdBy(userId);
                    updateParam.setLastUpdDate(nowDate);
                    microManufactureWorkOrderMapper.updateMicroManufactureWorkOrder(updateParam);
                }
            }
        }
        return manufactureTask;
    }
}
