/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.process.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessSelectEntity;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainBindEntity;
import com.cosmo.hhim.micro.base.domain.entity.tech.SimpleProcessResult;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroProductMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessCommonMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.mapper.tech.MicroTechnologyMapper;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessCommonService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.enums.CreatedTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.HotKeyEnum;
import com.cosmo.hhim.micro.infrastructure.events.CleanProcessDataEvent;
import com.cosmo.hhim.micro.infrastructure.util.MicroRedisUtils;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import com.cosmo.hhim.micro.infrastructure.util.TextCalculateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

/**
 * processService业务层处理
 *
 * @date 2022-10-11
 */
@Slf4j
@Service
public class MicroProcessCommonServiceImpl implements IMicroProcessCommonService {

    private static final int SIMILARITY_THRESHOLD = 80;
    private static final String ROOT_PARENT_PROCESS = "0";
    @Autowired
    private MicroProcessCommonMapper microProcessCommonMapper;
    @Autowired
    private MicroProductMapper microProductMapper;
    @Autowired
    private MicroWorkSubmitMapper submitMapper;
    @Autowired
    private MicroProcessStorageMapper storageMapper;
    @Autowired
    private MicroProcessStorageHistoryMapper historyMapper;
    @Autowired
    private MicroTechnologyMapper technologyMapper;
    @Autowired
    private MicroSupportUtil supportUtil;
    @Autowired
    private MicroRedisUtils microRedisUtils;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 查询process
     *
     * @param id id
     * @return process
     */
    @Override
    public MicroProcessCommon selectMicroProcessCommonById(Long id) {
        return microProcessCommonMapper.selectMicroProcessCommonById(id);
    }

    /**
     * 根据工序名称查询工序信息
     *
     * @param processName
     * @return
     */
    @Override
    public MicroProcessCommon selectMicroProcessCommonByName(String processName) {
        return microProcessCommonMapper.selectMicroProcessCommonByProcessName(processName);
    }

    /**
     * 根据工序编码查询工序信息
     *
     * @param processCode
     * @return
     */
    @Override
    public MicroProcessCommon selectMicroProcessCommonByCode(String processCode) {
        return microProcessCommonMapper.selectMicroProcessCommonByProcessCode(processCode);
    }

    /**
     * 查询process列表
     *
     * @param microProcessCommon microProcessCommon
     * @return process
     */
    @Override
    public List<MicroProcessCommon> selectMicroProcessCommonList(MicroProcessCommon microProcessCommon) {
        List<MicroProcessCommon> list = microProcessCommonMapper.selectMicroProcessCommonList(microProcessCommon);
        return TextCalculateUtil.sortBySimilarRadio(microProcessCommon.getKey(), list, p -> p.getProcessName().length());
    }

    /**
     * 查询所有工序列表
     *
     * @return
     */
    @Override
    public List<MicroProcessCommon> selectMicroAllProcessCommonList() {
        return microProcessCommonMapper.selectMicroProcessCommonList(null);
    }

