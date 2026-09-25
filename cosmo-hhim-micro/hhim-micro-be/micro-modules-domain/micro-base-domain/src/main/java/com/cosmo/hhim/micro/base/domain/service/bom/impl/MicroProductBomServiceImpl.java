/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.bom.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBom;
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBomHistory;
import com.cosmo.hhim.micro.base.domain.mapper.bom.MicroProductBomHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.bom.MicroProductBomMapper;
import com.cosmo.hhim.micro.base.domain.service.bom.IMicroProductBomService;
import com.cosmo.hhim.micro.infrastructure.enums.ProductTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.FormTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.ProductionModeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.BomAndTechTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品BOMService业务层处理
 * 
 * @date 2023-03-07
 */
@Service
@Slf4j
public class MicroProductBomServiceImpl implements IMicroProductBomService {

    @Autowired
    private MicroProductBomMapper microProductBomMapper;

    @Autowired
    private MicroProductBomHistoryMapper microProductBomHistoryMapper;

    /**
     * 获取生产订单排期页面的Bom
     * @param productSeq
     * @param planNum
     * @return
     */
    @Override
    public List<MicroProductBom> getBomListByScheduling(String productSeq, BigDecimal planNum){
        List<MicroProductBom> list=new ArrayList<>();
        //获取标准BOM
        MicroProductBom microProductBom=microProductBomMapper.getMicroProductBomByProductSeq(productSeq,BomAndTechTypeEnum.STANDARD.getCode());
        if (null == microProductBom) {
            //标准BOM不存在获取草稿BOM
            microProductBom = microProductBomMapper.getMicroProductBomByProductSeq(productSeq, BomAndTechTypeEnum.DRAFT.getCode());
        }
        if (null != microProductBom) {
            //获取二级BOM
            microProductBom.setProductNumber(planNum);
            list.add(microProductBom);
            MicroProductBom query = new MicroProductBom();
            query.setParentId(microProductBom.getId());
            query.setProductNumber(microProductBom.getProductNumber());
            query.setProductType(ProductTypeEnum.YCL.getCode());
            query.setProductionMode(ProductionModeEnum.SELF_CONTROL.getCode());
            List<MicroProductBom> sonList = microProductBomMapper.getMicroProductBomByParentId(query);
            // 因为一个产品至多有两种工艺，所以join的时候会出现至多两条，这里要过滤，优先选择标准工艺的
            Map<String, List<MicroProductBom>> bomMap = sonList.stream().collect(Collectors.groupingBy(MicroProductBom::getProductSeq));
            List<MicroProductBom> sonListTemp = new ArrayList<>();
            for (String key : bomMap.keySet()) {
                List<MicroProductBom> microProductBomList = bomMap.get(key);
                if (microProductBomList.size() > 1) {
                    // 只会有一个
                    List<MicroProductBom> bomList = microProductBomList.stream()
                            .filter(obj -> obj.getTechType().equals(BomAndTechTypeEnum.STANDARD.getCode()))
                            .collect(Collectors.toList());
                    sonListTemp.addAll(bomList);
                } else {
                    sonListTemp.addAll(microProductBomList);
                }
            }
            if (!CollectionUtils.isEmpty(sonListTemp)) {
                list.addAll(sonListTemp);
            }
        }
        return list;
    }

