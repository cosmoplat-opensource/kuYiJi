/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.micro.application.dto.base.MicroProductBomDto;
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBom;

import java.util.List;

/**
 * 产品BOMService接口
 * 
 * @date 2023-03-07
 */
public interface IMicroProductBomFacadeService
{
    /**
     * 查询产品BOM
     * 
     * @param id 产品BOMID
     * @return 产品BOM
     */
    public MicroProductBom selectMicroProductBomById(Long id);

    /**
     * 查询产品BOM列表
     * 
     * @param microProductBomDto 产品BOM
     * @return 产品BOM集合
     */
    public List<MicroProductBomDto> selectMicroProductBomList(MicroProductBomDto microProductBomDto);

    /**
     * 新增产品BOM
     * 
     * @param microProductBomDto 产品BOM
     * @return 结果
     */
    public void insertMicroProductBom(MicroProductBomDto microProductBomDto);

    /**
     * 修改产品BOM
     * 
     * @param microProductBomDto 产品BOM
     * @return 结果
     */
    public void updateMicroProductBom(MicroProductBomDto microProductBomDto);

}
