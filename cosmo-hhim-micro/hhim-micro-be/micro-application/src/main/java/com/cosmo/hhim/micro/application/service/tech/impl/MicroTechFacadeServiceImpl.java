/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.tech.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.tech.MicroTechnologyAssembler;
import com.cosmo.hhim.micro.application.dto.tech.*;
import com.cosmo.hhim.micro.application.service.tech.IMicroTechFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.tech.*;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroTechnologyService;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroStandardEntity;
import com.cosmo.hhim.micro.infrastructure.enums.planning.BomAndTechTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.TechPatternEnum;
import com.google.common.collect.HashMultimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Service
@Slf4j
public class MicroTechFacadeServiceImpl implements IMicroTechFacadeService {
    @Autowired
    private IMicroTechnologyService technologyService;
    @Autowired
    private IMicroProductService productService;

    /**
     * 获取工艺链信息,如果没有从预研团队获取
     *
     * @param productId
     * @return
     */
    @Override
    public MicroTechChainResult getTechnologyChain(Long productId) {
        MicroTechChainResult result = new MicroTechChainResult();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        MicroProduct existProduct = productService.selectMicroProductById(productId);
        if (existProduct == null) {
            throw new CustomException("无法获取产品数据");
        }
        List<MicroProcessChainBindEntity> bindList = technologyService.selectMicroTechChainByTechType(
                MicroTechnologyAssembler.toTechQuery(tenantCode, existProduct.getId(), null), null);
        if (CollectionUtils.isEmpty(bindList)) {
            //如果不存在工艺信息,说明标准与非标准(推荐/期初导入)均没有数据
            result.setStandard(false);
            return result;
        }
        result.setStandard(BomAndTechTypeEnum.STANDARD.getCode().equals(bindList.get(0).getTechType()));
        result.setTechPattern(bindList.get(0).getTechPattern());
        result.setTechData(assembleResult(bindList));
        return result;
    }

    /**
     * 拼装参数
     *
     * @param bindEntityList
     * @return
     */
    private List<MicroProcessChainBindDTO> assembleResult(List<MicroProcessChainBindEntity> bindEntityList) {
        return bindEntityList.stream().map(b -> BeanUtil.copyProperties(b, MicroProcessChainBindDTO.class)).collect(Collectors.toList());
    }

    /**
     * 绑定工艺
     *
     * @param bindDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MicroTechnologyBindDTO bindTech(MicroTechnologyBindDTO bindDTO) {
        validBindParam(bindDTO);
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        Long userId = SecurityUtils.getUserId();
        MicroTechnologyBindResult result = technologyService.bindTechAndChain(
                MicroTechnologyAssembler.toTechDomain(bindDTO, tenantCode, userId),
                MicroTechnologyAssembler.toChainList(bindDTO.getChainList(), tenantCode, userId, bindDTO.getTechId()),
                MicroTechnologyAssembler.toCondition(bindDTO));
        return MicroTechnologyAssembler.toBindResult(result);
    }

    /**
     * 工艺链绑定时,根据其他标准工艺产品的或非标产品进行绑定
     *
     * @param bindDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int coverOrSimilar(MicroTechnologyBindDTO bindDTO) {
        //先去掉清空操作的校验
//        validBindCoverParam(bindDTO);
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        Long userId = SecurityUtils.getUserId();
        return technologyService.coverOrSimilar(
                MicroTechnologyAssembler.toTechDomainList(bindDTO, tenantCode, userId),
                MicroTechnologyAssembler.toChainList(bindDTO.getChainList(), tenantCode, userId, bindDTO.getTechId()));
    }

    /**
     * 根据搜索条件获取产品(带标准工序标记)
     *
     * @param key
     * @return
     */
    @Override
    public List<MicroStandardEntity> selectStandardPro(String key) {
        return technologyService.selectStandardPro(key);
    }