    /**
     * 保存生产BOM
     * @param microProductBom
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public void save(MicroProductBom microProductBom){
        // 校验是否含有二级bom，如果不存在二级bom，不在生成bom信息
        if (CollectionUtil.isEmpty(microProductBom.getSecondBomList())) {
            throw new CustomException("不存在二级bom，不能生成bom信息");
        }
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();

        //系统沉淀BOM保存历史
        MicroProductBom parentBom = microProductBomMapper.getMicroProductBomByProductSeq(microProductBom.getProductSeq(),BomAndTechTypeEnum.DRAFT.getCode());
        // 原则上不会出现二级bom为空的情况
        List<MicroProductBom> sonList = new ArrayList<>();
        if(null != parentBom){
            MicroProductBom query=new MicroProductBom();
            query.setParentId(parentBom.getId());
            sonList = microProductBomMapper.selectMicroProductBomList(query);
        }

        // 针对草稿bom新增的情况
        if(BomAndTechTypeEnum.DRAFT.getCode().equals(microProductBom.getBomType())){
            // 追加新增的物料,组成新的bom（原有bom在新的过程中）
            List<String> productSeqForNewSon = microProductBom.getSecondBomList()
                    .stream().map(MicroProductBom::getProductSeq)
                    .collect(Collectors.toList());
            List<MicroProductBom> filterOriginalSonList = sonList.stream().filter(obj -> !productSeqForNewSon.contains(obj.getProductSeq()))
                    .collect(Collectors.toList());
            microProductBom.getSecondBomList().addAll(filterOriginalSonList);
        }

        // 草稿和标准工艺操作都涉及到草稿工艺的删除和保存历史bom,删除自己以及下属BOM
        this.deleteDraftBomAndSaveHistory(parentBom, sonList, userId, tenantCode);

        //保存最新BOM
        microProductBom.setProductNumber(new BigDecimal(1));
        microProductBom.setParentId(0L);
        microProductBom.setParentProductSeq("0");
        if(!microProductBom.getSecondBomList().isEmpty() && microProductBom.getSecondBomList().size()>0){
            microProductBom.setIfChildren("1");
        }
        microProductBom.setCreatedBy(String.valueOf(userId));
        microProductBom.setCreatedDate(new Date());
        microProductBom.setLastUpdBy(String.valueOf(userId));
        microProductBom.setLastUpdDate(new Date());
        microProductBom.setTenantCode(tenantCode);
        microProductBomMapper.insertMicroProductBom(microProductBom);
        Map<String,Object> map=new HashMap<>(16);
        map.put("productSeq",microProductBom.getProductSeq());
        map.put("ifChildren",ActiveFlagStandardEnum.NORMAL.getCode());
        microProductBomMapper.updateSonMicroProductBom(map);
        //保存子BOM
        this.addMicroProductBom(microProductBom,userId,tenantCode);
    }

    /**
     * 保存下级BOM
     * @param microProductBom
     * @param userId
     */
    public void addMicroProductBom(MicroProductBom microProductBom,Long userId,String tenantCode) {
        if (!CollectionUtils.isEmpty(microProductBom.getSecondBomList())) {
            for (MicroProductBom temp : microProductBom.getSecondBomList()) {
                temp.setParentId(microProductBom.getId());
                temp.setParentProductSeq(microProductBom.getProductSeq());
                temp.setCreatedBy(String.valueOf(userId));
                temp.setCreatedDate(new Date());
                temp.setLastUpdBy(String.valueOf(userId));
                temp.setLastUpdDate(new Date());
                temp.setTenantCode(tenantCode);
                Map<String, Object> map = new HashMap<>(16);
                map.put("productSeq", temp.getProductSeq());
                int count = microProductBomMapper.getIfChildren(map);
                if (count > 0) {
                    temp.setIfChildren("1");
                }
                microProductBomMapper.insertMicroProductBom(temp);
            }
        }
    }

    /**
     * 修改产品BOM
     *
     * @param microProductBom 产品BOM
     * @return 结果
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public int update(MicroProductBom microProductBom) {
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        List<MicroProductBom> addList=new ArrayList<>();
        List<MicroProductBom> updateList=new ArrayList<>();
        List<Long> removeList=new ArrayList<>();
        for(MicroProductBom temp:microProductBom.getSecondBomList()) {
            FormTypeEnum formTypeEnum = FormTypeEnum.getFormTypeEnum(temp.getFormType());
            switch (formTypeEnum) {
                case ADD:
                    //新增物料
                    temp.setParentId(microProductBom.getId());
                    temp.setParentProductSeq(microProductBom.getProductSeq());
                    Map<String,Object> map=new HashMap<>(16);
                    map.put("productSeq",temp.getProductSeq());
                    int count=microProductBomMapper.getIfChildren(map);
                    if(count>0){
                        temp.setIfChildren("1");
                    }
                    temp.setCreatedBy(String.valueOf(userId));
                    temp.setLastUpdBy(String.valueOf(userId));
                    temp.setTenantCode(tenantCode);
                    addList.add(temp);
                    break;
                case EDIT:
                    temp.setLastUpdBy(String.valueOf(userId));
                    updateList.add(temp);
                    break;
                case DELETE:
                    //删除物料
                    removeList.add(temp.getId());
                    break;
                default:
                    break;
            }
        }
        if (!CollectionUtils.isEmpty(addList)) {
            microProductBomMapper.insertMicroProductBomList(addList);
        }
        if (!CollectionUtils.isEmpty(updateList)) {
            microProductBomMapper.updateMicroProductBomList(updateList);
        }
        if (!CollectionUtils.isEmpty(removeList)) {
            microProductBomMapper.deleteMicroProductBomByIds(removeList.toArray(new Long[removeList.size()]));
        }
        microProductBom.setLastUpdDate(new Date());
        microProductBom.setLastUpdBy(String.valueOf(userId));
        return microProductBomMapper.updateMicroProductBom(microProductBom);
    }


    /**
     * 获取下级BOM
     *
     * @param productSeq
     * @return
     */
    @Override
    public MicroProductBom getSonBomList(String productSeq, String productName) {
        //获取标准BOM
        MicroProductBom microProductBom = microProductBomMapper.getMicroProductBomByProductSeq(productSeq, BomAndTechTypeEnum.STANDARD.getCode());
        if (null == microProductBom) {
            //标准BOM不存在获取草稿BOM
            microProductBom = microProductBomMapper.getMicroProductBomByProductSeq(productSeq, BomAndTechTypeEnum.DRAFT.getCode());
        }
        if (null != microProductBom) {
            List<MicroProductBom> sonList = getChildList(productName, microProductBom);
            if (CollectionUtils.isEmpty(sonList)) {
                sonList = Collections.emptyList();
            }
            microProductBom.setSecondBomList(sonList);
        }
        return microProductBom;
    }