    /**
     * 新增process
     *
     * @param microProcessCommon microProcessCommon
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MicroProcessCommon insertMicroProcessCommon(MicroProcessCommon microProcessCommon) {
        String seqNumber = supportUtil.getProcessSeqNumber(microProcessCommonMapper.selectProcessSeqAndCode());
        if (StringUtils.isEmpty(microProcessCommon.getProcessCode())) {
            microProcessCommon.setProcessCode(seqNumber);
        }
        MicroProcessCommon existProcess = existProcess(microProcessCommon);
        if (existProcess == null) {
            throw new CustomException("工序[" + microProcessCommon.getProcessName() + "]" + microProcessCommon.getProcessCode() + "已存在");
        } else {
            microProcessCommon.setProcessSeq(seqNumber);
            microProcessCommon.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
            microProcessCommon.setLastUpdDate(new Date());
            if (StringUtils.isEmpty(microProcessCommon.getCreatedType())) {
                microProcessCommon.setCreatedType(CreatedTypeEnum.AUTO.getCode());
            }
            microProcessCommonMapper.insertMicroProcessCommon(microProcessCommon);
            try {
                MicroSelectEntity hotData = new MicroSelectEntity();
                hotData.setItemSeq(microProcessCommon.getProcessSeq());
                hotData.setItemCode(microProcessCommon.getProcessCode());
                hotData.setItemName(microProcessCommon.getProcessName());
                microRedisUtils.incrByHotData(HotKeyEnum.PRE_PROCESS, CommonConstants.HOT_KEY_SUFFIX_TIMES, hotData);
                microRedisUtils.incrByHotData(HotKeyEnum.OPERATE_PROCESS, CommonConstants.HOT_KEY_SUFFIX_TIMES, hotData);
            } catch (Exception e) {
                log.error("插入工序热点数据失败:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            }
            return microProcessCommon;
        }
    }

    /**
     * 修改process
     *
     * @param microProcessCommon microProcessCommon
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroProcessCommon(MicroProcessCommon microProcessCommon) {
        if (StringUtils.isEmpty(microProcessCommon.getProcessCode())) {
            String seqNumber = supportUtil.getProcessSeqNumber(microProcessCommonMapper.selectProcessSeqAndCode());
            microProcessCommon.setProcessCode(seqNumber);
        }
        MicroProcessCommon existProcess = existProcess(microProcessCommon);
        if (existProcess == null) {
            throw new CustomException("工序[" + microProcessCommon.getProcessName() + "]" + microProcessCommon.getProcessCode() + "已存在");
        } else {
            microProcessCommon.setLastUpdBy(String.valueOf(SecurityUtils.getUserId()));
            microProcessCommon.setProcessSeq(existProcess.getProcessSeq());
            microProcessCommon.setLastUpdDate(new Date());
            int i = microProcessCommonMapper.updateMicroProcessCommon(microProcessCommon);
            updateProcessHotData(microProcessCommon);
            return i;
        }
    }

    /**
     * 更新工序热点数据(目前使用预研团队方案,只存储,暂时无用 2023年05月05日10:18:12)
     *
     * @param processCommon 工序数据
     */
    private void updateProcessHotData(MicroProcessCommon processCommon) {
        try {
            MicroSelectEntity entity = new MicroSelectEntity();
            entity.setItemSeq(processCommon.getProcessSeq());
            entity.setItemCode(processCommon.getProcessCode());
            entity.setItemName(processCommon.getProcessName());
            supportUtil.updateProcessHotData(entity);
        } catch (TaskRejectedException e) {
            log.error("update_process_hot_data was rejected!--->:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
    }

    private MicroProcessCommon existProcess(MicroProcessCommon microProcessCommon) {
        MicroProcessCommon verifyProcess = new MicroProcessCommon();
        if (StringUtils.isEmpty(microProcessCommon.getProcessCode())) {
            throw new CustomException("无法查询到该工序(" + microProcessCommon.getProcessCode() + "),请刷新后重试");
        }
        verifyProcess.setProcessCode(microProcessCommon.getProcessCode());
        verifyProcess.setId(microProcessCommon.getId());
        verifyProcess.setProcessName(microProcessCommon.getProcessName());
        List<MicroProcessCommon> verifyList = microProcessCommonMapper.selectExistMicroProcessCommonList(verifyProcess);
        if (microProcessCommon.getId() != null) {
            List<MicroProcessCommon> selfList = verifyList.stream().filter(v -> v.getId().equals(microProcessCommon.getId())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(selfList)) {
                if (verifyList.size() == 1) {
                    return selfList.get(0);
                } else {
                    return null;
                }
            }
            throw new CustomException("该工序[" + microProcessCommon.getProcessName() + "]" + microProcessCommon.getProcessCode() + "已不存在,请刷新后重试");
        } else if (!CollectionUtils.isEmpty(verifyList)) {
            return null;
        } else {
            return microProcessCommon;
        }
    }

    /**
     * 批量删除process
     *
     * @param ids 需要删除的processID
     * @return 结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int deleteMicroProcessCommonByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return 1;
        }
        List<MicroProcessSelectEntity> selectList = microProcessCommonMapper.selectMicroProcessStockByIds(Arrays.asList(ids), null, null);
        if (CollectionUtils.isEmpty(selectList)) {
            return 1;
        }
        microProcessCommonMapper.deleteMicroProcessCommonByIds(ids);
        applicationEventPublisher.publishEvent(new CleanProcessDataEvent(selectList));
        return 1;
    }

    /**
     * 删除process信息
     *
     * @param id processID
     * @return 结果
     */
    @Override
    public int deleteMicroProcessCommonById(Long id) {
        return microProcessCommonMapper.deleteMicroProcessCommonById(id);
    }

    /**
     * 根据模糊查询key值/产品序列码/是否圈定范围来查询工序下拉列表
     * 优先级productSeq > key
     *
     * @param key
     * @param productSeq
     * @param range      代表是否要在工艺链范围内获取工序数据
     * @return
     */
    @Override
    public List<MicroProcessSelectEntity> selectMicroProcessByName(String key, String productSeq, boolean range) {
        if (StringUtils.isEmpty(productSeq)) {
            List<MicroSelectEntity> list = microProcessCommonMapper.selectMicroProcessByName(key);
            if (!CollectionUtils.isEmpty(list)) {
                List<MicroProcessSelectEntity> result = new ArrayList<>();
                list.forEach(s -> {
                    MicroProcessSelectEntity processSelect = new MicroProcessSelectEntity();
                    BeanUtils.copyProperties(s, processSelect);
                    result.add(processSelect);
                });
                return selectEntitySort(key, result);
            }
            return Collections.emptyList();
        }
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        if (range) {
            List<Long> processIds = technologyMapper.selectProcessByProduct(key, productSeq);
            if (!CollectionUtils.isEmpty(processIds)) {
                return selectEntitySort(key, microProcessCommonMapper.selectMicroProcessStockByIds(processIds, productSeq, tenantCode));
            }
            return Collections.emptyList();
        }
        return selectEntitySort(key, microProcessCommonMapper.selectMicroProcessStockByName(key, productSeq, tenantCode));
    }

    /**
     * 工序通用的模糊匹配排序
     *
     * @param key
     * @param result
     * @param <T>
     * @return
     */
    public <T extends MicroSelectEntity> List<T> selectEntitySort(String key, List<T> result) {
        return TextCalculateUtil.sortBySimilarRadio(key, result, p -> (p.getItemName().length() + p.getItemCode().length()));
    }


    @Override
    public MicroProcessCommon createNewProcess(String processName) {
        MicroProcessCommon microProcessCommon = new MicroProcessCommon();
        String tenantCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        // 获取产品编码
        String productSeqNumber = supportUtil.getProcessSeqNumber(microProcessCommonMapper.selectProcessSeqAndCode());

        microProcessCommon.setProcessName(processName);
        microProcessCommon.setProcessCode(productSeqNumber);
        microProcessCommon.setProcessSeq(productSeqNumber);
        microProcessCommon.setTenantCode(tenantCode);
        microProcessCommon.setCreateInfo();
        microProcessCommon.setLastUpdDate(DateUtils.getNowDate());
        // 插入新的工序
        microProcessCommonMapper.insertMicroProcessCommon(microProcessCommon);

        return microProcessCommon;
    }

    /**
     * 计算工序名称的相似度
     *
     * @param processName
     * @return
     */
    @Override
    public MicroProcessCommon isOrNotHaveProcess(String processName) {
        Map<MicroProcessCommon, Float> res = new HashMap<>(16);
        // 获取当前所有工序
        List<MicroProcessCommon> microProcessCommons = microProcessCommonMapper.selectMicroProcessCommonList(new MicroProcessCommon());
        if (CollectionUtils.isEmpty(microProcessCommons)) {
            return new MicroProcessCommon();
        }
        microProcessCommons.forEach(obj -> {
            float similarityByLevenshteinDistance = TextCalculateUtil.getSimilarityByLevenshteinDistance(obj.getProcessName(), processName);
            // 设置阈值为80
            if (similarityByLevenshteinDistance > SIMILARITY_THRESHOLD) {
                res.put(obj, similarityByLevenshteinDistance);
            }
        });
        if (CollectionUtils.isEmpty(res)) {
            return new MicroProcessCommon();
        }
        // 按照相似度值排序
        // 得到Map中的Entry节点
        Set<Map.Entry<MicroProcessCommon, Float>> set = res.entrySet();
        // 将Set转换为List类型
        List<Map.Entry<MicroProcessCommon, Float>> list = new ArrayList<>(set);
        // 对list中的数据根据Value值进行排序
        Collections.sort(list, (o1, o2) -> {
            //降序排序
            return (int) (o2.getValue() - o1.getValue());
        });

        // 返回一个符合阈值且相似度最高的工序对象
        return list.get(0).getKey();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertExperienceProcess(List<String> processList) {
        List<MicroProcessCommon> list = microProcessCommonMapper.selectExistByNames(processList);
        if (!CollectionUtils.isEmpty(list)) {
            Set<String> existSet = list.stream().map(MicroProcessCommon::getProcessName).collect(Collectors.toSet());
            processList = processList.stream().filter(p -> !existSet.contains(p)).collect(Collectors.toList());
        }
        MicroProcessCommon common;
        List<MicroProcessCommon> insertBatch = new ArrayList<>();
        for (String processName : processList) {
            common = new MicroProcessCommon();
            String productSeqNumber = supportUtil.getProcessSeqNumber(microProcessCommonMapper.selectProcessSeqAndCode());
            common.setTenantCode((String) ThreadContext.get(Constants.TARGET_CUSTOMER));
            common.setProcessName(processName);
            common.setProcessCode(productSeqNumber);
            common.setProcessSeq(productSeqNumber);
            common.setLastUpdDate(DateUtils.getNowDate());
            common.setCreateInfo();
            insertBatch.add(common);
        }
        if (!CollectionUtils.isEmpty(insertBatch)) {
            microProcessCommonMapper.insertMicroProcessCommonBatch(insertBatch);
            try {
                MicroSelectEntity hotData;
                for (MicroProcessCommon batch : insertBatch) {
                    hotData = new MicroSelectEntity();
                    hotData.setItemSeq(batch.getProcessSeq());
                    hotData.setItemCode(batch.getProcessCode());
                    hotData.setItemName(batch.getProcessName());
                    microRedisUtils.incrByHotData(HotKeyEnum.PRE_PROCESS, CommonConstants.HOT_KEY_SUFFIX_TIMES, hotData);
                    microRedisUtils.incrByHotData(HotKeyEnum.OPERATE_PROCESS, CommonConstants.HOT_KEY_SUFFIX_TIMES, hotData);
                }
            } catch (Exception e) {
                log.error("批量插入工序热点数据失败:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            }
        }
        return 1;
    }

    /**
     * 编辑工艺链流程中查询工序,审核后工序放在头部
     *
     * @param key
     * @param productSeq
     * @return
     */
    @Override
    public List<MicroSelectEntity> selectByChain(String key, String productSeq) {
        List<MicroSelectEntity> headList = new ArrayList<>();
        if (!StringUtils.isEmpty(productSeq)) {
            headList = submitMapper.selectProcessByProduct(productSeq, key);
        }
        List<MicroSelectEntity> result = microProcessCommonMapper.selectMicroProcessByName(key);
        if (!CollectionUtils.isEmpty(result)) {
            result.addAll(0, headList);
        }
        return selectEntitySort(key, result.stream().distinct().collect(Collectors.toList()));
    }

    /**
     * 有工艺的产品，推荐工艺默认的工序
     *
     * @param standardCraftChain
     * @param operateProcessSeq
     * @return
     */
    @Override
    public List<SimpleProcessResult> defaultRecommendByStandardTech(List<MicroProcessChainBindEntity> standardCraftChain, String operateProcessSeq) {
        List<SimpleProcessResult> res = new ArrayList<>();
        // 1. 根据当前工序的信息获取前工序的信息
        List<MicroProcessChainBindEntity> tempList = standardCraftChain.stream().filter(obj -> operateProcessSeq.equals(obj.getProcessSeq()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(tempList)) {
            // 找到当前工序对应的尾序seq
            String preProcessSeqList = tempList.stream().map(MicroProcessChainBindEntity::getParentProcessSeq).collect(Collectors.joining(","));
            res = microProcessCommonMapper.selectSimpleProcessBySeqList(preProcessSeqList);
            // 添加首尾序信息
            Map<String, List<MicroProcessChainBindEntity>> preProcessInfoMap = standardCraftChain.stream().filter(obj -> preProcessSeqList.contains(obj.getProcessSeq()))
                    .collect(Collectors.groupingBy(MicroProcessChainBindEntity::getProcessSeq));
            res.forEach(obj -> {
                MicroProcessChainBindEntity processTemp = preProcessInfoMap.get(obj.getProcessSeq()).get(0);
                if (ROOT_PARENT_PROCESS.equals(processTemp.getParentProcessSeq())) {
                    obj.setIsFirstProcess(CommonConstants.YES);
                } else {
                    obj.setIsFirstProcess(CommonConstants.NO);
                }
                obj.setIsLastProcess(processTemp.getIsLastProcess());
            });
        }

//        // 2. 根据前工的信息获取当前工序
//        if (StringUtils.isEmpty(operateProcessSeq)) {
//            List<MicroProcessChainBindDTO> tempList = standardCraftChain.stream().filter(obj -> preProcessSeq.equals(obj.getParentProcessSeq()))
//                    .collect(Collectors.toList());
//            if (!CollectionUtils.isEmpty(tempList)) {
//                String operateProcessSeqList = tempList.stream().map(MicroProcessChainBindDTO::getProcessSeq).collect(Collectors.joining(","));
//                // 查询基础的工序信息
//                res = microProcessCommonMapper.selectSimpleProcessBySeqList(operateProcessSeqList);
//                Map<String, List<MicroProcessChainBindDTO>> operateProcessMap = tempList.stream().collect(Collectors.groupingBy(MicroProcessChainBindDTO::getProcessSeq));
//
//                res.forEach(obj -> {
//                    MicroProcessChainBindDTO processTemp = operateProcessMap.get(obj.getProcessSeq()).get(0);
//                    // 不可能作为首序
//                    obj.setIsFirstProcess(CommonConstants.NO);
//                    obj.setIsLastProcess(processTemp.getIsLastProcess());
//                });
//            }
//        } 

        return res;
    }

    /**
     * 拿到F02推荐数据,进行产品/工序数据清洗,防止微应用删除产品/工序,F02未同步导致的脏数据
     *
     * @param recommendResult F02推荐数据
     * @return
     */
    @Override
    public Map<String, Object> filterInvalidProductAndProcess(Map<String, Object> recommendResult) {
        try {
            JSONArray highFrequencyProduct = judgeArray(recommendResult.get(HotKeyEnum.HIGH_FREQUENCY_PRODUCT.getDesc()));
            JSONArray preProcess = judgeArray(recommendResult.get(HotKeyEnum.PRE_PROCESS.getDesc()));
            JSONArray operateProcess = judgeArray(recommendResult.get(HotKeyEnum.OPERATE_PROCESS.getDesc()));
            List<String> verifyList;
            if (!highFrequencyProduct.isEmpty()) {
                verifyList = new ArrayList<>();
                this.addVerifyList(highFrequencyProduct, verifyList);
                List<String> productList = microProductMapper.selectFilterProductBySeqList(verifyList);
                this.filterItem(productList, highFrequencyProduct);
                recommendResult.put(HotKeyEnum.HIGH_FREQUENCY_PRODUCT.getDesc(), highFrequencyProduct);
            }
            verifyList = new ArrayList<>();
            if (!preProcess.isEmpty()) {
                this.addVerifyList(preProcess, verifyList);
            }
            if (!operateProcess.isEmpty()) {
                this.addVerifyList(operateProcess, verifyList);
            }
            List<String> processList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(verifyList)) {
                processList = microProcessCommonMapper.selectFilterProcessBySeqList(verifyList);
                if (CollectionUtils.isEmpty(processList)) {
                    return emptyResult();
                }
            }
            if (!preProcess.isEmpty()) {
                this.filterItem(processList, preProcess);
                recommendResult.put(HotKeyEnum.PRE_PROCESS.getDesc(), preProcess);
            }
            if (!operateProcess.isEmpty()) {
                this.filterItem(processList, operateProcess);
                recommendResult.put(HotKeyEnum.OPERATE_PROCESS.getDesc(), operateProcess);
            }
            return recommendResult;
        } catch (Exception e) {
            log.error("过滤推荐数据失败, 原推荐数据为:{},:{}:{}, Error:{}", recommendResult, e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            return emptyResult();
        }
    }

    private HashMap<String, Object> emptyResult() {
        HashMap<String, Object> emptyRecommend = new HashMap<>(16);
        emptyRecommend.put(HotKeyEnum.HIGH_FREQUENCY_PRODUCT.getDesc(), new ArrayList<>());
        emptyRecommend.put(HotKeyEnum.OPERATE_PROCESS.getDesc(), new ArrayList<>());
        emptyRecommend.put(HotKeyEnum.PRE_PROCESS.getDesc(), new ArrayList<>());
        return emptyRecommend;
    }

    private JSONArray judgeArray(Object o) {
        if (o instanceof JSONArray) {
            return (JSONArray) o;
        } else {
            return new JSONArray();
        }
    }


    private void addVerifyList(JSONArray array, List<String> verifyList) {
        for (Object o : array) {
            if (o != null && !((JSONObject) o).isEmpty()) {
                Object itemSeqJson = ((JSONObject) o).get("itemSeq");
                if (itemSeqJson != null) {
                    verifyList.add(itemSeqJson.toString());
                }
            }
        }
    }

    private void filterItem(List<String> verifyList, JSONArray toVerifyArray) {
        Map<String, String> collect = verifyList.stream().collect(Collectors.groupingBy(k -> k.split(",")[0],
                Collectors.collectingAndThen(Collectors.toList(), v -> v.get(0))));
        Iterator<Object> it = toVerifyArray.iterator();
        while (it.hasNext()) {
            JSONObject operate = (JSONObject) it.next();
            Object itemSeqJson = operate.get("itemSeq");
            if (itemSeqJson != null && collect.containsKey(itemSeqJson)) {
                String[] split = collect.get(itemSeqJson).split(",");
                if (split.length > 1) {
                    operate.put("itemCode", split[1]);
                    operate.put("itemName", split[2]);
                    continue;
                }
            }
            it.remove();
        }
    }
}
