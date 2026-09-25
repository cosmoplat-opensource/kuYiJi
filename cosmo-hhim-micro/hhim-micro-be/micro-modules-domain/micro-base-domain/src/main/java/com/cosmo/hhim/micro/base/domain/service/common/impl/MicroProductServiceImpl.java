/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import cn.hutool.core.bean.BeanUtil;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBom;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProductEntity;
import com.cosmo.hhim.micro.base.domain.mapper.bom.MicroProductBomMapper;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroProductMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.entity.MicroProductSelectEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.enums.CreatedTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.HotKeyEnum;
import com.cosmo.hhim.micro.infrastructure.enums.ProductTypeEnum;
import com.cosmo.hhim.micro.infrastructure.events.CleanProductDataEvent;
import com.cosmo.hhim.micro.infrastructure.events.ProductNameChangeEvent;
import com.cosmo.hhim.micro.infrastructure.util.MicroRedisUtils;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import com.cosmo.hhim.micro.infrastructure.util.TextCalculateUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.cosmo.hhim.micro.infrastructure.util.TextCalculateUtil.sortBySimilarRadio;

/**
 * 产品Service业务层处理
 *
 * @date 2022-10-11
 */
@Slf4j
@Service
public class MicroProductServiceImpl implements IMicroProductService {

    private static final int SIMILARITY_THRESHOLD = 80;
    @Autowired
    private MicroProductMapper microProductMapper;
    @Autowired
    private MicroProductBomMapper bomMapper;
    @Autowired
    private MicroSupportUtil supportUtil;
    @Autowired
    private MicroRedisUtils microRedisUtils;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 查询产品
     *
     * @param id 产品ID
     * @return 产品
     */
    @Override
    public MicroProduct selectMicroProductById(Long id) {
        MicroProduct product = microProductMapper.selectMicroProductById(id);
        if (product != null) {
            product.setProductType(ProductTypeEnum.getEnumDesc(product.getProductType()));
        }
        return product;
    }

    /**
     * 根据产品编码查询产品信息
     *
     * @param productSeq
     * @return
     */
    @Override
    public MicroProduct selectMicroProductByProductSeq(String productSeq) {
        return microProductMapper.selectMicroProductByProductSeq(productSeq);
    }

    /**
     * 根据产品编码查询产品信息
     *
     * @param productCode
     * @return
     */
    @Override
    public MicroProduct selectMicroProductByProductCode(String productCode) {
        return microProductMapper.selectMicroProductByProductCode(productCode);
    }

    /**
     * 查询产品列表
     *
     * @param microProduct 产品
     * @return 产品
     */
    @Override
    public List<MicroProduct> selectMicroProductList(MicroProduct microProduct) {
        List<MicroProduct> list = microProductMapper.selectMicroProductList(microProduct);
//        microProducts.forEach(m -> m.setProductType(ProductTypeEnum.getEnumDesc(m.getProductType()))); 
        return TextCalculateUtil.sortBySimilarRadio(microProduct.getKey(), list, p -> p.getProductName().length());
    }

    /**
     * 查询所有产品列表
     *
     * @return 产品
     */
    @Override
    public List<MicroProduct> selectMicroAllProductList() {
        return microProductMapper.selectMicroProductList(null);
    }