    @Override
    public boolean isOrNotHaveStandardTech(String productSeq) {
        List<MicroTechnology> technologyList = technologyService.judgeStandardOrProduct(null, productSeq, null);
        if (CollectionUtils.isEmpty(technologyList)) {
            return false;
        } else {
            MicroTechnology technology = technologyList.stream().filter(t -> BomAndTechTypeEnum.STANDARD.getCode().equals(t.getTechType())).findAny().orElse(null);
            return technology != null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSingleChain(MicroSaveSingleTechDTO singleTechChainDTO) {
        Assert.isTrue(!CollectionUtils.isEmpty(singleTechChainDTO.getProcessChainList()), "无法获取工艺信息");
        String techType = singleTechChainDTO.getTechType();
        Assert.isTrue(StringUtils.isEmpty(techType), "无法获取工艺类型信息");
        String productSeq = singleTechChainDTO.getProductSeq();
        Assert.isTrue(StringUtils.isEmpty(productSeq), "无法获取产品信息");
        String productName = singleTechChainDTO.getProductName();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        Long productId = singleTechChainDTO.getProductId();
        if (productId == null) {
            MicroProduct product = new MicroProduct();
            product.setProductSeq(productSeq);
            product.setTenantCode(tenantCode);
            List<MicroProduct> productList = productService.selectMicroProductList(product);
            Assert.isTrue(!CollectionUtils.isEmpty(productList), "无法获取绑定的产品数据");
            productId = productList.get(0).getId();
        }
        MicroSingleTechChainEntity singleTechChain = new MicroSingleTechChainEntity();
        singleTechChain.setProductId(productId);
        singleTechChain.setProductSeq(productSeq);
        singleTechChain.setTechType(techType);
        singleTechChain.setProductName(productName);
        singleTechChain.setProcessChainList(BeanUtil.copyToList(singleTechChainDTO.getProcessChainList(), MicroSaveChainNodeEntity.class));
        return technologyService.saveSingleTechAndChain(singleTechChain);
    }

    @Override
    public List<MicroSingleTechChainDTO> getSelfProductTechnology(Long productId) {
        List<MicroSingleTechChainEntity> chainEntityList = technologyService.selectSingleChainByProduct(Collections.singletonList(productId), null);
        return chainEntityList.stream().map(b -> BeanUtil.copyProperties(b, MicroSingleTechChainDTO.class))
                .collect(Collectors.toList());
    }


    private void validBindParam(MicroTechnologyBindDTO bindDTO) {
        if (bindDTO.getTechPattern() == null) {
            throw new CustomException("无法获取工艺形式");
        }
        if (bindDTO.getStandard() == null) {
            throw new CustomException("无法获取标准工艺信息");
        }
        if (bindDTO.getProductId() == null) {
            throw new CustomException("无法获取要绑定的产品");
        }
        if (StringUtils.isEmpty(bindDTO.getProductSeq())) {
            throw new CustomException("无法获取要绑定的产品信息");
        }
    }

    /**
     * 校验产品是否有工艺，如果有工序是否符合工艺路线
     *
     * @param validSubmitRecordTechInfoParam
     * @return
     */
    @Override
    public boolean validSubmitRecordTechInfo(ValidSubmitRecordTechInfoParam validSubmitRecordTechInfoParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(validSubmitRecordTechInfoParam));
        return technologyService.validSubmitRecordTechInfo(validSubmitRecordTechInfoParam.getProductSeq(),
                validSubmitRecordTechInfoParam.getProcessSeq(),
                validSubmitRecordTechInfoParam.getPreProcessSeq());
    }

    @Override
    public void saveSingleChainFromStorageImport(HashMultimap<MicroSelectEntity, MicroSelectEntity> map) {
        for (MicroSelectEntity product : map.keySet()) {
            Set<MicroSelectEntity> processSet = map.get(product);
            MicroSingleTechChainEntity techChainEntity = new MicroSingleTechChainEntity();
            techChainEntity.setProductId(product.getItemId());
            techChainEntity.setProductSeq(product.getItemSeq());
            techChainEntity.setProductName(product.getItemName());
            techChainEntity.setTechType(BomAndTechTypeEnum.DRAFT.getCode());
            techChainEntity.setTechPattern(TechPatternEnum.INITIAL.getCode());
            techChainEntity.setProcessChainList(processSet.stream().map(p -> {
                MicroSaveChainNodeEntity nodeEntity = new MicroSaveChainNodeEntity();
                nodeEntity.setProcessId(p.getItemId());
                nodeEntity.setProcessSeq(p.getItemSeq());
                nodeEntity.setProcessName(p.getItemName());
                nodeEntity.setProcessCode(p.getItemCode());
                return nodeEntity;
            }).collect(Collectors.toList()));
            technologyService.saveSingleTechAndChain(techChainEntity);
        }
    }
}
