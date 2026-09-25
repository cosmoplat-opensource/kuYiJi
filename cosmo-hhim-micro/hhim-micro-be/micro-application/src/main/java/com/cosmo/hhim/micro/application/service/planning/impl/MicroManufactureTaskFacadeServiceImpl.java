/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.planning.impl;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.planning.MicroManufactureTaskAssembler;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureTaskDto;
import com.cosmo.hhim.micro.application.service.planning.IMicroManufactureTaskFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.TotalSubmitNumDTO;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroProductMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessCommonMapper;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitService;
import com.cosmo.hhim.micro.infrastructure.designPattern.factory.SubmitStrategyFactory;
import com.cosmo.hhim.micro.infrastructure.designPattern.strategy.SubmitStrategy;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask;
import com.cosmo.hhim.micro.planning.domain.service.task.IMicroManufactureTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 生产任务Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-09
 */
@Service
@Slf4j
public class MicroManufactureTaskFacadeServiceImpl implements IMicroManufactureTaskFacadeService {

    @Autowired
    private MicroProductMapper microProductMapper;

    @Autowired
    private MicroProcessCommonMapper microProcessCommonMapper;

    @Autowired
    private IMicroManufactureTaskService microManufactureTaskService;

    @Autowired
    private IMicroWorkSubmitService microWorkSubmitService;

    @Autowired
    private MicroSupportUtil microSupportUtil;

    /**
     * 生产报工
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void productionReporting(MicroManufactureTaskDto microManufactureTaskDto){
        log.info("请求的参数为:{}", JSONObject.toJSONString(microManufactureTaskDto));
        this.validSubmitNum(microManufactureTaskDto);
        //1、更新生产任务状态
        MicroManufactureTask microManufactureTask = MicroManufactureTaskAssembler.convert2Condition(microManufactureTaskDto);
        SubmitStrategy submitStrategy = SubmitStrategyFactory.getSubmitStrategy(SecurityUtils.getApplicationSign());
        submitStrategy.submit(microManufactureTask);
        //2、生成报工记录
        MicroProduct microProduct = microProductMapper.selectMicroProductByProductSeq(microManufactureTaskDto.getProductSeq());
        MicroProcessCommon microProcess = microProcessCommonMapper.selectMicroProcessCommonByProcessSeq(microManufactureTaskDto.getProcessSeq());
        MicroWorkSubmit microWorkSubmit= MicroManufactureTaskAssembler.convertDto2Domain(microManufactureTaskDto, microProduct, microProcess);
        microWorkSubmitService.insertMicroWorkSubmit(microWorkSubmit);
    }

    @Override
    public List<MicroManufactureTaskDto> selectMicroManufactureTaskList(MicroManufactureTaskDto condition) {
        List<MicroManufactureTask> taskList = microManufactureTaskService.selectMicroManufactureTaskList(MicroManufactureTaskAssembler.convert2Condition(condition));
        return MicroManufactureTaskAssembler.convertDomains2DTOs(taskList);
    }

    /**
     * 校验报工数量
     *
     * @param microManufactureTaskDto
     */
    public void validSubmitNum(MicroManufactureTaskDto microManufactureTaskDto) {
        // 查询任务获取计划数量
        MicroManufactureTask microManufactureTask = microManufactureTaskService.selectMicroManufactureTaskById(microManufactureTaskDto.getId());
        // 获取报工数量(已审良品 + 未审核良品 = 已报工， 计划数量 - 已报工 = 待报工)
        MicroWorkSubmit queryParam = new MicroWorkSubmit();
        queryParam.setWorkOrderNo(microManufactureTaskDto.getWorkOrderNo());
        queryParam.setOperateProcessSeq(microManufactureTaskDto.getProcessSeq());
        Long submitType = microSupportUtil.getSubmitType();
        queryParam.setSubmitType(submitType);
        TotalSubmitNumDTO totalSubmitNumDTO = microWorkSubmitService.obtainedSubmitNum(queryParam);
        // 校验数量
        BigDecimal alreadySubmitPassNum = totalSubmitNumDTO.getCheckNum().add(totalSubmitNumDTO.getWaitCheckNum());
        BigDecimal waitSubmitNum = microManufactureTask.getTaskPlanNum().subtract(alreadySubmitPassNum);
        if (microManufactureTaskDto.getPassNum().compareTo(waitSubmitNum) > 0) {
            throw new CustomException("报工数量超过工序任务计划数量");
        }
    }
}