    /**
     * 新增产品
     *
     * @param microProduct 产品
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MicroProduct insertMicroProduct(MicroProduct microProduct) {
        List<String> productSeqAndCodeList = microProductMapper.selectProductSeqAndCode();
        String productSeqNumber = supportUtil.getProductSeqNumber(productSeqAndCodeList);
        if (StringUtils.isEmpty(microProduct.getProductCode())) {
            microProduct.setProductCode(productSeqNumber);
        }
        MicroProduct existProduct = existProduct(microProduct);
        if (existProduct == null) {
            throw new CustomException("产品编码已存在(" + microProduct.getProductCode() + ") ");
        } else {
            microProduct.setProductSeq(productSeqNumber);
            if (StringUtils.isEmpty(microProduct.getProductType())) {
                microProduct.setProductType(ProductTypeEnum.CP.getCode());
            }
            microProduct.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
            microProduct.setLastUpdDate(new Date());
            if (StringUtils.isEmpty(microProduct.getCreatedType())) {
                microProduct.setCreatedType(CreatedTypeEnum.AUTO.getCode());
            }
            microProductMapper.insertMicroProduct(microProduct);
            try {
                MicroSelectEntity hotData = new MicroSelectEntity();
                hotData.setItemSeq(microProduct.getProductSeq());
                hotData.setItemCode(microProduct.getProductCode());
                hotData.setItemName(microProduct.getProductName());
                microRedisUtils.incrByHotData(HotKeyEnum.HIGH_FREQUENCY_PRODUCT, CommonConstants.HOT_KEY_SUFFIX_TIMES, hotData);
            } catch (Exception e) {
                log.error("插入产品热点数据失败:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            }
            return microProduct;
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public List<MicroProductEntity> saveMicroProducts(List<MicroProductEntity> productList) {
        if (CollectionUtils.isEmpty(productList)) {
            return Collections.emptyList();
        }
        Set<String> uniqCode = new HashSet<>();
        Iterator<MicroProductEntity> iterator = productList.iterator();
        while (iterator.hasNext()) {
            MicroProductEntity entity = iterator.next();
            if (!StringUtils.isEmpty(entity.getProductSeq())) {
                iterator.remove();
                continue;
            }
            if (!StringUtils.isEmpty(entity.getProductCode())) {
                if (!uniqCode.contains(entity.getProductCode())) {
                    uniqCode.add(entity.getProductCode());
                } else {
                    throw new CustomException("[" + entity.getProductCode() + "]" + "产品编码不能相同");
                }
            }
        }
        if (!CollectionUtils.isEmpty(productList)) {
            int oldSize = uniqCode.size();
            List<String> allProductCodes = microProductMapper.selectProductSeqAndCode();
            List<String> copyToList = BeanUtil.copyToList(allProductCodes, String.class);
            if (oldSize != 0) {
                allProductCodes.removeAll(uniqCode);
                uniqCode.retainAll(allProductCodes);
                if (uniqCode.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    uniqCode.forEach(u -> sb.append("[").append(u).append("],"));
                    throw new CustomException(sb + "产品编码已存在,请检查");
                }
            }
            LinkedList<String> productSeqQueue = new LinkedList<>(
                    supportUtil.getMultiProductSeqNumber(productList.size(), copyToList)
            );
            Date currentDate = new Date();
            List<MicroProduct> addList = new ArrayList<>();
            MicroProduct addProduct;
            String tenantCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
            Long userId = SecurityUtils.getUserId();
            for (MicroProductEntity product : productList) {
                addProduct = new MicroProduct();
                String productSeqNumber = productSeqQueue.poll();
                if (StringUtils.isEmpty(productSeqNumber)) {
                    productSeqNumber = supportUtil.getProductSeqNumber(copyToList);
                }
                product.setProductSeq(productSeqNumber);
                if (StringUtils.isEmpty(product.getProductCode())) {
                    product.setProductCode(productSeqNumber);
                }
                BeanUtil.copyProperties(product, addProduct);
                addProduct.setTenantCode(tenantCode);
                addProduct.setCreatedBy(String.valueOf(userId));
                addProduct.setCreatedDate(currentDate);
                addProduct.setLastUpdDate(currentDate);
                addList.add(addProduct);
            }
            microProductMapper.insertMicroProductBatch(addList);
            return productList;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 修改产品
     *
     * @param microProduct 产品
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroProduct(MicroProduct microProduct) {
        if (StringUtils.isEmpty(microProduct.getProductCode())) {
            List<String> productSeqAndCodeList = microProductMapper.selectProductSeqAndCode();
            String productSeqNumber = supportUtil.getProductSeqNumber(productSeqAndCodeList);
            microProduct.setProductCode(productSeqNumber);
        }
        MicroProduct existProduct = existProduct(microProduct);
        if (existProduct == null) {
            throw new CustomException(microProduct.getProductName() + "(" + microProduct.getProductCode() + ")该产品已存在 ");
        } else {
            String lastUpdBy = String.valueOf(SecurityUtils.getUserId());
            Date lastUpdDate = new Date();
            microProduct.setProductSeq(existProduct.getProductSeq());
            microProduct.setLastUpdBy(lastUpdBy);
            microProduct.setLastUpdDate(lastUpdDate);
            int i = microProductMapper.updateMicroProduct(microProduct);
            MicroProductBom bom = new MicroProductBom();
            bom.setLastUpdBy(lastUpdBy);
            bom.setLastUpdDate(lastUpdDate);
            bom.setProductType(microProduct.getProductType());
            bom.setProductionMode(microProduct.getProductionMode());
            bom.setProductSeq(existProduct.getProductSeq());
            bomMapper.updateBomByProductSeq(bom);
            updateProductHotData(microProduct);

            // 发送产品名称变更的事件 add by zyh 20230426
            if (StringUtils.hasText(microProduct.getProductName())) {
                Map<String, String> map = Maps.newHashMap();
                map.put("productSeq", microProduct.getProductSeq());
                map.put("productCode", microProduct.getProductCode());
                map.put("productName", microProduct.getProductName());
                applicationEventPublisher.publishEvent(new ProductNameChangeEvent(map));
            }

            return i;
        }
    }

    private void updateProductHotData(MicroProduct microProduct) {
        try {
            MicroSelectEntity entity = new MicroSelectEntity();
            entity.setItemSeq(microProduct.getProductSeq());
            entity.setItemCode(microProduct.getProductCode());
            entity.setItemName(microProduct.getProductName());
            supportUtil.updateProductHotData(entity);
        } catch (TaskRejectedException e) {
            log.error("update_product_hot_data was rejected!--->:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
    }

    /**
     * 是否存在产品
     *
     * @param product
     * @return
     */
    private MicroProduct existProduct(MicroProduct product) {
        MicroProduct verifyProduct = new MicroProduct();
        if (StringUtils.isEmpty(product.getProductCode())) {
            throw new CustomException("无法查询到该产品(" + product.getProductCode() + "),请刷新后重试");
        }
        verifyProduct.setProductCode(product.getProductCode());
        verifyProduct.setId(product.getId());
//        verifyProduct.setProductName(product.getProductName()); 
        List<MicroProduct> verifyList = microProductMapper.selectExistMicroProductList(verifyProduct);
        if (product.getId() != null) {
            List<MicroProduct> selfList = verifyList.stream().filter(v -> v.getId().equals(product.getId())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(selfList)) {
                if (verifyList.size() == 1) {
                    return selfList.get(0);
                } else {
                    return null;
                }
            }
            throw new CustomException("该产品(" + product.getProductCode() + ")已不存在,请刷新后重试");
        } else if (!CollectionUtils.isEmpty(verifyList)) {
            return null;
        } else {
            return product;
        }
    }


