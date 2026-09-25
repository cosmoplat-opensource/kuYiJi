/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.planning.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.base.MicroExtendFieldRelationAssembler;
import com.cosmo.hhim.micro.application.assembler.base.MicroManufactureLineAssembler;
import com.cosmo.hhim.micro.application.assembler.base.MicroWorkShopAssembler;
import com.cosmo.hhim.micro.application.assembler.planning.MicroManufactureWorkOrderAssembler;
import com.cosmo.hhim.micro.application.assembler.planning.MicroManufactureTaskAssembler;
import com.cosmo.hhim.micro.application.dto.planning.*;
import com.cosmo.hhim.micro.application.dto.tech.MicroSaveSingleChainDTO;
import com.cosmo.hhim.micro.application.dto.tech.MicroSaveSingleTechDTO;
import com.cosmo.hhim.micro.application.service.planning.IMicroManufactureWorkOrderFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBom;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProductEntity;
import com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendFieldRelation;
import com.cosmo.hhim.micro.base.domain.entity.custom.SimpleExtendFieldInfo;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroWorkShop;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroSaveChainNodeEntity;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroSingleTechChainEntity;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.bom.IMicroProductBomService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroExtendFieldRelationService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.base.domain.service.custom.IMicroExtendFieldService;
import com.cosmo.hhim.micro.base.domain.service.factory.IMicroManufactureLineService;
import com.cosmo.hhim.micro.base.domain.service.factory.IMicroWorkShopService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroTechnologyService;
import com.cosmo.hhim.micro.infrastructure.enums.IsFirstProcessEnum;
import com.cosmo.hhim.micro.infrastructure.enums.ProductTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.ProductionModeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.*;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroPickMaterials;
import com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.mapper.manufacture.MicroManufactureOrderMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.manufacture.MicroPickMaterialsMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.task.MicroManufactureTaskMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.work.MicroManufactureWorkOrderMapper;
import com.cosmo.hhim.micro.planning.domain.service.work.IMicroManufactureWorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生产工单Service业务层处理
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroManufactureWorkOrderFacadeServiceImpl implements IMicroManufactureWorkOrderFacadeService {
    @Autowired
    private MicroManufactureWorkOrderMapper microManufactureWorkOrderMapper;

    @Autowired
    private MicroManufactureTaskMapper microManufactureTaskMapper;

    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IMicroTechnologyService microTechnologyService;

    @Autowired
    private IMicroTechnologyService technologyService;

    @Autowired
    private IMicroProductService productService;

    @Autowired
    private IMicroProductBomService microProductBomService;

    @Autowired
    private MicroPickMaterialsMapper microPickMaterialsMapper;

    @Autowired
    private IMicroManufactureWorkOrderService manufactureWorkOrderService;

    @Autowired
    private IMicroWorkShopService microWorkShopService;

    @Autowired
    private IMicroManufactureLineService manufactureLineService;

    @Autowired
    private IMicroExtendFieldRelationService microExtendFieldRelationService;

    @Autowired
    private IMicroExtendFieldService microExtendFieldService;

    @Autowired
    private MicroManufactureOrderMapper microManufactureOrderMapper;

    @Autowired
    private MicroSupportUtil microSupportUtil;

    /**
     * 查询生产工单
     *
     * @param id 生产工单ID
     * @return 生产工单
     */
    @Override
    public MicroManufactureWorkOrderDto selectMicroManufactureWorkOrderById(Long id) {
        MicroManufactureWorkOrderDto microManufactureWorkOrderDto = new MicroManufactureWorkOrderDto();
        MicroManufactureWorkOrder microManufactureWorkOrder = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderById(id);
        BeanUtils.copyProperties(microManufactureWorkOrder, microManufactureWorkOrderDto);
        return microManufactureWorkOrderDto;
    }

    /**
     * 查询生产工单列表
     *
     * @param microManufactureWorkOrder 生产工单
     * @return 生产工单
     */
    @Override
    public List<MicroManufactureWorkOrderDto> selectMicroManufactureWorkOrderList(MicroManufactureWorkOrderDto microManufactureWorkOrder) {
        MicroManufactureWorkOrder condition = MicroManufactureWorkOrderAssembler.convert2Condition(microManufactureWorkOrder);
        List<MicroManufactureWorkOrder> workOrderList = manufactureWorkOrderService.selectMicroManufactureWorkOrderListByCondition(condition);
        return MicroManufactureWorkOrderAssembler.convertDomains2DTOS4Page(workOrderList);
    }

    /**
     * 获取工单详情
     *
     * @param workOrderNo 工单号
     * @return 工单详情
     */
    @Override
    public DetailManufactureWorkOrderInfo selectDetailManufactureWorkOrderInfo(String workOrderNo) {
        log.info("请求的参数为:{}", workOrderNo);
        // 存储结果
        DetailManufactureWorkOrderInfo result = new DetailManufactureWorkOrderInfo();

        MicroManufactureWorkOrder queryParamByWorkOrder = new MicroManufactureWorkOrder();
        queryParamByWorkOrder.setWorkOrderNo(workOrderNo);
        List<MicroManufactureWorkOrder> manufactureWorkOrderList = manufactureWorkOrderService.selectMicroManufactureWorkOrderListByCondition(queryParamByWorkOrder);
        if (!CollectionUtils.isEmpty(manufactureWorkOrderList)) {
            // 工单
            MicroManufactureWorkOrder microManufactureWorkOrder = manufactureWorkOrderList.get(0);
            result = MicroManufactureWorkOrderAssembler.covert2DetailManufactureWorkOrderInfo(microManufactureWorkOrder);
            // 查询相关的任务
            MicroManufactureTask queryParamByTask = new MicroManufactureTask();
            queryParamByTask.setWorkOrderNo(workOrderNo);
            List<MicroManufactureTask> microManufactureTaskList = microManufactureTaskMapper.selectMicroManufactureTaskList(queryParamByTask);
            // 存储任务进度实体
            List<MicroManufactureTaskProcessInfo> taskProcessInfoList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(microManufactureTaskList)) {
                // 查询相关的报工记录信息
                MicroWorkSubmit queryParamBySubmit = new MicroWorkSubmit();
                queryParamBySubmit.setWorkOrderNo(workOrderNo);
                List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectMicroWorkSubmitList(queryParamBySubmit);
                Map<String, List<MicroWorkSubmit>> map = microWorkSubmitList.stream().collect(Collectors.groupingBy(MicroWorkSubmit::getOperateProcessSeq));

                // 整合信息
                for (MicroManufactureTask microManufactureTask : microManufactureTaskList) {
                    List<MicroWorkSubmit> submitListTemp = map.get(microManufactureTask.getProcessSeq());
                    // 整合任务进度信息
                    MicroManufactureTaskProcessInfo temp = MicroManufactureTaskAssembler.covertMicroManufactureTaskProcessInfo(submitListTemp, microManufactureTask);
                    taskProcessInfoList.add(temp);
                }
                result.setMicroManufactureTaskProcessInfoList(taskProcessInfoList);
            }
            // 自定义字段信息
            if (StringUtils.isNotEmpty(microManufactureWorkOrder.getExtendContent())) {
                List<SimpleExtendFieldInfo> simpleExtendFieldInfos = microExtendFieldService.selectSimpleExtendFieldInfoList(microManufactureWorkOrder.getExtendContent());
                result.setExtendFieldInfoList(simpleExtendFieldInfos);
            }
        }
        return result;
    }

    /**
     * 新增生产工单
     * 1.新增工单，新增工单成功后执行2.3.4
     * 2.新增产品
     * 3.新增车间产线
     * 4.新增自定义字段
     *
     * @param microManufactureWorkOrder 生产工单
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult insertMicroManufactureWorkOrder(MicroManufactureWorkOrderDto microManufactureWorkOrder) {
        validManufactureWorkOrder4Insert(microManufactureWorkOrder);
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        MicroManufactureWorkOrder manufactureWorkOrder = MicroManufactureWorkOrderAssembler.convertDto2Domain4Insert(microManufactureWorkOrder, userId, tenantCode);
        try {
            //1.新增产品
            saveProduct(manufactureWorkOrder);
            //2.新增车间产线
            saveManufactureWorkShopAndLine(manufactureWorkOrder, userId, tenantCode);
            //3.新增自定义字段
            List<MicroExtendFieldRelation> extendFieldRelations = MicroExtendFieldRelationAssembler.convertDTOS2Domains4Insert(microManufactureWorkOrder.getExtendFieldRelationList(), userId, tenantCode);
            microExtendFieldRelationService.batchInsert(extendFieldRelations);
            //4.新增工单
            manufactureWorkOrderService.insertMicroManufactureWorkOrder(manufactureWorkOrder);
        } catch (Exception e) {
            log.error("新增工单出错，错误原因为{}", e);
            throw new CustomException(e.getMessage());
        }
        return AjaxResult.success();
    }

    /**
     * @return void
     * @author cosmo-hhim-open Team
     * @param workOrder 工单
     **/
    private void saveManufactureWorkShopAndLine(MicroManufactureWorkOrder workOrder, Long userId, String tenantCode) {
        //如果车间编码为空，则新增车间/产线
        if (StringUtils.isEmpty(workOrder.getWshopCode()) && StringUtils.isNotEmpty(workOrder.getWshopName())) {
            MicroWorkShop workShop = MicroWorkShopAssembler.convert2Domain(workOrder.getWshopName(), userId, tenantCode);
            if (microWorkShopService.insertMicroWorkshop(workShop) > 0) {
                MicroManufactureLine manufactureLine = MicroManufactureLineAssembler.convert2Domain(workShop.getWshopCode(), workOrder.getWshopName(), workOrder.getMlineName(), userId, tenantCode);
                manufactureLineService.insertMicroManufactureLine(manufactureLine);
                workOrder.setMlineCode(manufactureLine.getMlineCode());
            }
            workOrder.setWshopCode(workShop.getWshopCode());
        }
    }

    /**
     * @return void
     * @author cosmo-hhim-open Team
     * @param microManufactureWorkOrder 生产工单
     **/
    private void saveProduct(MicroManufactureWorkOrder microManufactureWorkOrder) {
        if (StringUtils.isEmpty(microManufactureWorkOrder.getProductSeq())) {
            MicroProduct product = new MicroProduct();
            product.setProductCode(microManufactureWorkOrder.getProductCode());
            product.setProductName(microManufactureWorkOrder.getProductName());
            product.setProductionMode(ProductionModeEnum.SELF_CONTROL.getCode());
            product.setProductType(ProductTypeEnum.BCP.getCode());
            productService.insertMicroProduct(product);
            microManufactureWorkOrder.setProductSeq(product.getProductSeq());
        }
    }

    /**
     * @return void
     * @author cosmo-hhim-open Team
     * @param microManufactureWorkOrder 生产工单
     **/
    private void validManufactureWorkOrder4Insert(MicroManufactureWorkOrderDto microManufactureWorkOrder) {
        validManufactureWorkOrder4Common(microManufactureWorkOrder);
        //手动输入工单号的,需判断工单号唯一性
        if (StringUtils.isNotEmpty(microManufactureWorkOrder.getWorkOrderNo())) {
            if (!manufactureWorkOrderService.isUniqueWorkOrderNo(microManufactureWorkOrder.getWorkOrderNo())) {
                throw new CustomException("工单编号已存在，请重新输入");
            }
        }
        //todo 判断计划开始时间、计划结束时间是否早于当前时间
    }

    /**
     * 修改生产工单
     *
     * @param microManufactureWorkOrder 生产工单
     * @return 结果
     */
    @Override
    public int updateMicroManufactureWorkOrder(MicroManufactureWorkOrderDto microManufactureWorkOrder) {
        //1.校验数据
        validManufactureWorkOrder4Common(microManufactureWorkOrder);
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        //2.新增自定义字段
        List<MicroExtendFieldRelation> extendFieldRelations = MicroExtendFieldRelationAssembler.convertDTOS2Domains4Insert(microManufactureWorkOrder.getExtendFieldRelationList(), userId, tenantCode);
        microExtendFieldRelationService.batchInsert(extendFieldRelations);
        //3.新增车间产线
        MicroManufactureWorkOrder manufactureWorkOrder = MicroManufactureWorkOrderAssembler.convertDto2Domain(microManufactureWorkOrder);
        saveManufactureWorkShopAndLine(manufactureWorkOrder, userId, tenantCode);
        return microManufactureWorkOrderMapper.updateMicroManufactureWorkOrder(manufactureWorkOrder);
    }

    /**
     * 批量删除生产工单
     *
     * @param ids 需要删除的生产工单ID
     * @return 结果
     */
    @Override
    public int deleteMicroManufactureWorkOrderByIds(Long[] ids) {
        return microManufactureWorkOrderMapper.deleteMicroManufactureWorkOrderByIds(ids);
    }

    /**
     * 删除生产工单信息
     *
     * @param id 生产工单ID
     * @return 结果
     */
    @Override
    public int deleteMicroManufactureWorkOrderById(Long id) {
        return microManufactureWorkOrderMapper.deleteMicroManufactureWorkOrderById(id);
    }

    /**
     * 获取生产工单开工下发页信息
     *
     * @param id 工单ID
     * @return 下发页信息
     */
    @Override
    public MicroManufactureWorkOrderDto getIssuedInfo(Long id) {
        MicroManufactureWorkOrderDto microManufactureWorkOrderDto = new MicroManufactureWorkOrderDto();
        MicroManufactureWorkOrder microManufactureWorkOrder = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderById(id);
        BeanUtils.copyProperties(microManufactureWorkOrder, microManufactureWorkOrderDto);
        List<String> productSeqs = new ArrayList<>();
        productSeqs.add(microManufactureWorkOrder.getProductSeq());
        List<MicroSingleTechChainEntity> list = microTechnologyService.selectSingleChainByProduct(null, productSeqs);
        if (CollectionUtils.isNotEmpty(list)) {
            MicroSaveSingleTechDTO microSaveSingleTechDTO = new MicroSaveSingleTechDTO();
            MicroSingleTechChainEntity microSingleTechChainEntity = list.get(0);
            BeanUtils.copyProperties(microSingleTechChainEntity, microSaveSingleTechDTO);
            List<MicroSaveSingleChainDTO> chainList = BeanUtil.copyToList(microSingleTechChainEntity.getProcessChainList(), MicroSaveSingleChainDTO.class);
            microSaveSingleTechDTO.setProcessChainList(chainList);
            microManufactureWorkOrderDto.setMicroSaveSingleTechDTO(microSaveSingleTechDTO);
        }
        return microManufactureWorkOrderDto;
    }

    /**
     * 生产工单开工下发
     *
     * @param microManufactureWorkOrderDto 工单数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void workOrderIssued(MicroManufactureWorkOrderDto microManufactureWorkOrderDto) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(microManufactureWorkOrderDto));
        Date date = new Date();
        // 判断状态，防止重复下发
        MicroManufactureWorkOrder workOrder = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderById(microManufactureWorkOrderDto.getId());
        if (!workOrder.getWorkOrderStatus().equals(WorkOrderStatusEnum.TO_BE_PRODCED.getCode())) {
            throw new CustomException("该工单状态不允许下发");
        }
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        //更新工单状态
        MicroManufactureWorkOrder microManufactureWorkOrder = new MicroManufactureWorkOrder();
        microManufactureWorkOrder.setId(microManufactureWorkOrderDto.getId());
        microManufactureWorkOrder.setWorkOrderStatus(WorkOrderStatusEnum.IN_PRODUCTION.getCode());
//        microManufactureWorkOrder.setProduceStartDate(date); 
        microManufactureWorkOrder.setLastUpdBy(String.valueOf(userId));
        microManufactureWorkOrder.setLastUpdDate(date);
        microManufactureWorkOrderMapper.updateMicroManufactureWorkOrder(microManufactureWorkOrder);
        //更新订单状态
        if (StringUtils.isNotEmpty(microManufactureWorkOrderDto.getOrderNo())) {
            MicroManufactureOrder microManufactureOrder = new MicroManufactureOrder();
            microManufactureOrder.setOrderNo(microManufactureWorkOrderDto.getOrderNo());
            microManufactureOrder.setOrderStatus(OrderStatusEnum.IN_PRODUCTION.getCode());
            microManufactureOrder.setLastUpdBy(String.valueOf(userId));
            microManufactureOrder.setLastUpdDate(date);
            microManufactureOrderMapper.updateOrderByNo(microManufactureOrder);
        }
        //生成生产任务
        List<MicroManufactureTaskDto> taskDtoList = microManufactureWorkOrderDto.getTaskDtoList();
        if (CollectionUtil.isNotEmpty(taskDtoList)) {
            List<MicroManufactureTask> taskList = new ArrayList<>();
            // 工序任务的顺序
            int sort = 0;
            for (int i = 0; i < taskDtoList.size(); i++) {
                MicroManufactureTask entity = new MicroManufactureTask();
                BeanUtils.copyProperties(taskDtoList.get(i), entity);
                if (i == 0) {
                    entity.setIsFirstProcess(IsFirstProcessEnum.YES.getCode());
                } else {
                    entity.setIsFirstProcess(IsFirstProcessEnum.NO.getCode());
                }
                entity.setTenantCode(tenantCode);
                entity.setTaskNo(redisCache.incrby("TO", 1));
                entity.setTechType(microManufactureWorkOrderDto.getTechType());
                entity.setSubmitStatus(TaskSubmitStatusEnum.TO_BE_REPORTED.getCode());
                entity.setCreatedBy(String.valueOf(userId));
                entity.setLastUpdBy(String.valueOf(userId));
                entity.setSort(sort);
                taskList.add(entity);
                sort++;
            }
            microManufactureTaskMapper.insertList(taskList);
        }

        //保存工艺 以及 工序链路
        if (BomAndTechTypeEnum.DRAFT.getCode().equals(microManufactureWorkOrderDto.getBeforeTechType())) {
            MicroSingleTechChainEntity microSingleTechChainEntity = new MicroSingleTechChainEntity();
            microSingleTechChainEntity.setProductSeq(microManufactureWorkOrderDto.getProductSeq());
            microSingleTechChainEntity.setTechType(microManufactureWorkOrderDto.getTechType());
            if (BomAndTechTypeEnum.STANDARD.getCode().equals(microManufactureWorkOrderDto.getTechType())) {
                microSingleTechChainEntity.setTechPattern(TechPatternEnum.SERIAL.getCode());
            } else {
                microSingleTechChainEntity.setTechPattern(TechPatternEnum.ISSUED_ORDER.getCode());
            }
            microSingleTechChainEntity.setProcessChainList(BeanUtil.copyToList(microManufactureWorkOrderDto.getTaskDtoList(), MicroSaveChainNodeEntity.class));
            MicroProduct product = new MicroProduct();
            product.setProductSeq(microSingleTechChainEntity.getProductSeq());
            product.setTenantCode(tenantCode);
            List<MicroProduct> productList = productService.selectMicroProductList(product);
            Assert.isTrue(!org.springframework.util.CollectionUtils.isEmpty(productList), "无法获取绑定的产品数据");
            Long productId = productList.get(0).getId();
            String productName = productList.get(0).getProductName();
            microSingleTechChainEntity.setProductId(productId);
            microSingleTechChainEntity.setProductName(productName);
            technologyService.saveSingleTechAndChain(microSingleTechChainEntity);
        }
    }

    /**
     * 新增投料单/退料单
     *
     * @param microManufactureWorkOrderDto 工单
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void feedingOrReturn(MicroManufactureWorkOrderDto microManufactureWorkOrderDto) {
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        //保存新增物料
        List<MicroProductEntity> microProductEntityList = microManufactureWorkOrderDto.getMicroPickMaterialsDtoList().stream().map(e -> {
            MicroProductEntity entity = new MicroProductEntity();
            entity.setProductCode(e.getProductCode());
            entity.setProductName(e.getProductName());
            entity.setProductionMode(ProductionModeEnum.SELF_CONTROL.getCode());
            entity.setProductType("YCL");
            entity.setProductSeq(e.getProductSeq());
            return entity;
        }).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(microProductEntityList)) {
            // 调用生成物料的方法
            productService.saveMicroProducts(microProductEntityList);
        }
        //新增投料单/退料单
        List<MicroPickMaterials> microPickMaterialsList = microManufactureWorkOrderDto.getMicroPickMaterialsDtoList().stream().filter(e -> e.getPickingNumber().compareTo(BigDecimal.ZERO) > 0).map(e -> {
            MicroPickMaterials entity = new MicroPickMaterials();
            BeanUtils.copyProperties(e, entity);
            if (StringUtils.isEmpty(e.getProductSeq())) {
                Optional<MicroProductEntity> microProductEntity = microProductEntityList.stream().filter(temp -> temp.getProductName().equals(e.getProductName())).findFirst();
                entity.setProductSeq(microProductEntity.get().getProductSeq());
                e.setProductType(microProductEntity.get().getProductType());
                e.setProductionMode(microProductEntity.get().getProductionMode());
                e.setProductSeq(microProductEntity.get().getProductSeq());
            }
            entity.setTenantCode(tenantCode);
            if (MaterialsTypeEnum.PICKING.getCode().equals(e.getMaterialsType())) {
                entity.setPickingNo(redisCache.incrby("LL", 1));
            } else {
                entity.setPickingNo(redisCache.incrby("TL", 1));
            }
            entity.setCreatedBy(String.valueOf(userId));
            return entity;
        }).collect(Collectors.toList());
        microPickMaterialsMapper.insertList(microPickMaterialsList);

        //生成BOM
        if (BomAndTechTypeEnum.DRAFT.getCode().equals(microManufactureWorkOrderDto.getBomType()) && MaterialsTypeEnum.PICKING.getCode().equals(microManufactureWorkOrderDto.getMaterialsType())) {
            // todo 投料新增的物料信息追加到bom中
            // 前端要判断一下物料是否大于0，在传递到后端
            if (microManufactureWorkOrderDto.getMicroPickMaterialsDtoList().size() > 0) {
                Map<String, List<MicroPickMaterialsDto>> map = microManufactureWorkOrderDto.getMicroPickMaterialsDtoList().stream().collect(Collectors.groupingBy(MicroPickMaterialsDto::getProductSeq));
                MicroProductBom microProductBom = new MicroProductBom();
                microProductBom.setProductSeq(microManufactureWorkOrderDto.getProductSeq());
                microProductBom.setBomType(BomAndTechTypeEnum.DRAFT.getCode());
                microProductBom.setProductNumber(BigDecimal.ONE);
                microProductBom.setProductType(microManufactureWorkOrderDto.getProductType());
                microProductBom.setProductionMode(microManufactureWorkOrderDto.getProductionMode());

                List<MicroProductBom> secondBomList = new ArrayList<>();
                Set<Map.Entry<String, List<MicroPickMaterialsDto>>> tempEntries = map.entrySet();
                Iterator<Map.Entry<String, List<MicroPickMaterialsDto>>> tempIterator = tempEntries.iterator();
                while (tempIterator.hasNext()) {
                    Map.Entry<String, List<MicroPickMaterialsDto>> tempEnetity = tempIterator.next();
                    BigDecimal tempNumber = tempEnetity.getValue().stream().map(MicroPickMaterialsDto::getPickingNumber).reduce(BigDecimal.ZERO, BigDecimal::add);
                    MicroProductBom bomEntity = new MicroProductBom();
                    bomEntity.setProductSeq(tempEnetity.getKey());
                    bomEntity.setBomType(BomAndTechTypeEnum.DRAFT.getCode());
                    bomEntity.setProductType(tempEnetity.getValue().get(0).getProductType());
                    bomEntity.setProductionMode(tempEnetity.getValue().get(0).getProductionMode());
                    bomEntity.setProductNumber(tempNumber.divide(microManufactureWorkOrderDto.getWorkOrderNum(), 4, RoundingMode.HALF_UP));
                    secondBomList.add(bomEntity);
                }
                microProductBom.setSecondBomList(secondBomList);
                microProductBomService.save(microProductBom);
            }
        }
    }


    /**
     * @return int
     * @author cosmo-hhim-open Team
     * @param microManufactureWorkOrder 生产工单
     **/
    @Override
    public int operate(MicroManufactureWorkOrderDto microManufactureWorkOrder) {
        validManufactureWorkOrder4Operate(microManufactureWorkOrder);
        MicroManufactureWorkOrder workOrder = MicroManufactureWorkOrderAssembler.convertDto2Domain4Operate(microManufactureWorkOrder, SecurityUtils.getUserId());

        // 查询工单对应报工记录列表 最后一次报工时间
        MicroWorkSubmit queryParamBySubmit = new MicroWorkSubmit();
        queryParamBySubmit.setWorkOrderNo(workOrder.getWorkOrderNo());
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectMicroWorkSubmitList(queryParamBySubmit);
        if (!CollectionUtils.isEmpty(microWorkSubmitList)) {
            microWorkSubmitList.stream().max(Comparator.comparing(MicroWorkSubmit::getCreatedDate))
                    .ifPresent(microWorkSubmit -> workOrder.setLastWorkSubmitDate(microWorkSubmit.getCreatedDate()));
        }
        return manufactureWorkOrderService.operate(workOrder, microManufactureWorkOrder.getOperateFlag());
    }

    /**
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.planning.MicroManufactureWorkOrderDto>
     * @author cosmo-hhim-open Team
     * @param microManufactureWorkOrder 生产工单
     **/
    @Override
    public List<MicroManufactureWorkOrderDto> selectWorkStatusCount(MicroManufactureWorkOrderDto microManufactureWorkOrder) {
        MicroManufactureWorkOrder condition = MicroManufactureWorkOrderAssembler.convert2Condition(microManufactureWorkOrder);
        List<MicroManufactureWorkOrder> workOrderList = manufactureWorkOrderService.selectWorkStatusCount(condition);
        return MicroManufactureWorkOrderAssembler.convertDomains2DTOS(workOrderList);
    }

    /**
     * @return void
     * @author cosmo-hhim-open Team
     * @param microManufactureWorkOrder 生产工单
     **/
    private void validManufactureWorkOrder4Operate(MicroManufactureWorkOrderDto microManufactureWorkOrder) {
        validManufactureWorkOrder4Common(microManufactureWorkOrder);
        if (Objects.isNull(microManufactureWorkOrder.getId())) {
            throw new CustomException("未选择工单进行操作");
        }
        if (StringUtils.isEmpty(microManufactureWorkOrder.getOperateFlag())) {
            throw new CustomException("未指明具体操作");
        }
        if (OrderOperateEnum.ORDER_OPERATE_CLOSE.getCode().equals(microManufactureWorkOrder.getOperateFlag())) {
            if (ActiveFlagStandardEnum.NORMAL.getCode().equals(microManufactureWorkOrder.getMainFlag())) {
                throw new CustomException("主工单不能被关单");
            }
        }
    }

    private void validManufactureWorkOrder4Common(MicroManufactureWorkOrderDto microManufactureWorkOrder) {
        if (Objects.isNull(microManufactureWorkOrder)) {
            throw new CustomException("参数不能为空");
        }
        //校验当前车间下新增的产线是否已存在     如果车间产线都是新增不需要考虑
        if (StringUtils.isNotEmpty(microManufactureWorkOrder.getWshopCode())
                && StringUtils.isEmpty(microManufactureWorkOrder.getMlineCode())
                && StringUtils.isNotEmpty(microManufactureWorkOrder.getMlineName())) {
            if (manufactureLineService.isUniqueLineNameByWorkShop(microManufactureWorkOrder.getWshopCode(), microManufactureWorkOrder.getMlineName(), null)) {
                throw new CustomException("当前车间下，产线【" + microManufactureWorkOrder.getMlineName() + "】已存在");
            }
        }
    }

    /**
     * 获取生产任务列表-app
     *
     * @param microManufactureWorkOrderDto
     * @return
     */
    @Override
    public List<MicroManufactureWorkOrderDto> getMicroManufactureTaskList(MicroManufactureWorkOrderDto microManufactureWorkOrderDto) {
        List<MicroManufactureWorkOrderDto> resultList = new ArrayList<>();
        MicroManufactureWorkOrder microManufactureWorkOrder = new MicroManufactureWorkOrder();
        BeanUtils.copyProperties(microManufactureWorkOrderDto, microManufactureWorkOrder);
        List<MicroManufactureWorkOrder> list = microManufactureWorkOrderMapper.getMicroManufactureTaskList(microManufactureWorkOrder);
        resultList = BeanUtil.copyToList(list, MicroManufactureWorkOrderDto.class);
        return MicroPageUtils.listToPage(list, resultList);
    }

    /**
     * 查询生产任务列表数量-app
     *
     * @param microManufactureWorkOrderDto 工单查询条件
     * @return 状态数量列表
     */
    @Override
    public List<MicroManufactureWorkOrderDto> getStatusCount(MicroManufactureWorkOrderDto microManufactureWorkOrderDto) {
        List<MicroManufactureWorkOrderDto> resultList = new ArrayList<>();
        MicroManufactureWorkOrder microManufactureWorkOrder = new MicroManufactureWorkOrder();
        BeanUtils.copyProperties(microManufactureWorkOrderDto, microManufactureWorkOrder);
        List<String> statusList = new ArrayList<>();
        statusList.add(WorkOrderStatusEnum.IN_PRODUCTION.getCode());
        statusList.add(WorkOrderStatusEnum.FINISHED.getCode());
        microManufactureWorkOrder.setStatusList(statusList);
        List<MicroManufactureWorkOrder> list = manufactureWorkOrderService.getStatusCount(microManufactureWorkOrder);
        resultList = BeanUtil.copyToList(list, MicroManufactureWorkOrderDto.class);
        return resultList;
    }

    /**
     * 获取生产报工页面
     */
    @Override
    public MicroManufactureWorkOrderDto getSubmitInfo(Long id) {
        MicroManufactureWorkOrderDto microManufactureWorkOrderDto = new MicroManufactureWorkOrderDto();
        MicroManufactureWorkOrder microManufactureWorkOrder = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderById(id);
        BeanUtils.copyProperties(microManufactureWorkOrder, microManufactureWorkOrderDto);
        MicroManufactureTask query = new MicroManufactureTask();
        query.setWorkOrderNo(microManufactureWorkOrder.getWorkOrderNo());
        List<MicroManufactureTask> microManufactureTaskList = microManufactureTaskMapper.selectMicroManufactureTaskList(query);
        List<MicroManufactureTaskDto> taskDtoList = BeanUtil.copyToList(microManufactureTaskList, MicroManufactureTaskDto.class);
        microManufactureWorkOrderDto.setTaskDtoList(taskDtoList);
        return microManufactureWorkOrderDto;
    }

    /**
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.planning.MicroManufactureWorkOrderDto>
     * @author cosmo-hhim-open Team
     * @param microManufactureWorkOrder 生产工单
     **/
    @Override
    public List<MicroManufactureWorkOrderDto> selectWorkOrderProgressList(MicroManufactureWorkOrderDto microManufactureWorkOrder) {
        List<MicroManufactureWorkOrder> workOrderList = manufactureWorkOrderService.selectWorkOrderProgressList(MicroManufactureWorkOrderAssembler.convert2Condition(microManufactureWorkOrder));
        return MicroManufactureWorkOrderAssembler.convertDomains2DTOS4Page(workOrderList);
    }

    /**
     * 获取到人的工单列表信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工单列表
     */
    @Override
    public List<SimpleWorkOrderInfo> selectSimpleWorkOrderInfoListByUserAndDay(Date startDate, Date endDate) {
        // 查询信息
        Long userId = SecurityUtils.getUserId();
        startDate = DateUtil.beginOfDay(startDate).toJdkDate();
        endDate = DateUtil.endOfDay(endDate).toJdkDate();
        Long submitType = microSupportUtil.getSubmitType();
        // 查询这个时间段、这个人的报工记录，从而获取工单号(要去重)
        List<String> workOrderNoList = microWorkSubmitMapper.selectAllWorkOrderFromSubmitRecordByUserAndDay(userId.toString(), startDate, endDate, submitType);
        if (CollectionUtil.isEmpty(workOrderNoList)) {
            return Collections.EMPTY_LIST;
        }
        // 根据工单号查询工单信息
        List<MicroManufactureWorkOrder> manufactureWorkOrderList = microManufactureWorkOrderMapper.selectSimpleWorkOrderInfoListByWorkOrderList(workOrderNoList);
        return MicroManufactureWorkOrderAssembler.workOrderDomain2SimpleWorkOrderInfo(workOrderNoList, manufactureWorkOrderList);
    }
}
