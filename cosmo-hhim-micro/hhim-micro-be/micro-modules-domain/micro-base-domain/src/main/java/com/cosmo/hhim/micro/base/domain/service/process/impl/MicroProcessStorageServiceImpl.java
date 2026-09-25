/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.process.impl;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroTags;
import com.cosmo.hhim.micro.base.domain.entity.storage.*;
import com.cosmo.hhim.micro.base.domain.entity.submit.FirstOrLastProcess;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroProductMapper;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroTagsMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessCommonMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.entity.MicroProductSelectEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.enums.*;
import com.cosmo.hhim.micro.infrastructure.events.FlushWarningMetricsEvent;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 库存Service业务层处理
 *
 * @date 2022-10-11
 */
@RefreshScope
@Slf4j
@Service
public class MicroProcessStorageServiceImpl implements IMicroProcessStorageService {
    @Autowired
    private MicroProcessStorageMapper microProcessStorageMapper;
    @Autowired
    private MicroProcessStorageHistoryMapper historyMapper;
    @Autowired
    private MicroProcessCommonMapper processCommonMapper;
    @Autowired
    private MicroProductMapper productMapper;
    @Autowired
    private MicroTagsMapper tagMapper;
    @Autowired
    private MicroSupportUtil supportUtil;
    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private MicroProcessStorageHistoryMapper microProcessStorageHistoryMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 查询库存
     *
     * @param id 库存ID
     * @return 库存
     */
    @Override
    public MicroProcessStorage selectMicroProcessStorageById(Long id) {
        return microProcessStorageMapper.selectMicroProcessStorageById(id);
    }

    /**
     * 查询库存列表
     *
     * @param microProcessStorageDto 库存
     * @return 库存
     */
    @Override
    public List<MicroProcessStorage> selectMicroProcessStorageList(MicroProcessStorageDto microProcessStorageDto) {
        return microProcessStorageMapper.selectMicroProcessStorageList(microProcessStorageDto);
    }