    /**
     * 批量删除产品
     *
     * @param ids 需要删除的产品ID
     * @return 结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int deleteMicroProductByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return 1;
        }
        List<MicroProduct> microProducts = microProductMapper.selectMicroProductListByItems(null, null, Arrays.asList(ids));
        if (CollectionUtils.isEmpty(microProducts)) {
            return 1;
        }
        microProductMapper.deleteMicroProductByIds(ids);
        List<MicroSelectEntity> existList = assembleSelectEntity(microProducts);
        applicationEventPublisher.publishEvent(new CleanProductDataEvent(existList));
        return 1;
    }

    private static List<MicroSelectEntity> assembleSelectEntity(List<MicroProduct> microProducts) {
        List<MicroSelectEntity> existList = microProducts.stream().map(m -> {
            MicroSelectEntity select = new MicroSelectEntity();
            select.setItemSeq(m.getProductSeq());
            select.setItemId(m.getId());
            select.setItemCode(m.getProductCode());
            select.setItemName(m.getProductName());
            return select;
        }).collect(Collectors.toList());
        return existList;
    }


    /**
     * 删除产品信息
     *
     * @param id 产品ID
     * @return 结果
     */
    @Override
    public int deleteMicroProductById(Long id) {
        return microProductMapper.deleteMicroProductById(id);
    }

    @Override
    public List<MicroProductSelectEntity> selectMicroProductByName(String key, boolean mixed) {
        return sortBySimilarRadio(key, microProductMapper.selectMicroProductByName(key, mixed), p -> (p.getItemName().length() + p.getItemCode().length()));
    }

    /**
     * 生成新的产品
     *
     * @param productName
     * @return
     */
    @Override
    public MicroProduct createNewProduct(String productName) {
        MicroProduct microProduct = new MicroProduct();
        String tenantCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        // 获取产品编码
        List<String> productSeqAndCodeList = microProductMapper.selectProductSeqAndCode();
        String productSeqNumber = supportUtil.getProductSeqNumber(productSeqAndCodeList);

        microProduct.setProductName(productName);
        microProduct.setProductCode(productSeqNumber);
        microProduct.setProductSeq(productSeqNumber);
        microProduct.setTenantCode(tenantCode);
        microProduct.setProductType(ProductTypeEnum.CP.getCode());
        microProduct.setCreateInfo();
        microProduct.setLastUpdDate(DateUtils.getNowDate());
        // 插入产品
        microProductMapper.insertMicroProduct(microProduct);

        return microProduct;
    }

    @Override
    public MicroProduct isOrNotHaveProduct(String productName) {
        Map<MicroProduct, Float> res = new HashMap<>(16);
        // 查询产品
        List<MicroProduct> microProducts = microProductMapper.selectMicroProductList(new MicroProduct());
        if (CollectionUtils.isEmpty(microProducts)) {
            return new MicroProduct();
        }
        microProducts.forEach(obj -> {
            // 计算相似度
            float similarity = TextCalculateUtil.getSimilarityByLevenshteinDistance(productName, obj.getProductName());
            if (similarity > SIMILARITY_THRESHOLD) {
                res.put(obj, similarity);
            }
        });
        if (CollectionUtils.isEmpty(res)) {
            return new MicroProduct();
        }

        Set<Map.Entry<MicroProduct, Float>> set = res.entrySet();
        List<Map.Entry<MicroProduct, Float>> list = new ArrayList<>(set);
        // 对list中的数据根据Value值进行排序
        Collections.sort(list, (o1, o2) -> {
            //降序排序
            return (int) (o2.getValue() - o1.getValue());
        });

        return list.get(0).getKey();
    }
}
