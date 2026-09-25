/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.material;

import com.cosmo.hhim.micro.application.dto.material.MicroMaterialAnalysisDTO;
import com.cosmo.hhim.micro.application.dto.material.MicroMaterialAnalysisDetailDTO;
import com.cosmo.hhim.micro.application.dto.material.MicroMaterialAnalysisQueryDTO;

import java.util.List;

/**
 * 料易投物料
 *
 * @author cosmo-hhim-open Team
 */
public interface IMicroMaterialFacadeService {

    /**
     * @param queryDTO
     * @return
     */
    List<MicroMaterialAnalysisDTO> selectMicroMaterialAnalysisList(MicroMaterialAnalysisQueryDTO queryDTO); 

    List<MicroMaterialAnalysisDetailDTO> selectAnalysisDetail(MicroMaterialAnalysisQueryDTO queryDTO); 
}
