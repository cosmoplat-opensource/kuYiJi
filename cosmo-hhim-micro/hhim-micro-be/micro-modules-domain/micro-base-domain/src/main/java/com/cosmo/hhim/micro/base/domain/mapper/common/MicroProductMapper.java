/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.infrastructure.entity.MicroProductSelectEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品Mapper接口
 *
 * @date 2022-10-11
 */
public interface MicroProductMapper {
    /**
     * 查询产品
     *
     * @param id 产品ID
     * @return 产品
     */
    MicroProduct selectMicroProductById(Long id);

    /**
     * 根据产品唯一码查询产品信息
     *
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
     * 新增产品
     *
     * @param microProduct 产品
     * @return 结果
     */
    int insertMicroProduct(MicroProduct microProduct);

    /**
     * 修改产品
     *
     * @param microProduct 产品
     * @return 结果
     */
    int updateMicroProduct(MicroProduct microProduct);

    /**
     * 删除产品
     *
     * @param id 产品ID
     * @return 结果
     */
    int deleteMicroProductById(Long id);

    /**
     * 批量删除产品
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroProductByIds(Long[] ids);

    List<MicroProductSelectEntity> selectMicroProductByName(String key, boolean mixed); 

    List<MicroSelectEntity> selectMicroProductByNameAndTech(String key); 

    /**
     * 批量插入新产品
     *
     * @param microProducts
     * @return
     */
    int insertMicroProductBatch(@Param("list") List<MicroProduct> microProducts);

    List<String> selectProductSeqAndCode(); 

    List<MicroProduct> selectExistMicroProductList(MicroProduct product); 

    Long selectTotalProductCount(); 

    List<MicroProduct> selectMasterProductWarnList(); 

    List<String> selectFilterProductBySeqList(@Param("list") List<String> verifyList); 

    List<MicroProduct> selectMicroProductListByItems(@Param("productSeqs") List<String> productSeqs, @Param("productCodes") List<String> productCodes, @Param("productIds") List<Long> productIds); 
}
