/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.tech;

import com.cosmo.hhim.micro.application.dto.tech.*;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroStandardEntity;
import com.google.common.collect.HashMultimap;

import java.util.List;

/**
 * 工艺链应用层接口
 *
 * @author cosmo-hhim-open Team
 */
public interface IMicroTechFacadeService {

    MicroTechChainResult getTechnologyChain(Long productId); 

    MicroTechnologyBindDTO bindTech(MicroTechnologyBindDTO bindDTO); 

    int coverOrSimilar(MicroTechnologyBindDTO bindDTO); 

    List<MicroStandardEntity> selectStandardPro(String key); 

    boolean isOrNotHaveStandardTech(String productSeq); 

    boolean saveSingleChain(MicroSaveSingleTechDTO techChainDTO); 

    List<MicroSingleTechChainDTO> getSelfProductTechnology(Long productId);

    /**
     * 校验产品是否有工艺，如果有工序是否符合工艺路线
     *
     * @param validSubmitRecordTechInfoParam
     * @return
     */
    boolean validSubmitRecordTechInfo(ValidSubmitRecordTechInfoParam validSubmitRecordTechInfoParam);

    void saveSingleChainFromStorageImport(HashMultimap<MicroSelectEntity, MicroSelectEntity> map); 
}