    /**
     * 获取子节点
     *
     * @param productName
     * @param microProductBom
     */
    private List<MicroProductBom> getChildList(String productName, MicroProductBom microProductBom) {
        //获取二级BOM
        MicroProductBom query = new MicroProductBom();
        query.setParentId(microProductBom.getId());
        query.setProductName(productName);
        return microProductBomMapper.selectMicroProductBomList(query);
    }

    /**
     *  删除草稿工艺并且保存历史
     *
     * @param parentBom
     * @param sonList
     * @param userId
     * @param tenantCode
     * @return
     */
    private void deleteDraftBomAndSaveHistory(MicroProductBom parentBom, List<MicroProductBom> sonList, Long userId, String tenantCode) {
        if (parentBom != null) {
            List<MicroProductBomHistory> saveList=new ArrayList<>();
            microProductBomMapper.deleteMicroProductBomByParentId(parentBom.getId());
            //保存历史数据
            int version = microProductBomHistoryMapper.getMaxVersion(parentBom.getProductSeq()) + 1;
            MicroProductBomHistory microProductBomHistory=new MicroProductBomHistory();
            BeanUtils.copyProperties(parentBom, microProductBomHistory, new String[]{"id","lastUpdBy","lastUpdDate"});
            microProductBomHistory.setVersion(String.valueOf(version));
            microProductBomHistory.setCreatedBy(String.valueOf(userId));
            microProductBomHistory.setTenantCode(tenantCode);
            microProductBomHistory.setLastUpdBy(String.valueOf(userId));
            saveList.add(microProductBomHistory);
            List<MicroProductBomHistory> historyList= sonList.stream().map(e->{
                MicroProductBomHistory entity= new MicroProductBomHistory();
                BeanUtils.copyProperties(e,entity,new String[]{"id","lastUpdBy","lastUpdDate"});
                entity.setCreatedBy(String.valueOf(userId));
                entity.setTenantCode(tenantCode);
                entity.setLastUpdBy(String.valueOf(userId));
                return entity;
            }).collect(Collectors.toList());
            saveList.addAll(historyList);
            microProductBomHistoryMapper.insertMicroProductBomHistoryList(saveList);
        }
    }

    /**
     * 判断新增产品是否符合bom（不能是当前产品的父级）
     *
     * @param currentProductSeq
     * @param addBomProductSeqList
     * @return
     */
    @Override
    public Boolean isOrNotBelongToParenBom(String currentProductSeq, List<String> addBomProductSeqList) {
        // 校验
        if (CollectionUtil.isEmpty(addBomProductSeqList)) {
            throw new CustomException("新增物料不能为空");
        }
        // 查询当前产品的所有父级bom
        List<String> productSeqList = new ArrayList<>();
        List<String> parenBomList = new ArrayList<>();
        productSeqList.add(currentProductSeq);
        parenBomList.add(currentProductSeq);
        // 正常情况下不会循环太多次
        while (!productSeqList.isEmpty()) {
            productSeqList = microProductBomMapper.selectFirstParentBomNodeList(productSeqList);
            // 判断是否会存在死循环的情况，bom存在嵌套
            if (CollectionUtil.isNotEmpty(productSeqList)) {
                for (String temp : productSeqList) {
                    if (parenBomList.contains(temp)) {
                        throw new CustomException("产品:" + temp + "存在嵌套");
                    }
                }
                log.info("新加入父级bom为:{}", JSONObject.toJSONString(productSeqList));
                // 加入新的一级父辈bom
                parenBomList.addAll(productSeqList);
            }
        }
        log.info("所有的父级bom为:{}", JSONObject.toJSONString(parenBomList));
        // 新增的bom信息是否在父级bom中
        for (String bom : addBomProductSeqList) {
            if (parenBomList.contains(bom)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int removeBomByProduct(List<String> seqList) {
        if (CollectionUtil.isEmpty(seqList)) {
            return 1;
        }
        microProductBomMapper.deleteMicroProductBomByProductSeq(seqList);
        microProductBomHistoryMapper.deleteMicroProductBomHistoryByProductSeq(seqList);
        return 1;
    }
}
