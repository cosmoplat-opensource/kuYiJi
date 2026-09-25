/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.strategy;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessCommonService;
import com.cosmo.hhim.micro.infrastructure.designPattern.factory.SubmitStrategyFactory;
import com.cosmo.hhim.micro.infrastructure.designPattern.strategy.SubmitStrategy;
import com.cosmo.hhim.micro.infrastructure.enums.ApplicationTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.ExpiredRecordFlagEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
@Service
@Slf4j
public class ConcreteSubmitStrategyByKuYiJi implements SubmitStrategy<MicroWorkSubmit, MicroWorkSubmit>, InitializingBean {

    private static final long DAYS_LOOKBACK = 7;

    @Autowired
    private IMicroProductService microProductService;
    @Autowired
    private IMicroProcessCommonService microProcessCommonService;

    /**
     * 初始化Bean
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        SubmitStrategyFactory.register(ApplicationTypeEnum.KU_YI_JI.getCode(), this);
    }

    /**
     * 处理业务
     */
    @Override
    public MicroWorkSubmit submit(MicroWorkSubmit microWorkSubmit) {
        // 1. 校验报工日期,返回是否补录标示
        Long expiredRecordFlag = checkSubmitDay(microWorkSubmit.getSubmitDay());
        // 2. 判断是否有新的产品生成
        String productSeq = createNewProductForSubmit(microWorkSubmit, null);
        // 3. 判断是否有新的工序生成，返回对象内部包含了前工序和当前工序的序列码
        MicroWorkSubmit newProcessSeq = createNewProcessForSubmit(microWorkSubmit, null);

        microWorkSubmit.setExpiredRecordFlag(expiredRecordFlag);
        microWorkSubmit.setProductSeq(productSeq);
        microWorkSubmit.setOperateProcessSeq(newProcessSeq.getOperateProcessSeq());
        microWorkSubmit.setPreProcessSeq(newProcessSeq.getPreProcessSeq());

        return microWorkSubmit;
    }

    /**
     * 校验报工日
     */
    private Long checkSubmitDay(Date submitDay) {
        // 校验报工日
        if (CheckObjectUtils.isEmpty(submitDay)) {
            throw new CustomException("报工时报工日期不能为空");
        }
        LocalDate currentDate = LocalDate.now();
        LocalDate localDate = submitDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (currentDate.isBefore(localDate)) {
            throw new CustomException("报工日期不能大于当前日期");
        }
        // 日期加上7天
        if (currentDate.isAfter(localDate.plusDays(DAYS_LOOKBACK))) {
            throw new CustomException("只能选择以当前时间开始7天之内对日期进行报工");
        }

        // 判读是否为补录
        if (currentDate.isEqual(localDate)) {
            return ExpiredRecordFlagEnum.NO.getCode();
        } else {
            return ExpiredRecordFlagEnum.YES.getCode();
        }
    }

    /**
     * 判断报工记录是否有新的产品, list中存储要插入的新的产品数据
     */
    private String createNewProductForSubmit(MicroWorkSubmit microWorkSubmit, List<MicroProduct> insertMicroProducts) {

        String productSeq;
        if (StringUtils.isEmpty(microWorkSubmit.getProductSeq())) {
            // 生成新的产品
            MicroProduct newProduct = microProductService.createNewProduct(microWorkSubmit.getProductName());
            productSeq = newProduct.getProductSeq();
            microWorkSubmit.setProductSeq(productSeq);
            microWorkSubmit.setProductCode(productSeq);
            if (CheckObjectUtils.isNotEmpty(insertMicroProducts)) {
                insertMicroProducts.add(newProduct);
            }
        } else {
            productSeq = microWorkSubmit.getProductSeq();
        }

        return productSeq;
    }

    /**
     * 判断报工记录是否有新的工序, list中存储要插入的新工序数据
     */
    private MicroWorkSubmit createNewProcessForSubmit(MicroWorkSubmit microWorkSubmit, List<MicroProcessCommon> insertMicroProcesses) {
        String preProcessSeq = microWorkSubmit.getPreProcessSeq();
        String operateProcessSeq = microWorkSubmit.getOperateProcessSeq();

        if (StringUtils.isEmpty(preProcessSeq) && StringUtils.hasText(microWorkSubmit.getPreProcessName())) {
            // 判断前工序的名称是否已经生成
            // 存放前工序的结果
            MicroProcessCommon newPreProcess;
            MicroProcessCommon param = new MicroProcessCommon();
            param.setProcessName(microWorkSubmit.getPreProcessName());
            List<MicroProcessCommon> microProcessCommons = microProcessCommonService.selectMicroProcessCommonList(param);
            if (CollectionUtils.isEmpty(microProcessCommons)) {
                // 生成新的前工序
                newPreProcess = microProcessCommonService.createNewProcess(microWorkSubmit.getPreProcessName());
            } else {
                // 取已经生成的工序信息
                newPreProcess = microProcessCommons.get(0);
            }
            preProcessSeq = newPreProcess.getProcessSeq();
            microWorkSubmit.setPreProcessSeq(newPreProcess.getProcessSeq());
            microWorkSubmit.setPreProcessCode(newPreProcess.getProcessCode());
            // 编辑并审核的接口需要该部分
            if (CheckObjectUtils.isNotEmpty(insertMicroProcesses)) {
                insertMicroProcesses.add(newPreProcess);
            }
        }

        if (StringUtils.isEmpty(operateProcessSeq)) {
            // 判断当前工序名称是否已经生成
            MicroProcessCommon newOperateProcess;
            MicroProcessCommon param = new MicroProcessCommon();
            param.setProcessName(microWorkSubmit.getOperateProcessName());
            List<MicroProcessCommon> microProcessCommons = microProcessCommonService.selectMicroProcessCommonList(param);
            if (CollectionUtils.isEmpty(microProcessCommons)) {
                newOperateProcess = microProcessCommonService.createNewProcess(microWorkSubmit.getOperateProcessName());
            } else {
                newOperateProcess = microProcessCommons.get(0);
            }
            operateProcessSeq = newOperateProcess.getProcessSeq();
            microWorkSubmit.setOperateProcessSeq(newOperateProcess.getProcessSeq());
            microWorkSubmit.setOperateProcessCode(newOperateProcess.getProcessCode());
            // 编辑并审核的接口需要该部分
            if (CheckObjectUtils.isNotEmpty(insertMicroProcesses)) {
                insertMicroProcesses.add(newOperateProcess);
            }
        }

        MicroWorkSubmit res = new MicroWorkSubmit();
        res.setPreProcessSeq(preProcessSeq);
        res.setOperateProcessSeq(operateProcessSeq);
        return res;
    }
}
