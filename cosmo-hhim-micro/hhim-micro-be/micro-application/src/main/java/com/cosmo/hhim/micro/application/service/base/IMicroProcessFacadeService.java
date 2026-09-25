/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.micro.application.dto.base.MicroProcessMixedResultDTO;
import com.cosmo.hhim.micro.application.dto.base.MicroProcessMixedSelectDTO;
import com.cosmo.hhim.micro.application.dto.base.MicroProcessMultiMixedResultDTO;
import com.cosmo.hhim.micro.application.dto.base.MicroProcessStockQueryDTO;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessSelectEntity;
import com.cosmo.hhim.micro.base.domain.entity.tech.SimpleProcessResult;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
public interface IMicroProcessFacadeService {
    List<SimpleProcessResult> defaultRecommendByStandardTech(String productSeq, String operateProcessSeq); 

    /**
     * 下拉工序附带库存查询实体
     *
     * @param queryDTO
     * @return
     */
    List<MicroProcessSelectEntity> selectProcessStock(MicroProcessStockQueryDTO queryDTO);

    MicroProcessMixedResultDTO selectMixedByName(MicroProcessMixedSelectDTO mixedSelectDTO); 

    List<MicroProcessMultiMixedResultDTO> selectMultiMixed(List<MicroProcessMixedSelectDTO> multiList); 

    int removeProcessByIds(Long[] ids); 
}
