/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.planning.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.base.MicroCustomerAssembler;
import com.cosmo.hhim.micro.application.assembler.base.MicroExtendFieldRelationAssembler;
import com.cosmo.hhim.micro.application.assembler.base.MicroManufactureLineAssembler;
import com.cosmo.hhim.micro.application.assembler.base.MicroWorkShopAssembler;
import com.cosmo.hhim.micro.application.assembler.planning.MicroManufactureOrderAssembler;
import com.cosmo.hhim.micro.application.assembler.planning.MicroManufactureWorkOrderAssembler;
import com.cosmo.hhim.micro.application.assembler.planning.MicroManufactureTaskAssembler;
import com.cosmo.hhim.micro.application.dto.base.MicroProductBomDto;
import com.cosmo.hhim.micro.application.dto.planning.*;
import com.cosmo.hhim.micro.application.service.planning.IMicroManufactureOrderFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBom;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendFieldRelation;
import com.cosmo.hhim.micro.base.domain.entity.custom.SimpleExtendFieldInfo;
import com.cosmo.hhim.micro.base.domain.entity.customer.MicroCustomer;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroWorkShop;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainBindEntity;
import com.cosmo.hhim.micro.base.domain.service.bom.IMicroProductBomService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroExtendFieldRelationService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.base.domain.service.custom.IMicroExtendFieldService;
import com.cosmo.hhim.micro.base.domain.service.customer.IMicroCustomerService;
import com.cosmo.hhim.micro.base.domain.service.factory.IMicroManufactureLineService;
import com.cosmo.hhim.micro.base.domain.service.factory.IMicroWorkShopService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroTechnologyService;
import com.cosmo.hhim.micro.infrastructure.enums.ProductTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.ProductionModeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.BomAndTechTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.OrderOperateEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.OrderStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.WorkOrderStatusEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder;
import com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.mapper.manufacture.MicroManufactureOrderMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.task.MicroManufactureTaskMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.work.MicroManufactureWorkOrderMapper;
import com.cosmo.hhim.micro.planning.domain.service.manufacture.IMicroManufactureOrderService;
import com.cosmo.hhim.micro.planning.domain.service.task.IMicroManufactureTaskService;
import com.cosmo.hhim.micro.planning.domain.service.work.IMicroManufactureWorkOrderService;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生产订单Service业务层处理
 *
 * @author cosmo-hhim-open Team
 */
@Service
@Slf4j
public class MicroManufactureOrderFacadeServiceImpl implements IMicroManufactureOrderFacadeService
{
    @Autowired
    private IMicroProductBomService microProductBomService;

    @Autowired
    private IMicroManufactureOrderService microManufactureOrderService;

    @Autowired
    private IMicroCustomerService microCustomerService;

    @Autowired
    private MicroManufactureOrderMapper microManufactureOrderMapper;

    @Autowired
    private IMicroProductService productService;

    @Autowired
    private IMicroManufactureWorkOrderService workOrderService;

    @Autowired
    private IMicroExtendFieldRelationService microExtendFieldRelationService;

    @Autowired
    private IMicroWorkShopService microWorkShopService;

    @Autowired
    private IMicroManufactureLineService manufactureLineService;

    @Autowired
    private MicroManufactureWorkOrderMapper microManufactureWorkOrderMapper;

    @Autowired
    private IMicroTechnologyService microTechnologyService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private MicroManufactureTaskMapper microManufactureTaskMapper;

    @Autowired
    private IMicroFinishedProductStorageService microFinishedProductStorageService;

    @Autowired
    private IMicroExtendFieldService microExtendFieldService;

    @Autowired
    private IMicroManufactureTaskService microManufactureTaskService;

    /**
     * 查询生产订单
     * 
     * @param id 生产订单ID
     * @return 生产订单
     */
    @Override
    public MicroManufactureOrderDto selectMicroManufactureOrderById(Long id)
    {
        return MicroManufactureOrderAssembler.convertDomain2DTO(microManufactureOrderService.selectMicroManufactureOrderById(id));
    }

    /**
     * 查询生产订单列表
     * 
     * @param microManufactureOrder 生产订单
     * @return 生产订单
     */
    @Override
    public List<MicroManufactureOrderDto> selectMicroManufactureOrderList(MicroManufactureOrderDto microManufactureOrder)
    {
        List<MicroManufactureOrder> orderList = microManufactureOrderService.selectMicroManufactureOrderListByCondition(MicroManufactureOrderAssembler.convert2Condition(microManufactureOrder));
        return MicroManufactureOrderAssembler.convertDomains2DTOs4Page(orderList);
    }

