/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.material;

import com.cosmo.hhim.micro.application.dto.planning.MicroPickMaterialsDto;
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBom;
import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 投料页面返回信息
 * @date 2023/5/22 16:31
 */
@Data
public class MicroPickingMaterialDetailInfoDTO {

    /**
     * bom类型
     */
    private String bomType;

    /**
     * bom信息
     */
    private MicroProductBom microProductBom;

    /**
     * 投/退 料信息list
     */
    List<MicroPickMaterialsDto> pickMaterialsDtoList;
}
