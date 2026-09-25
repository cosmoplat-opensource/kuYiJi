/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.micro.application.dto.base.MicroProductBomDto;
import com.cosmo.hhim.micro.application.service.base.IMicroProductBomFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBom;
import com.cosmo.hhim.micro.base.domain.mapper.bom.MicroProductBomMapper;
import com.cosmo.hhim.micro.base.domain.service.bom.IMicroProductBomService;
import com.cosmo.hhim.micro.infrastructure.enums.planning.BomAndTechTypeEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品BOMService业务层处理
 * 
 * @date 2023-03-07
 */
@Service
public class MicroProductBomFacadeServiceImpl implements IMicroProductBomFacadeService
{
    @Autowired
    private IMicroProductBomService microProductBomService;

    @Autowired
    private MicroProductBomMapper microProductBomMapper;

    /**
     * 查询产品BOM
     * 
     * @param id 产品BOMID
     * @return 产品BOM
     */
    @Override
    public MicroProductBom selectMicroProductBomById(Long id)
    {
        return microProductBomMapper.selectMicroProductBomById(id);
    }

    /**
     * 查询产品BOM列表
     * 
     * @param microProductBomDto 产品BOM
     * @return 产品BOM
     */
    @Override
    public List<MicroProductBomDto> selectMicroProductBomList(MicroProductBomDto microProductBomDto)
    {
        List<MicroProductBomDto> result=new ArrayList<>();
        MicroProductBom microProductBom=new MicroProductBom();
        BeanUtils.copyProperties(microProductBomDto,microProductBom);
        List<MicroProductBom> list=microProductBomMapper.selectMicroProductBomList(microProductBom);
        for(MicroProductBom entity:list){
            MicroProductBom query=new MicroProductBom();
            query.setParentId(entity.getId());
            List<MicroProductBom> sonList=microProductBomMapper.selectMicroProductBomList(query);
            entity.setSonNum(sonList.size());
        }
        result= list.stream().map(e->{
            MicroProductBomDto entity= new MicroProductBomDto();
            BeanUtils.copyProperties(e,entity);
            return entity;
        }).collect(Collectors.toList());
        return MicroPageUtils.listToPage(list, result);
    }

    /**
     * 新增产品BOM, 必然是标准的，标准的就得删去原先存在的草稿bom
     * 
     * @param microProductBomDto 产品BOM
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertMicroProductBom(MicroProductBomDto microProductBomDto) {
        MicroProductBom query=new MicroProductBom();
        query.setProductSeq(microProductBomDto.getProductSeq());
        query.setParentId(0L);
        query.setBomType(BomAndTechTypeEnum.STANDARD.getCode());
        List<MicroProductBom> tempList=microProductBomMapper.selectMicroProductBomList(query);
        if(CollectionUtil.isNotEmpty(tempList)){
            throw new CustomException("产品标准BOM已存在!");
        }
        MicroProductBom microProductBom = new MicroProductBom();
        BeanUtils.copyProperties(microProductBomDto, microProductBom);
        List<MicroProductBom> list= microProductBomDto.getSecondBomList().stream().map(e->{
            MicroProductBom entity= new MicroProductBom();
            BeanUtils.copyProperties(e,entity);
            // 插入时二级bom也都设置
            entity.setBomType(BomAndTechTypeEnum.STANDARD.getCode());
            return entity;
        }).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(list)) {
            throw new CustomException("未设置产品的二级bom");
        }
        // 删除该产品的草稿bom
        microProductBom.setSecondBomList(list);
        microProductBom.setBomType(BomAndTechTypeEnum.STANDARD.getCode());
        microProductBomService.save(microProductBom);
    }

    /**
     * 修改产品BOM
     * 
     * @param microProductBomDto 产品BOM
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMicroProductBom(MicroProductBomDto microProductBomDto)
    {
        MicroProductBom microProductBom=new MicroProductBom();
        BeanUtils.copyProperties(microProductBomDto,microProductBom);
        List<MicroProductBom> list= microProductBomDto.getSecondBomList().stream().map(e->{
            MicroProductBom entity= new MicroProductBom();
            BeanUtils.copyProperties(e,entity);
            // 更新时二级bom也都设置
            entity.setBomType(BomAndTechTypeEnum.STANDARD.getCode());
            return entity;
        }).collect(Collectors.toList());
        microProductBom.setSecondBomList(list);
        microProductBomService.update(microProductBom);
    }

}