    /**
     * 新增生产订单
     *    1.校验订单信息
     *    2.生成订单
     *      a.生成客户
     *      b.自定义字段关系绑定
     *      c.生成物料
     *      c.生成订单
     * @param microManufactureOrders 生产订单
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult insertMicroManufactureOrder(List<MicroManufactureOrderDto> microManufactureOrders)
    {
        //1.校验订单信息
        validManufactureOrder4Insert(microManufactureOrders);
        //基础信息
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        Long userId = SecurityUtils.getUserId();
        String owCode = null;
        //2.生成订单信息
        try {
            //a.如果客户编码为空，为新增客户，生成客户信息
            if (StringUtils.isEmpty(microManufactureOrders.get(0).getOwCode())&&StringUtils.isNotEmpty(microManufactureOrders.get(0).getOwName())){
                MicroCustomer customer = MicroCustomerAssembler.convert2Customer4Insert(microManufactureOrders.get(0).getOwName(),userId,tenantCode);
                microCustomerService.insertMicroCustomer(customer);
                owCode = customer.getCustomerCode();
            }
            //b.自定义字段关系绑定
            if (CollectionUtil.isNotEmpty(microManufactureOrders.get(0).getExtendFieldRelationList())){
                List<MicroExtendFieldRelation> extendFieldRelations = MicroExtendFieldRelationAssembler.convertDTOS2Domains4Insert(microManufactureOrders.get(0).getExtendFieldRelationList(),userId,tenantCode);
                microExtendFieldRelationService.batchInsert(extendFieldRelations);
            }
            //循环新增产品和订单
            for (MicroManufactureOrderDto microManufactureOrder:microManufactureOrders){
                MicroManufactureOrder manufactureOrder = MicroManufactureOrderAssembler.convertDto2Domain4Insert(microManufactureOrder,userId,tenantCode);
                //c.生成新物料
                //数据传输层转业务层数据
                if (StringUtils.isEmpty(microManufactureOrder.getProductSeq())){
                    MicroProduct productTemp = this.saveProduct(microManufactureOrder.getProductCode(), microManufactureOrder.getProductName(), ProductTypeEnum.CP.getCode());
                    manufactureOrder.setProductSeq(productTemp.getProductSeq());
                }

                if (StringUtils.isNotEmpty(owCode)){
                    manufactureOrder.setOwCode(owCode);
                }
                //d.生成订单
                microManufactureOrderService.insertMicroManufactureOrder(manufactureOrder);
            }
        }catch (Exception e){
            log.error("新增订单失败,原因为:{}",e);
            return AjaxResult.error();
        }
        return AjaxResult.success();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microManufactureOrders 生产订单
     * @return void
     **/
    private void validManufactureOrder4Insert(List<MicroManufactureOrderDto> microManufactureOrders) {
        if (CollectionUtils.isEmpty(microManufactureOrders)){
            throw new CustomException("参数不能为空");
        }

        microManufactureOrders.forEach(order->{
            if (!microManufactureOrderService.isUniqueOrder(order.getOrderNo())){
                throw new CustomException("订单编码已存在");
            }
            if (StringUtils.isEmpty(order.getProductName())){
                throw new CustomException("订单的产品不能为空");
            }
            //客户编码为空 且客户名称不为空  且客户名称已存在
            if (StringUtils.isEmpty(order.getOwCode())&&!microCustomerService.isUniqueCustomerName(order.getOwName())){
                throw new CustomException("客戶名称已存在");
            }
        });
    }

