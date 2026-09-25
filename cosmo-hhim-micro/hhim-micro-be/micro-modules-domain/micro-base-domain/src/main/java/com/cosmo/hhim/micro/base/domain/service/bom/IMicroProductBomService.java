/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.bom;

import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBom;

import java.math.BigDecimal;
import java.util.List;

/**
 * 产品BOMService业务层处理
 *
 * @date 2023-03-07
 */
public interface IMicroProductBomService {

    /**
     * 获取生产订单排期页面的Bom
     *
     * @param productSeq
     * @param planNum
     * @return
     */
    public List<MicroProductBom> getBomListByScheduling(String productSeq, BigDecimal planNum);

    /**
     * 保存生产BOM
     *
     * @param microProductBom
     */
    public void save(MicroProductBom microProductBom);

    /**
     * 修改生产BOM
     *
     * @param microProductBom
     */
    public int update(MicroProductBom microProductBom); 

    /**
     * 获取下级BOM
     *
     * @param productSeq
     */
    MicroProductBom getSonBomList(String productSeq, String productName); 

    /**
     * 判断新增产品是否符合bom（不能是当前产品的父级）
     *
     * @param currentProductSeq
     * @param addBomProductSeqList
     * @return
     */
    Boolean isOrNotBelongToParenBom(String currentProductSeq, List<String> addBomProductSeqList);

    /**
     * 根据产品seq集合删除BOM
     *
     * @param seqList
     * @return
     */
    int removeBomByProduct(List<String> seqList);
}
