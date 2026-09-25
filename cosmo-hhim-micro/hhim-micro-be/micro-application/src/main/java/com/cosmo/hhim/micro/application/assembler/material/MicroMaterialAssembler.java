/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.material;

import com.cosmo.hhim.micro.application.dto.material.MicroMaterialAnalysisDTO;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrderMaterialEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 物料组装类
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroMaterialAssembler {


    public static List<MicroMaterialAnalysisDTO> materialEntityToDTO(List<MicroManufactureWorkOrderMaterialEntity> materialEntityList) {
        List<MicroMaterialAnalysisDTO> analysisList = new ArrayList<>();
        for (MicroManufactureWorkOrderMaterialEntity material : materialEntityList) {
            MicroMaterialAnalysisDTO analysisDTO = new MicroMaterialAnalysisDTO();
            analysisDTO.setProductId(material.getProductId());
            analysisDTO.setProductSeq(material.getProductSeq());
            analysisDTO.setProductCode(material.getProductCode());
            analysisDTO.setProductName(material.getProductName());
            analysisDTO.setProductUnit(material.getProductUnit());
            analysisDTO.setDemandNum(material.getDemandNum());
            analysisDTO.setStockNum(material.getStockNum());
            analysisDTO.setShortageNum(material.getShortageNum());
            analysisList.add(analysisDTO);
        }
        return analysisList;
    }
}