    /**
     * 修改生产订单
     * 
     * @param microManufactureOrder 生产订单
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroManufactureOrder(MicroManufactureOrderDto microManufactureOrder)
    {
        //1.校验数据
        validManufactureOrder4Update(microManufactureOrder);
        //基础信息
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        Long userId = SecurityUtils.getUserId();
        //2.业务数据处理
        MicroManufactureOrder manufactureOrder = MicroManufactureOrderAssembler.convertDto2Domain4Update(microManufactureOrder,userId);
        //a.如果客户编码为空，为新增客户，生成客户信息
        if (StringUtils.isEmpty(microManufactureOrder.getOwCode())&&StringUtils.isNotEmpty(microManufactureOrder.getOwName())){
            MicroCustomer customer = MicroCustomerAssembler.convert2Customer4Insert(microManufactureOrder.getOwName(),userId,tenantCode);
            microCustomerService.insertMicroCustomer(customer);
            manufactureOrder.setOwCode(customer.getCustomerCode());
        }
        //b.自定义字段关系绑定
        if (CollectionUtil.isNotEmpty(microManufactureOrder.getExtendFieldRelationList())){
            List<MicroExtendFieldRelation> extendFieldRelations = MicroExtendFieldRelationAssembler.convertDTOS2Domains4Insert(microManufactureOrder.getExtendFieldRelationList(),userId,tenantCode);
            microExtendFieldRelationService.batchInsert(extendFieldRelations);
        }
        //b.生成新物料
        if (StringUtils.isEmpty(microManufactureOrder.getProductSeq())){
            MicroProduct product = new MicroProduct();
            product.setProductCode(microManufactureOrder.getProductCode());
            product.setProductName(microManufactureOrder.getProductName());
            product.setProductionMode(ProductionModeEnum.SELF_CONTROL.getCode());
            product.setProductType(ProductTypeEnum.BCP.getCode());
            productService.insertMicroProduct(product);
            manufactureOrder.setProductSeq(product.getProductSeq());
        }

        return microManufactureOrderService.updateWithFiledNull(manufactureOrder);
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microManufactureOrder 生产订单
     * @return void
     **/
    private void validManufactureOrder4Update(MicroManufactureOrderDto microManufactureOrder) {
        if (ObjectUtil.isEmpty(microManufactureOrder)){
            throw new CustomException("参数不能为空");
        }
        if (StringUtils.isEmpty(microManufactureOrder.getProductName())){
            throw new CustomException("订单的产品不能为空");
        }
        if (StringUtils.isEmpty(microManufactureOrder.getOwCode())&&!microCustomerService.isUniqueCustomerName(microManufactureOrder.getOwName())){
            throw new CustomException("客戶名称已存在");
        }
    }

    /**
     * 批量删除生产订单
     * 
     * @param ids 需要删除的生产订单ID
     * @return 结果
     */
    @Override
    public int deleteMicroManufactureOrderByIds(Long[] ids)
    {
        return microManufactureOrderService.deleteMicroManufactureOrderByIds(ids);
    }


    /**
     * 生产订单预排产页详情信息
     * @param microManufactureOrderDto 生产订单
     * @return 结果
     */
    @Override
    public MicroManufactureOrderDto productionSchedulingDetails(MicroManufactureOrderDto microManufactureOrderDto){
        MicroManufactureOrderDto result=new MicroManufactureOrderDto();
        MicroManufactureOrder microManufactureOrder=microManufactureOrderMapper.productionSchedulingDetails(microManufactureOrderDto.getId());
        BeanUtils.copyProperties(microManufactureOrder,result);
        List<MicroProductBom> microProductBomList=microProductBomService.getBomListByScheduling(microManufactureOrder.getProductSeq(),microManufactureOrder.getPlanNum());
        List<MicroProductBomDto> microProductBomDtoList = BeanUtil.copyToList(microProductBomList, MicroProductBomDto.class);
        // 获取bom的库存数量 (主bom和子bom)
        if (CollectionUtil.isNotEmpty(microProductBomDtoList)) {
            List<String> productSeqList = microProductBomDtoList.stream()
                    .map(MicroProductBomDto::getProductSeq)
                    .collect(Collectors.toList());
            Map<String, BigDecimal> stockMap = new HashMap<>(16);
            if (CollectionUtil.isNotEmpty(productSeqList)) {
                List<MicroFinishedProductStorage> microFinishedProductStorageList = microFinishedProductStorageService.selectMicroFinishedProductStorageByProductSeqList(productSeqList);
                if (CollectionUtil.isNotEmpty(microFinishedProductStorageList)) {
                    stockMap = microFinishedProductStorageList.stream()
                            .collect(Collectors.groupingBy(MicroFinishedProductStorage::getProductSeq,
                                    Collectors.collectingAndThen(Collectors.toList(), value -> value.get(0).getNum())));
                }
            }
            for (MicroProductBomDto microProductBomDto : microProductBomDtoList) {
                BigDecimal stockNum = BigDecimal.ZERO;
                if (!stockMap.isEmpty()) {
                    stockNum = stockMap.get(microProductBomDto.getProductSeq());
                }
                microProductBomDto.setStockNum(stockNum);
            }
            BigDecimal materStockNum = stockMap.get(microManufactureOrder.getProductSeq());
            if (materStockNum != null) {
                result.setStockNum(materStockNum);
            } else {
                result.setStockNum(BigDecimal.ZERO);
            }
            // 没有子bom的情况
        } else {
            MicroFinishedProductStorage finishedProductStorage = microFinishedProductStorageService.selectMicroFinishedProductStorageByProductSeq(result.getProductSeq());
            if (finishedProductStorage == null) {
                result.setStockNum(BigDecimal.ZERO);
            } else {
                result.setStockNum(finishedProductStorage.getNum());
            }
        }
        result.setSecondBomDtoList(microProductBomDtoList);
        return result;
    }

