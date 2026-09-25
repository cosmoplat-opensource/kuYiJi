/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.planning;

import com.cosmo.hhim.micro.application.dto.material.MicroPickingMaterialDetailInfoDTO;
import com.cosmo.hhim.micro.application.dto.planning.MicroPickMaterialsDto;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroPickMaterials;

import java.util.List;

/**
 * 投料单/退料单Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-10
 */
public interface IMicroPickMaterialsFacadeService {

    /**
     * 查询投料单/退料单列表
     * 
     * @param microPickMaterialsDto 投料单/退料单
     * @return 投料单/退料单集合
     */
    public MicroPickingMaterialDetailInfoDTO getListByWorkOrder(MicroPickMaterialsDto microPickMaterialsDto);

    /**
     * 查询投料单/退料单明细列表
     *
     * @param microPickMaterialsDto 投料单/退料单
     * @return 投料单/退料单集合
     */
    public List<MicroPickMaterialsDto> getList(MicroPickMaterialsDto microPickMaterialsDto);


}