    /**
     * 修改库存(包含调整某个库存和自定义调整库存)
     * 先判断入参产品/工序是否已经存在,不存在要新增
     * 存在则先更新库存表数据
     * 更新成功后插入一条库存变动数据
     *
     * @param modifyList 库存
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroProcessStorage(List<MicroProcessStorageModify> modifyList) {
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        String redisKey = "micro:update_process_storage:" + tenantCode + CommonConstants.SEPARATOR_SYMBOL;
        if (redisCache.getCacheObject(redisKey) == null) {
            redisCache.setCacheObject(redisKey, 1L, 10, TimeUnit.SECONDS);
        } else {
            throw new CustomException("有其他用户在调整库存，请核实后继续操作！");
        }
        String userId = String.valueOf(SecurityUtils.getUserId());
        // 过滤出 是否有productSeq的产品,汇总出相同产品名的/相同产品编码的
        // 查询产品+工序是否有库存,有库存依次扣减,判断是否超出库存
        //是否存在产品或工序不存在的数据
        List<MicroProcessStorageModify> missList = modifyList.stream().filter(m -> (StringUtils.isEmpty(m.getProductSeq()) || StringUtils.isEmpty(m.getProcessSeq()))).collect(Collectors.toList());
        List<MicroProcessStorageModify> insertStorageList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(missList)) {
            modifyList.removeAll(missList);
            //新建产品或工序,然后插入库存,库存变动
            this.createNewAndAssembleStock(missList, insertStorageList, tenantCode, userId);
        }
        if (!CollectionUtils.isEmpty(modifyList)) {
            this.validAndAssembleStock(modifyList, insertStorageList);
        }
        for (MicroProcessStorageModify modify : insertStorageList) {
            MicroProcessStorage storage = new MicroProcessStorage();
            storage.setId(modify.getId());
            storage.setTenantCode(tenantCode);
            storage.setProductSeq(modify.getProductSeq());
            storage.setProcessSeq(modify.getProcessSeq());
            storage.setPassNum(modify.getPassToNum());
            storage.setNgNum(modify.getNgToNum());
            storage.setCreatedBy(userId);
            int i = microProcessStorageMapper.insertMicroProcessStorage(storage);
            if (i < 1) {
                log.error("库存变更失败:入参->{}", modify);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return 0;
            }
            MicroProcessStorageHistory history = new MicroProcessStorageHistory();
            BeanUtils.copyProperties(modify, history);
            history.setCreatedBy(userId);
            history.setTenantCode(tenantCode);
            history.setOperateNode(CommonConstants.STORAGE_MANUAL_MODIFY);
            if (modify.getPassFromNum() == null) {
                history.setPassFromNum(BigDecimal.valueOf(0));
            }
            if (modify.getNgFromNum() == null) {
                history.setNgFromNum(BigDecimal.valueOf(0));
            }
            i = historyMapper.insertMicroProcessStorageHistory(history);
            this.insertTag(history.getRemark(), tenantCode, userId);
            if (i < 1) {
                log.error("插入库存变动表失败:入参->{}", modify);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return 0;
            }
        }
        redisCache.deleteObject(redisKey);
        applicationEventPublisher.publishEvent(new FlushWarningMetricsEvent(tenantCode));
        return 1;
    }

    /**
     * 验证工序/产品并形成新库存
     *
     */
    private void validAndAssembleStock(List<MicroProcessStorageModify> modifyList, List<MicroProcessStorageModify> insertStorageList) {
        //相同产品名/相同产品编码的
        Function<MicroProcessStorageModify, MicroProcessStorageModify> function = item -> {
            MicroProcessStorageModify modify = new MicroProcessStorageModify();
            modify.setProcessSeq(item.getProcessSeq());
            modify.setProductSeq(item.getProductSeq());
            return modify;
        };
        Map<MicroProcessStorageModify, List<MicroProcessStorageModify>> modifyMap = modifyList.stream().collect(Collectors.groupingBy(function, Collectors.toList()));
        //汇总后的列表,进行产品和工序的库存查询,汇总后是否满足库存扣减,如不满足全部回滚
        Set<MicroProcessStorageModify> keys = modifyMap.keySet();
        List<MicroProcessStorage> groupStoragelist = microProcessStorageMapper.selectStorageByProductAndProcessList(keys);
        Map<String, String> verfiyMap = groupStoragelist.stream().collect(Collectors.toMap(key -> key.getProductSeq() + "&&" + key.getProcessSeq(), value -> (value.getPassNum() + "&&" + value.getNgNum())));
        String verifyKey;
        for (Map.Entry<MicroProcessStorageModify, List<MicroProcessStorageModify>> entry : modifyMap.entrySet()) {
            MicroProcessStorageModify key = entry.getKey();
            List<MicroProcessStorageModify> modifies = entry.getValue();
            verifyKey = key.getProductSeq() + "&&" + key.getProcessSeq();
            // 使用 BigDecimal 精确累加库存变更量，避免 double 运算引入浮点误差/Infinity（拒绝服务与精度加固）
            BigDecimal modifyPassNum = modifies.stream()
                    .map(m -> m.getPassToNum().subtract(m.getPassFromNum()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal modifyNgNum = modifies.stream()
                    .map(m -> m.getNgToNum().subtract(m.getNgFromNum()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            MicroProcessStorageModify oldEntity = modifies.get(0);
            MicroProcessStorageModify storage = new MicroProcessStorageModify();
            if (verfiyMap.containsKey(verifyKey)) {
                String numStr = verfiyMap.get(verifyKey);
                String[] numSplit = numStr.split("&&");
                BigDecimal oldPassFromNum = new BigDecimal(numSplit[0]);
                BigDecimal oldNgFromNum = new BigDecimal(numSplit[1]);
                BigDecimal actualPassToNum = oldPassFromNum.add(modifyPassNum);
                BigDecimal actualNgToNum = oldNgFromNum.add(modifyNgNum);
                if (actualPassToNum.signum() < 0 || actualNgToNum.signum() < 0) {
                    throw new CustomException("库存不足!");
                }
                //原始库存小于0&&操作减少库存
                boolean a = oldPassFromNum.signum() < 0 && actualPassToNum.compareTo(oldPassFromNum) < 0;
                boolean b = oldNgFromNum.signum() < 0 && actualNgToNum.compareTo(oldNgFromNum) < 0;
                if (a || b) {
                    throw new CustomException("库存不足!");
                }
                if (modifyPassNum.signum() < 0 || modifyNgNum.signum() < 0) {
                    if (modifyPassNum.add(modifyNgNum).abs().compareTo(oldPassFromNum.add(oldNgFromNum)) > 0) {
                        throw new CustomException("库存不足!");
                    }
                }
                storage.setPassFromNum(oldPassFromNum);
                storage.setPassToNum(actualPassToNum);
                storage.setNgFromNum(oldNgFromNum);
                storage.setNgToNum(actualNgToNum);
            } else {
                //判断是否减少,提出负库存
                if (modifyPassNum.signum() < 0 || modifyNgNum.signum() < 0) {
                    throw new CustomException("库存不足!");
                }
                storage.setPassFromNum(BigDecimal.ZERO);
                storage.setPassToNum(modifyPassNum);
                storage.setNgFromNum(BigDecimal.ZERO);
                storage.setNgToNum(modifyNgNum);
            }
            storage.setId(oldEntity.getId());
            storage.setRemark(oldEntity.getRemark());
            storage.setProductSeq(key.getProductSeq());
            storage.setProductCode(oldEntity.getProductCode());
            storage.setProductName(oldEntity.getProductName());
            storage.setProcessSeq(key.getProcessSeq());
            storage.setProcessCode(oldEntity.getProcessCode());
            storage.setProcessName(oldEntity.getProcessName());
            insertStorageList.add(storage);
        }
    }

    private void insertTag(String remark, String tenantCode, String userId) {
        if (!StringUtils.isEmpty(remark)) {
            try {
//                String[] tags = remark.split(";");
                //这里保留多标签模式 
                String[] tags = Collections.singletonList(remark).toArray(new String[0]);
                ;
                List<MicroTags> tagList = new ArrayList<>();
                MicroTags microTags;
                for (String tag : tags) {
                    microTags = new MicroTags();
                    microTags.setTenantCode(tenantCode);
                    microTags.setTagName(tag);
                    microTags.setTagType(TagTypeEnum.STORAGE_CHANGE.getCode());
                    microTags.setCreatedBy(userId);
                    tagList.add(microTags);
                }
                tagMapper.insertMicroTagBatch(tagList);
                supportUtil.addHotTag(tags);
            } catch (Exception e) {
                log.error("记录变动原因标签失败:入参->{}:{}:{}, Error:{}", remark, e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            }
        }
    }

    /**
     * 创建新工序/产品并形成新库存
     *
     */
    private void createNewAndAssembleStock(List<MicroProcessStorageModify> modifyList, List<MicroProcessStorageModify> insertStorageList, String tenantCode, String userId) { 
        Set<String> newProcessSet = new HashSet<>();
        Set<String> newProductSet = new HashSet<>();
        Map<String, String> oldProcessMap = new HashMap<>(16);
        Map<String, String> oldProductMap = new HashMap<>(16);
        Date currentDate = new Date();
        for (MicroProcessStorageModify modify : modifyList) {
            if (StringUtils.isEmpty(modify.getProcessSeq())) {
                newProcessSet.add(modify.getProcessName());
            } else {
                oldProcessMap.putIfAbsent(modify.getProcessName(), modify.getProcessSeq());
            }
            if (StringUtils.isEmpty(modify.getProductSeq())) {
                newProductSet.add(modify.getProductName());
            } else {
                oldProductMap.putIfAbsent(modify.getProductName(), modify.getProductSeq());
            }
        }
        Map<String, String> newProcessMap = new HashMap<>(8);
        Map<String, String> newProductMap = new HashMap<>(8);
        List<MicroProcessCommon> processList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(newProcessSet)) {
            List<String> processSeqAndCodeList = processCommonMapper.selectProcessSeqAndCode();
            LinkedList<String> processSeqQueue = new LinkedList<>(
                    supportUtil.getMultiProcessSeqNumbers(newProcessSet.size(), processSeqAndCodeList)
            );
            for (String processName : newProcessSet) {
                String processSeqNumber = processSeqQueue.poll();
                if (StringUtils.isEmpty(processSeqNumber)) {
                    processSeqNumber = supportUtil.getProcessSeqNumber(processSeqAndCodeList);
                }
                MicroProcessCommon process = new MicroProcessCommon();
                process.setTenantCode(tenantCode);
                process.setProcessSeq(processSeqNumber);
                process.setProcessCode(processSeqNumber);
                process.setProcessName(processName);
                process.setCreatedBy(userId);
                process.setCreatedDate(currentDate);
                process.setLastUpdDate(currentDate);
                processList.add(process);
                newProcessMap.put(processName, processSeqNumber);
            }
            processCommonMapper.insertMicroProcessCommonBatch(processList);
        }
        List<MicroProduct> productList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(newProductSet)) {
            List<String> allProductCodes = productMapper.selectProductSeqAndCode();
            LinkedList<String> productSeqQueue = new LinkedList<>(
                    supportUtil.getMultiProductSeqNumber(newProductSet.size(), allProductCodes)
            );
            for (String productName : newProductSet) {
                String productSeqNumber = productSeqQueue.poll();
                if (StringUtils.isEmpty(productSeqNumber)) {
                    productSeqNumber = supportUtil.getProductSeqNumber(allProductCodes);
                }
                MicroProduct product = new MicroProduct();
                product.setTenantCode(tenantCode);
                product.setProductCode(productSeqNumber);
                product.setProductSeq(productSeqNumber);
                product.setProductName(productName);
                product.setProductType(ProductTypeEnum.CP.getCode());
                product.setCreatedBy(userId);
                product.setCreatedDate(currentDate);
                product.setLastUpdDate(currentDate);
                productList.add(product);
                newProductMap.put(productName, productSeqNumber);
            }
            productMapper.insertMicroProductBatch(productList);
        }
        String processSeq;
        String productSeq;
        List<MicroProcessStorageModify> modifies;
        HashMap<String, List<MicroProcessStorageModify>> groupMap = modifyList.stream().collect(Collectors.groupingBy(key -> key.getProductName() + "&&" + key.getProcessName(), HashMap::new, Collectors.toList()));
        for (Map.Entry<String, List<MicroProcessStorageModify>> entry : groupMap.entrySet()) {
            String[] split = entry.getKey().split("&&");
            modifies = entry.getValue();
            String productName = split[0];
            String processName = split[1];
            // 使用 BigDecimal 精确累加库存变更量，避免 double 运算引入浮点误差/Infinity（拒绝服务与精度加固）
            BigDecimal passNum = modifies.stream()
                    .map(m -> m.getPassToNum().subtract(m.getPassFromNum()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal ngNum = modifies.stream()
                    .map(m -> m.getNgToNum().subtract(m.getNgFromNum()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (passNum.signum() < 0 || ngNum.signum() < 0) {
                throw new CustomException("不能扣减成负库存!");
            }
            //是否新增产品或工序,不是的话取旧的产品/工序序列号
            if (newProcessMap.containsKey(processName)) {
                processSeq = newProcessMap.get(processName);
            } else {
                processSeq = oldProcessMap.get(processName);
            }
            if (newProductMap.containsKey(productName)) {
                productSeq = newProductMap.get(productName);
            } else {
                productSeq = oldProductMap.get(productName);
            }
            MicroProcessStorageModify storage = new MicroProcessStorageModify();
            storage.setRemark(modifies.get(0).getRemark());
            storage.setProductSeq(productSeq);
            storage.setProductName(productName);
            storage.setProductCode(productSeq);
            storage.setProcessName(processName);
            storage.setProcessSeq(processSeq);
            storage.setProcessCode(processSeq);
            storage.setPassFromNum(BigDecimal.valueOf(0));
            storage.setPassToNum(passNum);
            storage.setNgFromNum(BigDecimal.valueOf(0));
            storage.setNgToNum(ngNum);
            insertStorageList.add(storage);
        }
    }

    /**
     * 批量删除库存
     *
     * @param ids 需要删除的库存ID
     * @return 结果
     */
    @Override
    public int deleteMicroProcessStorageByIds(Long[] ids) {
        return microProcessStorageMapper.deleteMicroProcessStorageByIds(ids);
    }

    /**
     * 删除库存信息
     *
     * @param id 库存ID
     * @return 结果
     */
    @Override
    public int deleteMicroProcessStorageById(Long id) {
        return microProcessStorageMapper.deleteMicroProcessStorageById(id);
    }

    @Override
    public MicroProcessStorage selectMicroProcessStorageByInfo(MicroProcessStorageDto microProcessStorageDto) {
        List<MicroProcessStorage> list = microProcessStorageMapper.selectMicroProcessStorageList(microProcessStorageDto);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public Map<String, List<? extends MicroSelectEntity>> selectProcessStorageCondition(String key) {
        List<MicroSelectEntity> processList = processCommonMapper.selectMicroProcessByName(key);
        List<MicroProductSelectEntity> productList = productMapper.selectMicroProductByName(key, false);
        LinkedHashMap<String, List<? extends MicroSelectEntity>> map = new LinkedHashMap<>();
        map.put("process", processList);
        map.put("product", productList);
        return map;
    }

    @Override
    public List<MicroProcessStorage> selectMicroProcessStorageListBySeq(String seqKey, String productSeq, String processSeq) {
        return microProcessStorageMapper.selectMicroProcessStorageListBySeq(seqKey, productSeq, processSeq);
    }

    /**
     * 在制品查询 - （产品维度）
     *
     */
    @Override
    public List<MicroProcessStorage> selectMicroProcessStorageListByProduct(String productNameOrCode, String productSeq) {
        return microProcessStorageMapper.selectMicroProcessStorageListByProduct(productNameOrCode, productSeq);
    }

    /**
     * 在制品查询: 产品 + 工序
     * 要返回工序的最后一道工序标示字段
     *
     */
    @Override
    public List<MicroProcessStorage> selectMicroProcessStorageListByCondition(MicroProcessStorageDto microProcessStorageDto) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(microProcessStorageDto));
        List<MicroProcessStorage> microProcessStorageList = microProcessStorageMapper.selectMicroProcessStorageList(microProcessStorageDto);
        if (CollectionUtils.isEmpty(microProcessStorageList)) {
            return microProcessStorageList;
        }
        // 设置尾序和首序的标示
        microProcessStorageList.forEach(obj -> {
            FirstOrLastProcess firstOrLastProcess = microWorkSubmitMapper.selectIsFirstAndLastProcess(obj.getProductSeq(), obj.getProcessSeq(), SubmitStatusEnum.APPROVED.getCode());
            if (firstOrLastProcess == null) {
                obj.setIsLastProcess(IsLastProcessEnum.NO.getCode());
                obj.setIsFirstProcess(IsFirstProcessEnum.NO.getCode());
            } else {
                obj.setIsLastProcess(firstOrLastProcess.getIsLastProcess());
                obj.setIsFirstProcess(firstOrLastProcess.getIsFirstProcess());
            }
        });

        return microProcessStorageList;
    }

    @Override
    public List<MicroProcessStorage> negativeStockList() {
        return microProcessStorageMapper.selectNegativeStockList();
    }

    @Override
    public List<MicroProcessStorage> ngList() {
        return microProcessStorageMapper.selectNgList();
    }

    @Override
    public Map<String, Object> storageHealth() {
        return microProcessStorageMapper.selectStorageHealth();
    }

    /**
     * 负库存分析
     *
     */
    @Override
    public NegativeStockAnalysisTip negativeStockAnalysis(String productSeq, String processSeq, Date startDate, Date endDate) {
        log.info("请求的参数为:productSeq:{},processSeq:{},startDate:{},endDate:{}", productSeq, processSeq, startDate, endDate);
        // 构造查询参数
        MicroProcessStorageHistory param = new MicroProcessStorageHistory();
        param.setProcessSeq(processSeq);
        param.setProductSeq(productSeq);
        param.setStartDate(startDate);
        param.setEndDate(endDate);
        // 查找产品+工序在指定时间内的报工历史记录
        List<MicroProcessStorageHistory> processStorageHistoryList = historyMapper.selectMicroProcessStorageHistoryList(param);
        if (CollectionUtils.isEmpty(processStorageHistoryList)) {
            throw new CustomException("报工历史记录为空");
        }

        NegativeStockAnalysisTip res = new NegativeStockAnalysisTip();
        // 出现负数则+1
        int num = 0;
        // 出现负数的索引位置
        int index = 0;
        // 存储出现负数的索引, 记录最早出现负数的情况
        List<Integer> negativeIndex = new ArrayList<>();

        for (int i = 0; i < processStorageHistoryList.size(); i++) {
            MicroProcessStorageHistory temp = processStorageHistoryList.get(i);
            index++;

            // 判断是否负数还连续
            int flag = num;
            if (temp.getPassToNum().add(temp.getNgToNum()).signum() < 0) {
                negativeIndex.add(index - 1);
                num++;
            }
            if (num == 0 || flag < num) {
                continue;
            } else {
                break;
            }
        }

        // 有负数存在
        if (negativeIndex.size() != 0) {
            int lastIndex = negativeIndex.get(negativeIndex.size() - 1);
            // 获取当前索引的信息
            MicroProcessStorageHistory processStorageHistoryTemp = processStorageHistoryList.get(lastIndex);
            LocalDate occurredDate = processStorageHistoryTemp.getCreatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            res.setOccurredDate(occurredDate);

            // 判断
            if (negativeIndex.size() == processStorageHistoryList.size()) {
                res.setMessage("无期初库存");
            }
            MicroProcessStorageHistory temp = processStorageHistoryList.get(processStorageHistoryList.size() - 1);
            // 有期初库存
            if (temp.getPassToNum().add(temp.getNgToNum()).signum() > 0
                    && negativeIndex.size() == processStorageHistoryList.size() - 1) {
                res.setMessage("这一道工序一直没有记工, 但是库存一直被后一道工序所消耗");
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (negativeIndex.size() < processStorageHistoryList.size() - 1) {
                if (CommonConstants.STORAGE_PROCESS_FLOW_MODIFY.equals(processStorageHistoryList.get(lastIndex).getOperateNode())) {
                    res.setMessage(formatter.format(processStorageHistoryTemp.getCreatedDate()) + " " + processStorageHistoryTemp.getCreatedBy()
                            + "的记工数大于本道工序的累计记工数，转下序时被扣减为负数");
                }
                if (CommonConstants.STORAGE_UNDO_FALLBACK.equals(processStorageHistoryList.get(lastIndex).getOperateNode())) {
                    res.setMessage(formatter.format(processStorageHistoryTemp.getCreatedDate()) + " " + processStorageHistoryTemp.getCreatedBy()
                            + "的记工记录被撤销，导致累计记工数量小于转下序数量");
                }
            }
        }
        return res;
    }

    /**
     * 变更库存 -> 库存变动历史记录
     *
     */
    @Override
    public void changeStorage(MicroWorkSubmit microWorkSubmit, String dealType) { 

        List<MicroProcessStorageHistory> microProcessStorageHistoryList = new ArrayList<>();
        // 如果前工序存在（产品 + 前工序），进行扣减
        if (!StringUtils.isEmpty(microWorkSubmit.getPreProcessSeq())) {
            // preProcessSeq可能是多个
            List<String> preProcessSeqList = Arrays.stream(microWorkSubmit.getPreProcessSeq().split(",")).collect(Collectors.toList());
            List<String> preProcessCodeList = Arrays.stream(microWorkSubmit.getPreProcessCode().split(",")).collect(Collectors.toList());
            List<String> preProcessNameList = Arrays.stream(microWorkSubmit.getPreProcessName().split(",")).collect(Collectors.toList());

            for (int i = 0; i < preProcessSeqList.size(); i++) {
                MicroProcessStorageDto preProcessParam = new MicroProcessStorageDto();
                preProcessParam.setProcessSeq(preProcessSeqList.get(i));
                preProcessParam.setProductSeq(microWorkSubmit.getProductSeq());
                MicroProcessStorage preProcessStorage = this.selectMicroProcessStorageByInfo(preProcessParam);

                // 生成前工序库存变动历史记录
                MicroProcessStorageHistory preProcessStorageHistory = new MicroProcessStorageHistory();
                preProcessStorageHistory.setProductSeq(microWorkSubmit.getProductSeq());
                preProcessStorageHistory.setProductCode(microWorkSubmit.getProductCode());
                preProcessStorageHistory.setProductName(microWorkSubmit.getProductName());
                preProcessStorageHistory.setProcessSeq(preProcessSeqList.get(i));
                preProcessStorageHistory.setProcessCode(preProcessCodeList.get(i));
                preProcessStorageHistory.setProcessName(preProcessNameList.get(i));
                // 判断库存变动的原因类型
                if (CommonConstants.STORAGE_CHANGE_TYPE_SUBMIT.equals(dealType)) {
                    preProcessStorageHistory.setOperateNode(CommonConstants.STORAGE_PROCESS_FLOW_MODIFY);
                    // 库存变动历史记录创建人设置的是报工人
                    preProcessStorageHistory.setCreatedBy(microWorkSubmit.getSubmitUser());
                    // 设置工序流转的详细变动原因
                    preProcessStorageHistory.setRemark("工序" + microWorkSubmit.getOperateProcessName() + "扣减");
                } else if (CommonConstants.STORAGE_CHANGE_TYPE_ELIMINATION.equals(dealType)) {
                    preProcessStorageHistory.setOperateNode(CommonConstants.STORAGE_UNDO_FALLBACK);
                    // 审核撤销的操作人是当前人
                    preProcessStorageHistory.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
                    // 设置审核撤销的详细变动原因
                    if (ExpiredRecordFlagEnum.YES.getCode().equals(microWorkSubmit.getExpiredRecordFlag())) {
                        preProcessStorageHistory.setRemark("撤销" +
                                DateUtils.parseDateToStr("MM-dd", microWorkSubmit.getSubmitDay()) +
                                microWorkSubmit.getSubmitNickName() + "的记工");
                    } else {
                        preProcessStorageHistory.setRemark("撤销" +
                                DateUtils.parseDateToStr("MM-dd HH:mm:ss", microWorkSubmit.getCreatedDate()) +
                                microWorkSubmit.getSubmitNickName() + "的记工");
                    }
                } else if (CommonConstants.STORAGE_CHANGE_TYPE_QC_ELIMINATION.equals(dealType)) {
                    preProcessStorageHistory.setOperateNode(CommonConstants.STORAGE_QC_UNDO_FALLBACK);
                    // 审核撤销的操作人是质检人
                    preProcessStorageHistory.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
                    // 设置审核撤销的详细变动原因
                    if (ExpiredRecordFlagEnum.YES.getCode().equals(microWorkSubmit.getExpiredRecordFlag())) {
                        preProcessStorageHistory.setRemark("撤销" +
                                DateUtils.parseDateToStr("MM-dd", microWorkSubmit.getSubmitDay()) +
                                microWorkSubmit.getSubmitNickName() + "的记工");
                    } else {
                        preProcessStorageHistory.setRemark("撤销" +
                                DateUtils.parseDateToStr("MM-dd HH:mm:ss", microWorkSubmit.getCreatedDate()) +
                                microWorkSubmit.getSubmitNickName() + "的记工");
                    }
                }
                preProcessStorageHistory.setCreatedDate(DateUtils.getNowDate());
                preProcessStorageHistory.setTenantCode(microWorkSubmit.getTenantCode());

                // 如果 前工序 + 产品 无对应的库存记录，则新增
                // 撤销审核的数据正常不会出现库存不存在的情况  
                if (CheckObjectUtils.isEmpty(preProcessStorage)) {
                    MicroProcessStorage preProcessStorageInsert = new MicroProcessStorage();
                    preProcessStorageInsert.setProductSeq(microWorkSubmit.getProductSeq());
                    preProcessStorageInsert.setProcessSeq(preProcessSeqList.get(i));
                    // 口径：工序间流转的只有良品（不良品通过返修/报废离开本工序的 ng_num，不参与流转），
                    // 因此前工序的"欠账"只记良品 —— 原来记的是 -(良品+不良)，每流转一次就多扣一个不良，导致在制系统性偏负
                    preProcessStorageInsert.setNgNum(BigDecimal.ZERO);
                    preProcessStorageInsert.setPassNum(microWorkSubmit.getCheckPassNum().negate());
                    preProcessStorageInsert.setCreatedDate(DateUtils.getNowDate());
                    preProcessStorageInsert.setTenantCode(microWorkSubmit.getTenantCode());
                    preProcessStorageInsert.setCreatedBy(microWorkSubmit.getSubmitUser());
                    // 直接插入
                    microProcessStorageMapper.insertMicroProcessStorage(preProcessStorageInsert);

                    preProcessStorageHistory.setPassFromNum(BigDecimal.ZERO);
                    preProcessStorageHistory.setPassToNum(preProcessStorageInsert.getPassNum());
                    preProcessStorageHistory.setNgFromNum(BigDecimal.ZERO);
                    preProcessStorageHistory.setNgToNum(BigDecimal.ZERO);
                } else {
                    MicroProcessStorage preProcessStorageUpdate = new MicroProcessStorage();
                    preProcessStorageUpdate.setId(preProcessStorage.getId());
                    // 加减相关库存并更新（口径同注释：只对良品加减，不含不良）
                    if (CommonConstants.STORAGE_CHANGE_TYPE_SUBMIT.equals(dealType)) {
                        // 只扣减良品的数量
                        preProcessStorageUpdate.setPassNum(preProcessStorage.getPassNum().subtract(microWorkSubmit.getCheckPassNum()));
                    } else {
                        // 只加良品的数量
                        preProcessStorageUpdate.setPassNum(preProcessStorage.getPassNum().add(microWorkSubmit.getCheckPassNum()));
                    }
                    preProcessStorageUpdate.setLastUpdDate(DateUtils.getNowDate());
                    preProcessStorageUpdate.setLastUpdBy(microWorkSubmit.getSubmitUser());
                    // 直接更新
                    microProcessStorageMapper.updateMicroProcessStorage(preProcessStorageUpdate);

                    preProcessStorageHistory.setPassFromNum(preProcessStorage.getPassNum());
                    preProcessStorageHistory.setPassToNum(preProcessStorageUpdate.getPassNum());
                    // 不良品前后无变化
                    preProcessStorageHistory.setNgFromNum(preProcessStorage.getNgNum());
                    preProcessStorageHistory.setNgToNum(preProcessStorage.getNgNum());
                }
                microProcessStorageHistoryList.add(preProcessStorageHistory);
            }
        }

        // 产品 + 当前工序，进行增加
        MicroProcessStorageDto currentProcessParam = new MicroProcessStorageDto();
        currentProcessParam.setProcessSeq(microWorkSubmit.getOperateProcessSeq());
        currentProcessParam.setProductSeq(microWorkSubmit.getProductSeq());
        MicroProcessStorage currentProcessStorage = this.selectMicroProcessStorageByInfo(currentProcessParam);

        // 生成当前工序库存变动历史记录
        MicroProcessStorageHistory currentProcessStorageHistory = new MicroProcessStorageHistory();
        currentProcessStorageHistory.setProductSeq(microWorkSubmit.getProductSeq());
        currentProcessStorageHistory.setProductCode(microWorkSubmit.getProductCode());
        currentProcessStorageHistory.setProductName(microWorkSubmit.getProductName());
        currentProcessStorageHistory.setProcessSeq(microWorkSubmit.getOperateProcessSeq());
        currentProcessStorageHistory.setProcessCode(microWorkSubmit.getOperateProcessCode());
        currentProcessStorageHistory.setProcessName(microWorkSubmit.getOperateProcessName());
        // 判断库存变动的原因类型
        if (CommonConstants.STORAGE_CHANGE_TYPE_SUBMIT.equals(dealType)) {
            currentProcessStorageHistory.setOperateNode(CommonConstants.STORAGE_SUBMIT_INBOUND);
            // 库存变动历史记录创建人设置的是报工人
            currentProcessStorageHistory.setCreatedBy(microWorkSubmit.getSubmitUser());
        } else if (CommonConstants.STORAGE_CHANGE_TYPE_REPAIR.equals(dealType)) {
            // 返修复核类型
            currentProcessStorageHistory.setOperateNode(CommonConstants.STORAGE_REPAIR_QUALITY_CONTROL);
            // 创建人是返修人
            currentProcessStorageHistory.setCreatedBy(microWorkSubmit.getSubmitUser());
        } else if (CommonConstants.STORAGE_CHANGE_TYPE_ELIMINATION.equals(dealType)) {
            currentProcessStorageHistory.setOperateNode(CommonConstants.STORAGE_UNDO_FALLBACK);
            // 审核撤销的操作是当前人
            currentProcessStorageHistory.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
            // 设置审核撤销的详细变动原因
            if (ExpiredRecordFlagEnum.YES.getCode().equals(microWorkSubmit.getExpiredRecordFlag())) {
                currentProcessStorageHistory.setRemark("撤销" +
                        DateUtils.parseDateToStr("MM-dd", microWorkSubmit.getSubmitDay()) +
                        microWorkSubmit.getSubmitNickName() + "的记工");
            } else {
                currentProcessStorageHistory.setRemark("撤销" +
                        DateUtils.parseDateToStr("MM-dd HH:mm:ss", microWorkSubmit.getCreatedDate()) +
                        microWorkSubmit.getSubmitNickName() + "的记工");
            }
        } else if (CommonConstants.STORAGE_CHANGE_TYPE_QC_ELIMINATION.equals(dealType)) {
            currentProcessStorageHistory.setOperateNode(CommonConstants.STORAGE_QC_UNDO_FALLBACK);
            // 审核撤销的操作是当前人
            currentProcessStorageHistory.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
            // 设置审核撤销的详细变动原因
            if (ExpiredRecordFlagEnum.YES.getCode().equals(microWorkSubmit.getExpiredRecordFlag())) {
                currentProcessStorageHistory.setRemark("撤销" +
                        DateUtils.parseDateToStr("MM-dd", microWorkSubmit.getSubmitDay()) +
                        microWorkSubmit.getSubmitNickName() + "的记工");
            } else {
                currentProcessStorageHistory.setRemark("撤销" +
                        DateUtils.parseDateToStr("MM-dd HH:mm:ss", microWorkSubmit.getCreatedDate()) +
                        microWorkSubmit.getSubmitNickName() + "的记工");
            }
        }
        currentProcessStorageHistory.setCreatedDate(DateUtils.getNowDate());
        currentProcessStorageHistory.setTenantCode(microWorkSubmit.getTenantCode());

        // 撤销审核的数据正常不会出现库存不存在的情况  
        if (CheckObjectUtils.isEmpty(currentProcessStorage)) {
            MicroProcessStorage currentProcessStorageInsert = new MicroProcessStorage();
            currentProcessStorageInsert.setProductSeq(microWorkSubmit.getProductSeq());
            currentProcessStorageInsert.setProcessSeq(microWorkSubmit.getOperateProcessSeq());
            currentProcessStorageInsert.setNgNum(microWorkSubmit.getCheckNgNum());
            currentProcessStorageInsert.setPassNum(microWorkSubmit.getCheckPassNum());
            currentProcessStorageInsert.setCreatedBy(microWorkSubmit.getSubmitUser());
            currentProcessStorageInsert.setCreatedDate(DateUtils.getNowDate());
            currentProcessStorageInsert.setTenantCode(microWorkSubmit.getTenantCode());
            microProcessStorageMapper.insertMicroProcessStorage(currentProcessStorageInsert);

            currentProcessStorageHistory.setPassFromNum(BigDecimal.ZERO);
            currentProcessStorageHistory.setPassToNum(currentProcessStorageInsert.getPassNum());
            currentProcessStorageHistory.setNgFromNum(BigDecimal.ZERO);
            currentProcessStorageHistory.setNgToNum(currentProcessStorageInsert.getNgNum());
        } else {
            // 增加相关库存并更新
            MicroProcessStorage currentProcessStorageUpdate = new MicroProcessStorage();
            currentProcessStorageUpdate.setId(currentProcessStorage.getId());
            // 加减库存数量:
            // (返修的数量只会跟当前工序有关，跟前工序的数量无关)
            if (CommonConstants.STORAGE_CHANGE_TYPE_SUBMIT.equals(dealType) || CommonConstants.STORAGE_CHANGE_TYPE_REPAIR.equals(dealType)) {
                currentProcessStorageUpdate.setPassNum(currentProcessStorage.getPassNum().add(microWorkSubmit.getCheckPassNum()));
                currentProcessStorageUpdate.setNgNum(currentProcessStorage.getNgNum().add(microWorkSubmit.getCheckNgNum()));
                // 更新人是报工人或者返修人
                currentProcessStorageUpdate.setLastUpdBy(microWorkSubmit.getSubmitUser());
            } else {
                // 撤销的时候要考虑 -> **返修完成数量**
                currentProcessStorageUpdate.setPassNum(currentProcessStorage.getPassNum().subtract(microWorkSubmit.getCheckPassNum().add(microWorkSubmit.getRepairNum())));
                currentProcessStorageUpdate.setNgNum(currentProcessStorage.getNgNum().subtract(microWorkSubmit.getCheckNgNum().subtract(microWorkSubmit.getRepairNum())));
                // 更新人是当前操作人（审产员或者是质检人）
                currentProcessStorageUpdate.setLastUpdBy(String.valueOf(SecurityUtils.getUserId()));
            }
            currentProcessStorageUpdate.setLastUpdDate(DateUtils.getNowDate());
            microProcessStorageMapper.updateMicroProcessStorage(currentProcessStorageUpdate);

            currentProcessStorageHistory.setPassFromNum(currentProcessStorage.getPassNum());
            currentProcessStorageHistory.setPassToNum(currentProcessStorageUpdate.getPassNum());
            currentProcessStorageHistory.setNgFromNum(currentProcessStorage.getNgNum());
            currentProcessStorageHistory.setNgToNum(currentProcessStorageUpdate.getNgNum());
        }
        microProcessStorageHistoryList.add(currentProcessStorageHistory);
        // 插入库存变动历史记录
        microProcessStorageHistoryMapper.insertMicroProcessStorageHistoryBatch(microProcessStorageHistoryList);
    }

    /**
     * 查询库存导出模版所需信息列表
     *
     */
    @Override
    public List<MicroProcessStorageExportModelResult> selectAllMicroProcessStorageList() {
        return microProcessStorageMapper.selectAllMicroProcessStorageList();
    }

    /**
     * 根据产品id集合删除空库存
     *
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int removeZeroProcessStorageByProduct(List<Long> productIds) {
        Long[] ids = productIds.toArray(new Long[0]);
        List<Long> reList = microProcessStorageMapper.selectReStorageByProductIds(ids);
        if (!CollectionUtils.isEmpty(reList)) {
            microProcessStorageMapper.deleteMicroProcessStorageByIds(reList.toArray(new Long[0]));
        }
        reList = microProcessStorageMapper.selectReStorageHistByProductIds(ids);
        if (!CollectionUtils.isEmpty(reList)) {
            historyMapper.deleteMicroProcessStorageHistoryByIds(reList.toArray(new Long[0]));
        }
        return 1;
    }

    /**
     * 根据工序ID集合删除空库存
     *
     */
    @Override
    public int removeZeroProcessStorageByProcess(List<Long> processIds) {
        Long[] ids = processIds.toArray(new Long[0]);
        List<Long> reList = microProcessStorageMapper.selectReStorageByProcessIds(ids);
        if (!CollectionUtils.isEmpty(reList)) {
            microProcessStorageMapper.deleteMicroProcessStorageByIds(reList.toArray(new Long[0]));
        }
        reList = microProcessStorageMapper.selectReStorageHistByProcessIds(ids);
        if (!CollectionUtils.isEmpty(reList)) {
            historyMapper.deleteMicroProcessStorageHistoryByIds(reList.toArray(new Long[0]));
        }
        return 1;
    }

}