    /**
     * 生产订单排产
     * @param microManufactureOrderDto 生产订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void productionScheduling(MicroManufactureOrderDto microManufactureOrderDto){
        log.info("请求的参数为:{}", JSONObject.toJSONString(microManufactureOrderDto));
        // 校验工单信息
        this.validManufactureWorkOrderInfoFromOrder(microManufactureOrderDto);
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        Long userId = SecurityUtils.getUserId();

        //1、更新订单的状态
        MicroManufactureOrder microManufactureOrder=new MicroManufactureOrder();
        microManufactureOrder.setId(microManufactureOrderDto.getId());
        microManufactureOrder.setOrderStatus(OrderStatusEnum.TO_BE_PRODCED.getCode());
        microManufactureOrder.setLastUpdBy(SecurityUtils.getUsername());
        microManufactureOrder.setLastUpdDate(new Date());
        microManufactureOrder.setProductSeq(microManufactureOrderDto.getProductSeq());
        microManufactureOrder.setProductName(microManufactureOrderDto.getProductName());
        microManufactureOrder.setProductCode(microManufactureOrderDto.getProductCode());
        microManufactureOrderMapper.updateMicroManufactureOrder(microManufactureOrder);
        //todo 2、新增自定义字段 & 生成工单
        if (CollectionUtil.isNotEmpty(microManufactureOrderDto.getExtendFieldRelationList())){
            microExtendFieldRelationService.batchInsert(MicroExtendFieldRelationAssembler.convertDTOS2Domains4Insert(microManufactureOrderDto.getExtendFieldRelationList(),userId,tenantCode));
        }
        // 存储新增的产品信息
        if (CollectionUtil.isNotEmpty(microManufactureOrderDto.getWorkOrderDtoList())){
            microManufactureOrderDto.getWorkOrderDtoList().forEach(workOrder -> {
                if (StringUtils.isEmpty(workOrder.getProductSeq())){
                    MicroProduct productTemp = this.saveProduct(workOrder.getProductCode(), workOrder.getProductName(), ProductTypeEnum.BCP.getCode());
                    workOrder.setProductSeq(productTemp.getProductSeq());
                    workOrder.setProductionMode(productTemp.getProductionMode());
                    workOrder.setProductType(productTemp.getProductType());
                }
                saveManufactureWorkShopAndLine(workOrder);
            });
        }
        // 生成工单
        List<MicroManufactureWorkOrder> workOrderList= MicroManufactureWorkOrderAssembler.covertDTOs2Domains4Insert(microManufactureOrderDto.getWorkOrderDtoList(),userId,tenantCode);
        if (CollectionUtil.isNotEmpty(workOrderList)){
            workOrderService.saveWorkOrders(workOrderList);
        }
        //3、生成BOM
        if(BomAndTechTypeEnum.DRAFT.getCode().equals(microManufactureOrderDto.getBomType())){
            // 工单数量必须大于1
            if(microManufactureOrderDto.getWorkOrderDtoList().size() > 1) {
                Map<String,List<MicroManufactureWorkOrderDto>> map = microManufactureOrderDto.getWorkOrderDtoList().stream().collect(Collectors.groupingBy(MicroManufactureWorkOrderDto::getProductSeq));
                MicroProductBom microProductBom=new MicroProductBom();
                microProductBom.setProductSeq(microManufactureOrderDto.getProductSeq());
                microProductBom.setBomType(BomAndTechTypeEnum.DRAFT.getCode());
                microProductBom.setProductNumber(BigDecimal.ONE);
                microProductBom.setProductType(microManufactureOrderDto.getProductType());
                microProductBom.setProductionMode(microManufactureOrderDto.getProductionMode());

                List<MicroManufactureWorkOrderDto> tempList=map.get(microManufactureOrderDto.getProductSeq());
                BigDecimal productNumber=tempList.stream().map(MicroManufactureWorkOrderDto::getWorkOrderNum).reduce(BigDecimal.ZERO,BigDecimal::add);
                map.remove(microManufactureOrderDto.getProductSeq());
                List<MicroProductBom> secondBomList=new ArrayList<>();
                Set<Map.Entry<String, List<MicroManufactureWorkOrderDto>>> tempEntries = map.entrySet();
                Iterator<Map.Entry<String, List<MicroManufactureWorkOrderDto>>> tempIterator = tempEntries.iterator();
                while (tempIterator.hasNext()){
                    Map.Entry<String, List<MicroManufactureWorkOrderDto>> entryTemp = tempIterator.next();
                    MicroProductBom bomEntity=new MicroProductBom();
                    bomEntity.setProductSeq(entryTemp.getKey());
                    bomEntity.setBomType(BomAndTechTypeEnum.DRAFT.getCode());
                    bomEntity.setProductType(entryTemp.getValue().get(0).getProductType());
                    bomEntity.setProductionMode(entryTemp.getValue().get(0).getProductionMode());
                    BigDecimal numberTemp = entryTemp.getValue().stream()
                            .map(MicroManufactureWorkOrderDto::getWorkOrderNum)
                            .reduce(BigDecimal.ZERO,BigDecimal::add);
                    bomEntity.setProductNumber(numberTemp.divide(productNumber,4, RoundingMode.HALF_UP));
                    secondBomList.add(bomEntity);
                }
                // 双重验证，生产订单排产生成bom的时候，需要存在二级bom
                if (CollectionUtil.isNotEmpty(secondBomList)) {
                    microProductBom.setSecondBomList(secondBomList);
                    microProductBomService.save(microProductBom);
                }
            }
        }
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microManufactureOrder 生产订单
     * @return int
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int operate(MicroManufactureOrderDto microManufactureOrder) {
        validManufactureOrder4Operate(microManufactureOrder);
        MicroManufactureOrder manufactureOrder = MicroManufactureOrderAssembler.convertDto2Domain4Update(microManufactureOrder,SecurityUtils.getUserId());
        return microManufactureOrderService.operate(manufactureOrder,microManufactureOrder.getOperateFlag());
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microManufactureOrder 生产订单
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.planning.MicroManufactureOrderDto>
     **/
    @Override
    public List<MicroManufactureOrderDto> selectOrderStatusCount(MicroManufactureOrderDto microManufactureOrder) {
        MicroManufactureOrder condition = MicroManufactureOrderAssembler.convert2Condition(microManufactureOrder);
        List<MicroManufactureOrder> orderList = microManufactureOrderService.selectWorkStatusCount(condition);
        return MicroManufactureOrderAssembler.convertDomains2DTOs(orderList);
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microManufactureOrder 生产订单
     * @return com.cosmo.hhim.common.core.web.domain.AjaxResult
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult adjust(MicroManufactureOrderDto microManufactureOrder) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(microManufactureOrder));
        //校验数据
        this.validManufactureWorkOrderInfoFromOrder(microManufactureOrder);
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        try {
            //1.修改订单
            microManufactureOrderService.updateWithFiledNull(MicroManufactureOrderAssembler.convertDto2Domain4Update(microManufactureOrder,userId));
            //2.区分新增的工单和调整的工单
            Map<String,List<MicroManufactureWorkOrderDto>> workOrderMap = classifyWorkOrder(microManufactureOrder.getWorkOrderDtoList());
            //3.绑定工单自定义字段
            if (CollectionUtil.isNotEmpty(microManufactureOrder.getExtendFieldRelationList())){
                List<MicroExtendFieldRelation> extendFieldRelations = MicroExtendFieldRelationAssembler.convertDTOS2Domains4Insert(microManufactureOrder.getExtendFieldRelationList(),userId,tenantCode);
                microExtendFieldRelationService.batchInsert(extendFieldRelations);
            }
            //4.查询原始工单信息，为了过滤出要删除的工单信息
            MicroManufactureWorkOrder queryParam = new MicroManufactureWorkOrder();
            queryParam.setOrderNo(microManufactureOrder.getOrderNo());
            queryParam.setActiveFlag("1");
            List<MicroManufactureWorkOrder> originalManufactureWorkOrderList = workOrderService.selectMicroManufactureWorkOrderList(queryParam);
            // 获取编辑的工单列表，用于过滤
            List<MicroManufactureWorkOrderDto> updateWorkOrderList = workOrderMap.get(OrderOperateEnum.ORDER_OPERATE_UPDATE.getCode());
            Long[] ids;
            if (CollectionUtils.isEmpty(updateWorkOrderList)) {
                ids = originalManufactureWorkOrderList.stream().map(MicroManufactureWorkOrder::getId)
                        .collect(Collectors.toList()).toArray(new Long[0]);
            } else {
                List<Long> updateIds = updateWorkOrderList.stream().map(MicroManufactureWorkOrderDto::getId).collect(Collectors.toList());
                ids = originalManufactureWorkOrderList.stream()
                        .map(MicroManufactureWorkOrder::getId)
                        .filter(obj -> !updateIds.contains(obj))
                        .collect(Collectors.toList())
                        .toArray(new Long[0]);
            }
            if (ids.length > 0) {
                workOrderService.deleteMicroManufactureWorkOrderByIds(ids);
            }
            //5.调整工单
            workOrderService.updateWorkOrders(MicroManufactureWorkOrderAssembler.covertDTOs2Domains4Update(updateWorkOrderList, userId));
            //6.新增工单
            workOrderService.saveWorkOrders(MicroManufactureWorkOrderAssembler.covertDTOs2Domains4Insert(workOrderMap.get(OrderOperateEnum.ORDER_OPERATE_ADD.getCode()),userId,tenantCode));
        }catch (Exception e){
            return AjaxResult.error("操作失败");
        }
        return AjaxResult.success("操作成功");
    }

    /**
     * 生产订单下发
     *
     * @param orderNo 订单号
     * @return 是否下发成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean productiveOrderIssued(String orderNo) {
        log.info("请求的参数为:orderNo:{}", orderNo);
        // 根据生产订单查询工单信息
        MicroManufactureWorkOrder queryParamByWorkOrder = new MicroManufactureWorkOrder();
        queryParamByWorkOrder.setOrderNo(orderNo);
        queryParamByWorkOrder.setActiveFlag("1");
        List<MicroManufactureWorkOrder> microManufactureWorkOrderList = workOrderService.selectMicroManufactureWorkOrderList(queryParamByWorkOrder);
        if (CollectionUtils.isEmpty(microManufactureWorkOrderList)) {
            throw new CustomException("该生产订单还未排期");
        }
        // 查询产品的工艺信息
        List<String> productSeqList = microManufactureWorkOrderList.stream().map(MicroManufactureWorkOrder::getProductSeq).distinct().collect(Collectors.toList());
        List<MicroProcessChainBindEntity> chainBindEntityList = microTechnologyService.selectMicroTechChainByProductIdsOrSeqs(null, productSeqList, null);
        // 判断工单数量和工单产品是否有工艺
        if (microManufactureWorkOrderList.size() == 1 && !CollectionUtils.isEmpty(chainBindEntityList)) {
            // 可以执行工单下发的流程, 整合工单下发所需要的参数
            Long userId = SecurityUtils.getUserId();
            String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
            MicroManufactureWorkOrder workOrderTemp = microManufactureWorkOrderList.get(0);

            // 更新生产订单信息
            MicroManufactureOrder microManufactureOrder = new MicroManufactureOrder();
            microManufactureOrder.setOrderNo(orderNo);
            microManufactureOrder.setOrderStatus(OrderStatusEnum.IN_PRODUCTION.getCode());
            microManufactureOrder.setLastUpdBy(String.valueOf(userId));
            microManufactureOrder.setLastUpdDate(new Date());
            microManufactureOrderMapper.updateOrderByNo(microManufactureOrder);

            // 更新工单状态
            MicroManufactureWorkOrder microManufactureWorkOrder = new MicroManufactureWorkOrder();
            microManufactureWorkOrder.setId(workOrderTemp.getId());
            microManufactureWorkOrder.setWorkOrderStatus(WorkOrderStatusEnum.IN_PRODUCTION.getCode());
            microManufactureWorkOrder.setLastUpdBy(String.valueOf(userId));
            microManufactureWorkOrder.setLastUpdDate(new Date());
            workOrderService.updateMicroManufactureWorkOrder(microManufactureWorkOrder);

            // 构建生产任务
            List<MicroManufactureTask> microManufactureTaskList = new ArrayList<>();
            // 要过滤一下工艺: (c -> a, sort 1) (c -> b, sort 2), 其实是一个工序
            chainBindEntityList = chainBindEntityList.stream()
                    .collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(MicroProcessChainBindEntity::getProcessSeq))), ArrayList::new));
            for (MicroProcessChainBindEntity microProcessChainBindEntity : chainBindEntityList) {
                String taskNo = redisCache.incrby("TO", 1);
                MicroManufactureTask temp = MicroManufactureTaskAssembler.cover2DomainByWorkOrderAndTech(workOrderTemp, microProcessChainBindEntity, taskNo, userId.toString(), tenantCode);
                microManufactureTaskList.add(temp);
            }
            microManufactureTaskMapper.insertList(microManufactureTaskList);
            return true;
        }
        return false;
    }

    /**
     * 生产订单详情页面 v2
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @Override
    public DetailMicroManufactureOrderInfo getDetailInfo(String orderNo) {
        log.info("请求的参数为:orderNo:{}", orderNo);
        // 生产订单信息
        MicroManufactureOrder microManufactureOrder = microManufactureOrderService.selectMicroManufactureOrderByOrderNo(orderNo);
        DetailMicroManufactureOrderInfo result = MicroManufactureOrderAssembler.covertDetailMicroManufactureOrderInfo(microManufactureOrder);
        // 工单信息
        MicroManufactureWorkOrder queryParam = new MicroManufactureWorkOrder();
        queryParam.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        queryParam.setOrderNo(microManufactureOrder.getOrderNo());
        List<MicroManufactureWorkOrder> microManufactureWorkOrderList = workOrderService.selectMicroManufactureWorkOrderListByCondition(queryParam);
        if (CollectionUtils.isNotEmpty(microManufactureWorkOrderList)) {
            List<WorkOrderInfoFromDetailOrder> workOrderList = MicroManufactureWorkOrderAssembler.covert2WorkOrderInfoFromDetailOrder(microManufactureWorkOrderList);
            result.setWorkOrderList(workOrderList);
        }
        // 自定义字段
        if (StringUtils.isNotEmpty(microManufactureOrder.getExtendContent())) {
            List<SimpleExtendFieldInfo> simpleExtendFieldInfoList = microExtendFieldService.selectSimpleExtendFieldInfoList(microManufactureOrder.getExtendContent());
            result.setExtendFieldInfoList(simpleExtendFieldInfoList);
        }
        return result;
    }

    /**
     * 订单进度追踪列表
     *
     * @param orderProgressParam 订单进度追踪入参
     * @return 订单进度列表
     */
    @Override
    public List<OrderProgressDetailInfo> getOrderProgressList(OrderProgressParam orderProgressParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(orderProgressParam));

        // 存放结果
        List<OrderProgressDetailInfo> result = new ArrayList<>();

        // 查询生产订单
        MicroManufactureOrder condition = MicroManufactureOrderAssembler.covertOrderProgressParam2MicroManufactureOrderDomain(orderProgressParam);
        List<MicroManufactureOrder> microManufactureOrderList = microManufactureOrderMapper.selectMicroManufactureOrderListByCondition(condition);
        if (CollectionUtil.isEmpty(microManufactureOrderList)) {
            return Collections.EMPTY_LIST;
        }
        // 查询订单对应的主工单信息
        List<String> orderNoList = microManufactureOrderList.stream().map(MicroManufactureOrder::getOrderNo).collect(Collectors.toList());
        List<MicroManufactureWorkOrder> microManufactureWorkOrderList = microManufactureWorkOrderMapper.selectMicroMainManufactureWorkOrderByOrderNoList(orderNoList);
        Map<String, List<MicroManufactureWorkOrder>> map = new HashMap<>(16);
        // 找到主工单对应的工序任务信息，并计算进度
        if (CollectionUtil.isNotEmpty(microManufactureWorkOrderList)) {
            map = microManufactureWorkOrderList.stream().collect(Collectors.groupingBy(MicroManufactureWorkOrder::getOrderNo));
        }
        for (MicroManufactureOrder microManufactureOrder : microManufactureOrderList) {
            OrderProgressDetailInfo temp = new OrderProgressDetailInfo();
            temp.setOrderNo(microManufactureOrder.getOrderNo());
            temp.setDeliveryDate(microManufactureOrder.getDeliveryDate());
            temp.setOrderPlanNum(microManufactureOrder.getPlanNum());
            temp.setOrderStatus(microManufactureOrder.getOrderStatus());
            temp.setOwName(microManufactureOrder.getOwName());
            temp.setOwCode(microManufactureOrder.getOwCode());
            temp.setProductCode(microManufactureOrder.getProductCode());
            temp.setProductName(microManufactureOrder.getProductName());
            temp.setProductSeq(microManufactureOrder.getProductSeq());
            temp.setProductUnit(microManufactureOrder.getUnit());

            if (!map.isEmpty()) {
                // 正常情况下一个订单只有一个主工单
                List<MicroManufactureWorkOrder> workOrderList = map.get(microManufactureOrder.getOrderNo());
                if (CollectionUtil.isNotEmpty(workOrderList)) {
                    String workOrderNo = workOrderList.get(0).getWorkOrderNo();
                    temp.setWorkOrderNo(workOrderNo);

                    // 查询工序任务
                    MicroManufactureTask param = new MicroManufactureTask();
                    param.setWorkOrderNo(workOrderNo);
                    List<MicroManufactureTask> microManufactureTaskList = microManufactureTaskService.selectMicroManufactureTaskList(param);
                    if (CollectionUtil.isNotEmpty(microManufactureTaskList)) {
                        temp.setTaskDtoList(MicroManufactureTaskAssembler.convertDomains2DTOs(microManufactureTaskList));
                    }
                }
            }
            result.add(temp);
        }
        return MicroPageUtils.listToPage(microManufactureOrderList, result);
    }

    /**
     * @author cosmo-hhim-open Team
     * @param workOrderDtoList 工单列表
     * @return java.util.Map<java.lang.String,java.util.List<...>>
     **/
    private Map<String, List<MicroManufactureWorkOrderDto>> classifyWorkOrder(List<MicroManufactureWorkOrderDto> workOrderDtoList) {
        Map<String, List<MicroManufactureWorkOrderDto>> resultMap = Maps.newHashMap();
        List<MicroManufactureWorkOrderDto> addList = new ArrayList<>();
        List<MicroManufactureWorkOrderDto> updateList = new ArrayList<>();
        resultMap.put(OrderOperateEnum.ORDER_OPERATE_ADD.getCode(),addList);
        resultMap.put(OrderOperateEnum.ORDER_OPERATE_UPDATE.getCode(),updateList);
        workOrderDtoList.forEach(workOrder->{
            if (ObjectUtil.isEmpty(workOrder.getId())){
                addList.add(workOrder);
            }else {
                updateList.add(workOrder);
            }
        });
        return resultMap;
    }

    private void validManufactureOrder4Adjust(MicroManufactureOrderDto microManufactureOrder) {
        if (ObjectUtil.isEmpty(microManufactureOrder)){
            throw new CustomException("参数不能为空");
        }
        if (StringUtils.isEmpty(microManufactureOrder.getOrderNo())){
            throw new CustomException("订单编号不能为空");
        }
        if (CollectionUtils.isEmpty(microManufactureOrder.getWorkOrderDtoList())){
            throw new CustomException("工单列表不能为空");
        }
    }

    private void validManufactureOrder4Operate(MicroManufactureOrderDto microManufactureOrder) {
        if (Objects.isNull(microManufactureOrder)){
            throw new CustomException("参数不能为空");
        }
        if (StringUtils.isEmpty(microManufactureOrder.getOrderNo())){
            throw new CustomException("订单编号不能为空");
        }
        if (StringUtils.isEmpty(microManufactureOrder.getOperateFlag())){
            throw new CustomException("未指明具体操作");
        }
    }

    /**
     * 生产订单排产和调整校验信息
     *
     * @param microManufactureOrder 生产订单
     */
    private void validManufactureWorkOrderInfoFromOrder(MicroManufactureOrderDto microManufactureOrder) {
        this.validManufactureOrder4Adjust(microManufactureOrder);
        // 订单调整时也会新增工单(id为null)，也有可能会手写工单
        List<MicroManufactureWorkOrderDto> addWorkOrderList = microManufactureOrder.getWorkOrderDtoList()
                .stream().filter(obj -> ObjectUtil.isEmpty(obj.getId()))
                .collect(Collectors.toList());

        if (CollectionUtil.isNotEmpty(addWorkOrderList)) {
            // 校验手写工单号是否有重复(只能查询数据库中已存在的工单号)
            List<String> workOrderNoList = new ArrayList<>();
            addWorkOrderList.forEach(obj -> {
                if (StringUtils.isNotEmpty(obj.getWorkOrderNo())) {
                    if (!workOrderService.isUniqueWorkOrderNo(obj.getWorkOrderNo())) {
                        throw new CustomException("工单号:" + obj.getWorkOrderNo() + "已存在");
                    }
                    workOrderNoList.add(obj.getWorkOrderNo());
                }
            });

            // 校验本次手动添加的工单号是否有重复
            if (CollectionUtil.isNotEmpty(workOrderNoList)) {
                // 手写工单号去重
                long count = workOrderNoList.stream().distinct().count();
                // 如果数量与原工单号list大小不一致
                if (workOrderNoList.size() != (int) count) {
                    throw new CustomException("工单号输入重复");
                }
            }
        }
    }

    /**
     * @author cosmo-hhim-open Team
     * @param productCode 产品编码
     * @return void
     **/
    private MicroProduct saveProduct(String productCode,String productName,String productType) {
            MicroProduct product = new MicroProduct();
            product.setProductCode(productCode);
            product.setProductName(productName);
            product.setProductionMode(ProductionModeEnum.SELF_CONTROL.getCode());
            product.setProductType(productType);
            productService.insertMicroProduct(product);
            return product;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param workOrder 工单
     * @return void
     **/
    private void saveManufactureWorkShopAndLine(MicroManufactureWorkOrderDto workOrder) {
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        //如果车间编码为空，则新增车间
        if (StringUtils.isEmpty(workOrder.getWshopCode()) && StringUtils.isNotEmpty(workOrder.getWshopName())){
            MicroWorkShop workShop = MicroWorkShopAssembler.convert2Domain(workOrder.getWshopName(),userId,tenantCode);
            microWorkShopService.insertMicroWorkshop(workShop);
            workOrder.setWshopCode(workShop.getWshopCode());
        }
        // 如果产线编码为空，则新增产线
        if (StringUtils.isEmpty(workOrder.getMlineCode()) && StringUtils.isNotEmpty(workOrder.getMlineName())) {
            MicroManufactureLine manufactureLine = MicroManufactureLineAssembler.convert2Domain(workOrder.getWshopCode(),workOrder.getWshopName(), workOrder.getMlineName(), userId, tenantCode);
            manufactureLineService.insertMicroManufactureLine(manufactureLine);
            workOrder.setMlineCode(manufactureLine.getMlineCode());
        }
    }
}
