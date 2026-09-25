/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProductEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroProductSelectEntity;

import java.util.List;

/**
 * 产品Service接口
 *
 * @date 2022-10-11
 */
public interface IMicroProductService {
    /**
     * 查询产品
     *
     * @param id 产品ID
     * @return 产品
     */
    MicroProduct selectMicroProductById(Long id);

    /**
     * 根据产品编码查询产品信息
     * @param productSeq
     * @return
     */
    MicroProduct selectMicroProductByProductSeq(String productSeq);

    /**
     * 根据产品编码查询产品信息
     * @param productCode
     * @return
     */
    MicroProduct selectMicroProductByProductCode(String productCode);

    /**
     * 查询产品列表
     *
     * @param microProduct 产品
     * @return 产品集合
     */
    List<MicroProduct> selectMicroProductList(MicroProduct microProduct);

    /**
     * 查询所有产品列表
     *
     * @return 产品
     */
    List<MicroProduct> selectMicroAllProductList();

    /**
     * 新增产品
     *
     * @param microProduct 产品
     * @return 结果
     */
    MicroProduct insertMicroProduct(MicroProduct microProduct);

    /**
     * 批量保存产品
     *
     * @param productList
     * @return
     */
    List<MicroProductEntity> saveMicroProducts(List<MicroProductEntity> productList);

    /**
     * 修改产品
     *
     * @param microProduct 产品
     * @return 结果
     */
    int updateMicroProduct(MicroProduct microProduct);

    /**
     * 批量删除产品
     *
     * @param ids 需要删除的产品ID
     * @return 结果
     */
    int deleteMicroProductByIds(Long[] ids);

    /**
     * 删除产品信息
     *
     * @param id 产品ID
     * @return 结果
     */
    int deleteMicroProductById(Long id);

    List<MicroProductSelectEntity> selectMicroProductByName(String key, boolean mixed); 

    /**
     * 根据产品名称建立新的产品
     *
     * @param productName
     * @return
     */
    MicroProduct createNewProduct(String productName);


    /**
     * 是否有相似的产品名称
     *
     * @param productName
     * @return
     */
    MicroProduct isOrNotHaveProduct(String productName);
}
