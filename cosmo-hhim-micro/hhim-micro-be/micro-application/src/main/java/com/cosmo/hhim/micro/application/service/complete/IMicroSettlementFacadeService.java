/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.complete;

import com.cosmo.hhim.micro.application.dto.complete.*;

import java.util.Date;
import java.util.List;

/**
 * 计件结算报告Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
public interface IMicroSettlementFacadeService {
    /**
     * 报工记录审产后生成待结算报告
     *
     * @return
     */
    int generate(MicroSettlementGenerateQueryDTO queryDTO); 


    /**
     * 查询人员的待结算信息
     *
     * @param queryDTO
     * @return
     */
    List getOpenSettlementUser(MicroSettlementQueryDTO queryDTO);

    /**
     * 查询人员的已结算信息
     *
     * @param queryDTO
     * @return
     */
    List getSettledUser(MicroSettlementQueryDTO queryDTO);

    /**
     * 查询产品的已结算信息
     *
     * @param queryDTO
     * @return
     */
    List getSettledProduct(MicroSettlementQueryDTO queryDTO);

    List getSettledProductDetail(MicroSettlementQueryDTO queryDTO); 

    /**
     * 调整员工的待结算数量
     *
     * @param modifyDTO
     * @return
     */
    int editEmployeeSettlement(MicroSettlementModifyDTO modifyDTO);

    List<MicroSettlementHistoryDTO> getReportEditHistory(MicroSettlementQueryDTO userKey); 

    String exportSettledReport(MicroSettlementQueryDTO queryDTO); 

    String exportOpenSettlementReport(MicroSettlementQueryDTO queryDTO); 

    List<MicroSettlementHistoryDetailDTO> getReportEditHistoryDetail(Long userId, String productSeq, String operateProcessSeq, Date searchDate); 

    MicroSettlementHomeIndexDTO homeIndex(); 
}
