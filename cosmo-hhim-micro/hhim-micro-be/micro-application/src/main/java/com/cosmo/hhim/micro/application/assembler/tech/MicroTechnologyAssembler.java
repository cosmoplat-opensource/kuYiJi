/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.tech;

import cn.hutool.core.bean.BeanUtil;
import com.cosmo.hhim.micro.application.dto.tech.MicroProcessChainBindDTO;
import com.cosmo.hhim.micro.application.dto.tech.MicroTechnologyBindDTO;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessChain;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroTechBindCondition;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroTechBindProductDomain;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroTechnology;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroTechnologyBindResult;
import com.cosmo.hhim.micro.infrastructure.enums.IsLastProcessEnum;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
public class MicroTechnologyAssembler {

    public static MicroTechnologyBindDTO toBindResult(MicroTechnologyBindResult result) {
        MicroTechnologyBindDTO bindDTO = new MicroTechnologyBindDTO();
        bindDTO.setCoverProductList(result.getCoverProductList());
        bindDTO.setSimilarProductList(result.getSimilarProductList());
        return bindDTO;
    }


    public static MicroTechBindCondition toCondition(MicroTechnologyBindDTO bindDTO) {
        return BeanUtil.copyProperties(bindDTO, MicroTechBindCondition.class);
    }

    public static MicroTechnology toTechDomain(MicroTechnologyBindDTO bindDTO, String tenantCode, Long userId) {
        MicroTechnology microTechnology = getMicroTechnology(tenantCode, userId, bindDTO.getTechId(),
                bindDTO.getProductSeq(), bindDTO.getProductId(), bindDTO.getProductName(), bindDTO.getTechPattern());
        return microTechnology;
    }

    public static List<MicroProcessChain> toChainList(List<MicroProcessChainBindDTO> chainBindDTOList, String tenantCode, Long userId, Long techId) {
        return chainBindDTOList.stream().map(chain -> toProcessChainDomain(chain, tenantCode, userId, techId)).collect(Collectors.toList());
    }

    private static MicroProcessChain toProcessChainDomain(MicroProcessChainBindDTO chain, String tenantCode, Long userId, Long techId) {
        MicroProcessChain processChain = new MicroProcessChain();
        processChain.setId(chain.getId());
        processChain.setTechId(techId);
        processChain.setProcessId(chain.getProcessId());
        processChain.setProcessSeq(chain.getProcessSeq());
        processChain.setProcessCode(chain.getProcessCode());
        processChain.setParentProcessId(chain.getParentProcessId());
        processChain.setParentProcessSeq(chain.getParentProcessSeq());
        processChain.setIsLastProcess(StringUtils.isEmpty(chain.getIsLastProcess()) ? IsLastProcessEnum.NO.getCode() : chain.getIsLastProcess());
        processChain.setTenantCode(tenantCode);
        processChain.setCreatedBy(String.valueOf(userId));
        processChain.setLastUpdBy(String.valueOf(userId));
        return processChain;
    }

    public static MicroTechnology getMicroTechnology(String tenantCode, Long userId, Long techId, String productSeq, Long productId, String productName, Integer techPattern) {
        MicroTechnology technology = new MicroTechnology();
        technology.setId(techId);
        technology.setProductSeq(productSeq);
        technology.setProductId(productId);
        technology.setProductName(productName);
        technology.setTenantCode(tenantCode);
        technology.setCreatedBy(String.valueOf(userId));
        technology.setLastUpdBy(String.valueOf(userId));
        technology.setTechPattern(techPattern);
        return technology;
    }

    public static List<MicroTechnology> toTechDomainList(MicroTechnologyBindDTO bindDTO, String tenantCode, Long userId) {
        List<MicroTechBindProductDomain> productList;
        if (CollectionUtils.isEmpty(bindDTO.getCoverProductList())) {
            productList = bindDTO.getSimilarProductList();
        } else {
            productList = bindDTO.getCoverProductList();
        }
        return productList.stream().map(p -> getMicroTechnology(tenantCode, userId, p.getTechId(), p.getProductSeq(),
                p.getProductId(), bindDTO.getProductName(), bindDTO.getTechPattern())).collect(Collectors.toList());
    }

    /**
     * 组装工艺查询实体
     *
     * @param tenantCode 租户编码
     * @param productId 产品ID
     * @param productSeq 产品序号
     * @return 工艺查询实体
     */
    public static MicroTechnology toTechQuery(String tenantCode, Long productId, String productSeq) {
        MicroTechnology queryTechnology = new MicroTechnology();
        queryTechnology.setActiveFlag("1");
        queryTechnology.setProductId(productId);
        queryTechnology.setProductSeq(productSeq);
        queryTechnology.setTenantCode(tenantCode);
        return queryTechnology;
    }

}
