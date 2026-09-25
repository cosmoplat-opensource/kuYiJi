/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.entity.MicroCapacityUpLimitEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroExistEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroHotDataEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.enums.ApplicationTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.DataWarnTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.HotKeyEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 辅助服务类,用来包装一些业务额外的操作
 *
 * @author cosmo-hhim-open Team
 */
@Component
@Slf4j
public class MicroSupportUtil {
    @Autowired
    private MicroRedisUtils redisUtils;
    @Autowired
    private RedisCache redisCache;

    private static final String REDIS_TYPE = "@type";
    private static final int MAXIMUM_NUMBER = 1000;
    private static final int MINIMUM_NUMBER = 1;
    // 热门推荐数量上限：业务计数（非密钥），通过配置 hhim.recommend.tag-max / hhim.recommend.common-max 注入（默认 5/4）
    private int recommendTagMax = 5;
    private int recommendCommonMax = 4;

    @org.springframework.beans.factory.annotation.Value("${hhim.recommend.tag-max:5}")
    public void setRecommendTagMax(int recommendTagMax) {
        this.recommendTagMax = recommendTagMax;
    }

    @org.springframework.beans.factory.annotation.Value("${hhim.recommend.common-max:4}")
    public void setRecommendCommonMax(int recommendCommonMax) {
        this.recommendCommonMax = recommendCommonMax;
    }

    /**
     * 获取多个租户工序流水号，自动跳过已存在的产品号
     *
     * @param number 获取数量
     * @param codes
     * @return
     */
    public List<String> getMultiProcessSeqNumbers(int number, List<String> codes) {
        Assert.checkBetween(number, MINIMUM_NUMBER, MAXIMUM_NUMBER);
        List<String> seqList = new ArrayList<>();
        String seqNumber;
        for (int i = 0; i < number; i++) {
            seqNumber = CommonConstants.PROCESS_GENERATE_PREFIX + redisUtils.getSeqNumber(CommonConstants.SEQ_NUMBER_KEY_PROCESS + ThreadContext.get(Constants.TARGET_CUSTOMER).toString());
            if (CollectionUtils.isEmpty(codes)) {
                seqList.add(seqNumber);
            }
            seqList.add(recursionSeq(codes, seqNumber, CommonConstants.PROCESS_GENERATE_PREFIX, CommonConstants.SEQ_NUMBER_KEY_PROCESS));
        }
        return seqList;
    }

    /**
     * 获取租户工序流水号，自动跳过已存在的产品号
     *
     * @return 对象列表
     */
    public String getProcessSeqNumber(List<String> codes) {
        String seqNumber = CommonConstants.PROCESS_GENERATE_PREFIX + redisUtils.getSeqNumber(CommonConstants.SEQ_NUMBER_KEY_PROCESS + ThreadContext.get(Constants.TARGET_CUSTOMER).toString());
        if (CollectionUtils.isEmpty(codes)) {
            return seqNumber;
        }
        return recursionSeq(codes, seqNumber, CommonConstants.PROCESS_GENERATE_PREFIX, CommonConstants.SEQ_NUMBER_KEY_PROCESS);
    }

    /**
     * @param number
     * @return
     */
    public List<String> getMultiProductSeqNumber(int number, List<String> codes) {
        Assert.checkBetween(number, MINIMUM_NUMBER, MAXIMUM_NUMBER);
        List<String> seqList = new ArrayList<>();
        String seqNumber;
        for (int i = 0; i < number; i++) {
            seqNumber = CommonConstants.PRODUCT_GENERATE_PREFIX + redisUtils.getSeqNumber(CommonConstants.SEQ_NUMBER_KEY_PRODUCT + ThreadContext.get(Constants.TARGET_CUSTOMER).toString());
            if (CollectionUtils.isEmpty(codes)) {
                seqList.add(seqNumber);
            }
            seqList.add(recursionSeq(codes, seqNumber, CommonConstants.PRODUCT_GENERATE_PREFIX, CommonConstants.SEQ_NUMBER_KEY_PRODUCT));
        }
        return seqList;
    }

    /**
     * 获取租户工序流水号，自动跳过已存在的产品号
     *
     * @return 对象列表
     */
    public String getProductSeqNumber(List<String> codes) {
        String seqNumber = CommonConstants.PRODUCT_GENERATE_PREFIX + redisUtils.getSeqNumber(CommonConstants.SEQ_NUMBER_KEY_PRODUCT + ThreadContext.get(Constants.TARGET_CUSTOMER).toString());
        if (CollectionUtils.isEmpty(codes)) {
            return seqNumber;
        }
        return recursionSeq(codes, seqNumber, CommonConstants.PRODUCT_GENERATE_PREFIX, CommonConstants.SEQ_NUMBER_KEY_PRODUCT);
    }

    /**
     * 此处递归查找用户自定义编码与redis编码是否冲突
     * ※※※ 一般情况无需递归即可返回
     * ※※※ 特殊情况最多1-2次递归即可返回
     *
     * @param codes
     * @param seqNumber
     * @param type
     * @return
     */
    private String recursionSeq(List<String> codes, String seqNumber, String prefix, String type) {
        log.info("生成的序列码:{}", seqNumber);
        if (codes.contains(seqNumber)) {
            seqNumber = prefix + redisUtils.getSeqNumber(type + ThreadContext.get(Constants.TARGET_CUSTOMER).toString());
            return recursionSeq(codes, seqNumber, prefix, type);
        }
        return seqNumber;
    }

    /**
     * 移除redis中热点数据
     *
     * @param existList
     */
    @Async("redisOperateExecutor")
    public void removeProductHotData(List<MicroSelectEntity> existList) {
        if (!CollectionUtils.isEmpty(existList)) {
            for (MicroSelectEntity selectEntity : existList) {
                redisUtils.removeHotData(HotKeyEnum.HIGH_FREQUENCY_PRODUCT, CommonConstants.HOT_KEY_SUFFIX_SEQ, selectEntity);
                redisUtils.removeHotData(HotKeyEnum.HIGH_FREQUENCY_PRODUCT, CommonConstants.HOT_KEY_SUFFIX_TIMES, selectEntity);
            }
        }
    }

    /**
     * 移除redis中热点数据
     *
     * @param existList
     */
    @Async("redisOperateExecutor")
    public void removeProcessHotData(List<MicroExistEntity> existList) {
        if (!CollectionUtils.isEmpty(existList)) {
            MicroSelectEntity selectEntity;
            Set<Long> set = new HashSet<>();
            for (MicroExistEntity exist : existList) {
                if (!set.contains(exist.getId())) {
                    selectEntity = new MicroSelectEntity();
                    selectEntity.setItemSeq(exist.getItemSeq());
                    selectEntity.setItemCode(exist.getItemCode());
                    selectEntity.setItemName(exist.getItemName());
                    redisUtils.removeHotData(HotKeyEnum.OPERATE_PROCESS, CommonConstants.HOT_KEY_SUFFIX_SEQ, selectEntity);
                    redisUtils.removeHotData(HotKeyEnum.OPERATE_PROCESS, CommonConstants.HOT_KEY_SUFFIX_TIMES, selectEntity);
                    redisUtils.removeHotData(HotKeyEnum.PRE_PROCESS, CommonConstants.HOT_KEY_SUFFIX_SEQ, selectEntity);
                    redisUtils.removeHotData(HotKeyEnum.PRE_PROCESS, CommonConstants.HOT_KEY_SUFFIX_TIMES, selectEntity);
                    set.add(exist.getId());
                }
            }
        }
    }

    /**
     * 更新redis中热点数据
     * matchKey:"micro:hot_key:*process*:userId"
     *
     * @param entity 更新数据
     */
    @Async("redisOperateExecutor")
    public void updateProcessHotData(MicroSelectEntity entity) {
        String matchKey = CommonConstants.HOT_KEY + CommonConstants.MATCH_SYMBOL + "process" + CommonConstants.MATCH_SYMBOL + ThreadContext.get(Constants.TARGET_CUSTOMER) + CommonConstants.SEPARATOR_SYMBOL + SecurityUtils.getUserId();
        this.updateHotData(entity, matchKey);
    }

    /**
     * 更新redis中热点数据
     * matchKey:"micro:hot_key:high_frequency_product:*:userId"
     *
     * @param entity 更新数据
     */
    @Async("redisOperateExecutor")
    public void updateProductHotData(MicroSelectEntity entity) {
        String matchKey = CommonConstants.HOT_KEY_HIGH_FREQUENCY_PRODUCT + CommonConstants.MATCH_SYMBOL + ThreadContext.get(Constants.TARGET_CUSTOMER) + CommonConstants.SEPARATOR_SYMBOL + SecurityUtils.getUserId();
        this.updateHotData(entity, matchKey);
    }


    private Map<String, Set<MicroSelectEntity>> recommend() {
        HashMap<String, Set> timesMap = new HashMap<>(16);
        HashMap<String, Set> seqMap = new HashMap<>(16);
        for (HotKeyEnum keyEnum : HotKeyEnum.values()) {
            timesMap.put(keyEnum.getDesc(), redisUtils.getRankAndScoreByRange(keyEnum, CommonConstants.HOT_KEY_SUFFIX_TIMES, 0, 8));
            seqMap.put(keyEnum.getDesc(), redisUtils.getRankAndScoreByRange(keyEnum, CommonConstants.HOT_KEY_SUFFIX_SEQ, 0, 8));
        }
        Map<String, Set<MicroSelectEntity>> result = new HashMap<>(16);
//        timesMap.forEach((k, v) -> result.put(k, sortResult(v, seqMap.get(k)))); 
        timesMap.forEach((k, v) -> result.put(k, sortResultRaw(k, v, seqMap.get(k))));
        return result;
    }

    /**
     * 用户推荐,上次选择的排第一,然后使用次数倒序排
     *
     * @param k
     * @param timesSet
     * @param seqSet
     * @return
     */
    private Set<MicroSelectEntity> sortResultRaw(String k, Set timesSet, Set seqSet) {
        List<MicroHotDataEntity> timesResult = new ArrayList<>();
        if (!CollectionUtils.isEmpty(timesSet)) {
            List<DefaultTypedTuple> timesList = new ArrayList<DefaultTypedTuple>(timesSet);
            timesList.forEach(t -> {
                timesResult.add(hotDataValue(t));
            });
        } else {
            return new HashSet<>();
        }
        List<MicroHotDataEntity> seqResult = new ArrayList<>();
        if (!CollectionUtils.isEmpty(seqSet)) {
            List<DefaultTypedTuple> seqList = new ArrayList<DefaultTypedTuple>(seqSet);
            seqResult.add(hotDataValue(seqList.get(0)));
        }
        return getFinalSort(k, timesResult, seqResult);
    }

    private MicroHotDataEntity hotDataValue(DefaultTypedTuple t) {
        MicroHotDataEntity hotData = new MicroHotDataEntity();
        hotData.setScore(t.getScore().longValue());
        Object value = t.getValue();
        if (value instanceof JSONObject) {
            ((JSONObject) value).remove(REDIS_TYPE);
            hotData.setValue(JSONObject.parseObject(value.toString(), MicroSelectEntity.class));
        } else if (value instanceof MicroSelectEntity) {
            hotData.setValue((MicroSelectEntity) value);
        }
        return hotData;
    }


    private Set<MicroSelectEntity> getFinalSort(String keyType, List<MicroHotDataEntity> timesResult, List<MicroHotDataEntity> seqResult) {
        if (!CollectionUtils.isEmpty(seqResult)) {
            seqResult.addAll(timesResult);
            timesResult = seqResult;
        }
        LinkedHashMap<MicroSelectEntity, Long> tempMap = new LinkedHashMap<>();
        for (MicroHotDataEntity entity : timesResult) {
            if (tempMap.containsKey(entity.getValue())) {
                tempMap.put(entity.getValue(), tempMap.get(entity.getValue()) + entity.getScore());
            } else {
                tempMap.put(entity.getValue(), entity.getScore());
            }
        }
        List<MicroHotDataEntity> sortList = new ArrayList<>();
        tempMap.forEach((k, v) -> {
            MicroHotDataEntity hotData = new MicroHotDataEntity();
            hotData.setScore(v);
            hotData.setValue(k);
            sortList.add(hotData);
        });
        sortList.sort((o1, o2) -> (int) (o2.getScore() - o1.getScore()));
        int max;
        if (HotKeyEnum.HOT_TAG_STORAGE_CHANGE.getDesc().equals(keyType)) {
            max = recommendTagMax;
        } else {
            max = recommendCommonMax;
        }
        return sortList.subList(0, Math.min(sortList.size(), max)).stream().map(MicroHotDataEntity::getValue).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 记录用户常用工序/产品等
     *
     * @param productEntity 产品实体
     * @param operateProcessEntity 当前工序实体
     * @param preProcessEntity 前工序实体
     */
    @Async("redisOperateExecutor")
    public void addHotData(MicroSelectEntity productEntity, MicroSelectEntity operateProcessEntity, MicroSelectEntity preProcessEntity) {
        try {
//             每次报工需要记录登录人报工次数,超过5次前端会更换成智能报工页面
            redisUtils.incrBy(CommonConstants.SUBMIT_TIMES + ThreadContext.get(Constants.TARGET_CUSTOMER) + CommonConstants.SEPARATOR_SYMBOL + SecurityUtils.getUserId());
            MicroSelectEntity operateProcessHotData = new MicroSelectEntity();
            MicroSelectEntity productHotData = new MicroSelectEntity();
            MicroSelectEntity preProcessHotData;
            Map<String, MicroSelectEntity> useMap = new HashMap<>(8);
            if (!StringUtils.isEmpty(preProcessEntity.getItemSeq())) {
                String[] preSeqs = preProcessEntity.getItemSeq().split(",");
                String[] preCodes = preProcessEntity.getItemCode().split(",");
                String[] preNames = preProcessEntity.getItemName().split(",");
                if (preSeqs.length == preCodes.length && preCodes.length == preNames.length) {
                    for (int i = 0; i < preSeqs.length; i++) {
                        preProcessHotData = new MicroSelectEntity();
                        String preCode = preCodes[i];
                        String preName = preNames[i];
                        String preSeq = preSeqs[i];
                        preProcessHotData.setItemSeq(preSeq);
                        preProcessHotData.setItemCode(preCode);
                        preProcessHotData.setItemName(preName);
                        useMap.put(HotKeyEnum.PRE_PROCESS.getDesc(), preProcessHotData);
                    }
                }
            }
            if (!StringUtils.isEmpty(operateProcessEntity.getItemSeq())) {
                operateProcessHotData.setItemSeq(operateProcessEntity.getItemSeq());
                operateProcessHotData.setItemCode(operateProcessEntity.getItemCode());
                operateProcessHotData.setItemName(operateProcessEntity.getItemName());
                useMap.put(HotKeyEnum.OPERATE_PROCESS.getDesc(), operateProcessHotData);
            }
            if (!StringUtils.isEmpty(productEntity.getItemSeq())) {
                productHotData.setItemSeq(productEntity.getItemSeq());
                productHotData.setItemCode(productEntity.getItemCode());
                productHotData.setItemName(productEntity.getItemName());
                useMap.put(HotKeyEnum.HIGH_FREQUENCY_PRODUCT.getDesc(), productHotData);
            }
            Double timeSeq = Double.valueOf(DateUtils.dateTimeNow(DateUtils.YYYYMMDDHHMMSS));
            useMap.forEach((k, v) -> {
                HotKeyEnum keyEnum = HotKeyEnum.getEnumByDesc(k);
                redisUtils.incrByHotData(keyEnum, CommonConstants.HOT_KEY_SUFFIX_TIMES, v);
                redisUtils.addHotData(keyEnum, CommonConstants.HOT_KEY_SUFFIX_SEQ, v, timeSeq);
            });
        } catch (Exception e) {
            log.error("记录推荐失败:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
    }


    /**
     * 更新热点数据
     *
     * @param newEntity 新数据
     * @param key       热点数据key
     */
    public void updateHotData(MicroSelectEntity newEntity, String key) {
        try {
            Set<String> match = redisUtils.match(key);
            if (!CollectionUtils.isEmpty(match)) {
                match.forEach(m -> {
                    Set all = redisUtils.getRank(m, 0, -1);
                    if (!CollectionUtils.isEmpty(all)) {
                        List<DefaultTypedTuple> timesList = new ArrayList<DefaultTypedTuple>(all);
                        MicroSelectEntity oldValue = new MicroSelectEntity();
                        for (DefaultTypedTuple tuple : timesList) {
                            Object value = tuple.getValue();
                            if (value instanceof JSONObject) {
                                ((JSONObject) value).remove(REDIS_TYPE);
                                oldValue = JSONObject.parseObject(value.toString(), MicroSelectEntity.class);
                            } else if (value instanceof MicroSelectEntity) {
                                oldValue = (MicroSelectEntity) value;
                            }
                            if (newEntity.getItemSeq().equals(oldValue.getItemSeq())) {
                                redisUtils.removeZset(m, oldValue);
                                redisUtils.addZset(m, newEntity, tuple.getScore());
                                log.debug("hot data update success!:key->{},newEntity->{},score->{}", m, newEntity, tuple.getScore());
                                break;
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to update hot data from redis:{}:{}, Error:{}, the key is {}, the data is {}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), key, newEntity, e);
        }
    }


    /**
     * 根据用户名查询用户基础信息
     * ⭐⭐️️请谨慎在异步线程中使用, 可能会造成无法获取tenantCode问题
     *
     * @param userId 用户名
     * @return 用户基础信息
     */
    public Object getUserCache(String userId) {
        return this.getUserCache(userId, (String) ThreadContext.get(Constants.TARGET_CUSTOMER));
    }

    /**
     * 根据用户名查询用户基础信息
     *
     * @param userId     用户名
     * @param tenantCode 租户名
     * @return 用户基础信息
     * 更新用户时
     */
    public Object getUserCache(String userId, String tenantCode) {
        String appSign = SecurityUtils.getApplicationSign();
        Assert.notNull(userId, "userId can not be empty");
        Object cacheObject = redisCache.getCacheObject(CommonConstants.USER_CACHE_INFO + appSign + ":" + tenantCode + ":" + userId);
        if (cacheObject != null) {
            return cacheObject;
        }
        return null;
    }

    /**
     * 新增标签热点数据
     *
     * @param tags
     */
    public void addHotTag(String[] tags) {
        MicroSelectEntity entity;
        for (String tag : tags) {
            entity = new MicroSelectEntity();
            entity.setItemName(tag);
            entity.setItemCode(tag);
            entity.setItemSeq(tag);
            Double timeSeq = Double.valueOf(DateUtils.dateTimeNow(DateUtils.YYYYMMDDHHMMSS));
            redisUtils.addHotData(HotKeyEnum.HOT_TAG_STORAGE_CHANGE, CommonConstants.HOT_KEY_SUFFIX_SEQ, entity, timeSeq);
            redisUtils.incrByHotData(HotKeyEnum.HOT_TAG_STORAGE_CHANGE, CommonConstants.HOT_KEY_SUFFIX_TIMES, entity);
        }
    }

    /**
     * 获取某一组产品+工序目前的良品率及日产能指标
     * 返回值参考范例 key值{@link DataWarnTypeEnum}
     * {
     * AVG_PRODUCTION_CAPACITY=2.00000000,
     * PASS_RATE=0.5000,
     * SEQ_KEY=P000002_GX000003
     * }
     *
     * @param productSeq 产品唯一码
     * @param processSeq 工序唯一码
     * @return Map<String, Object>
     */
    public Map<String, Object> getWarningMetricsBySeq(String productSeq, String processSeq) {
        String redisKeyPrefix = CommonConstants.WARNING_KEY + ThreadContext.get(Constants.TARGET_CUSTOMER) + CommonConstants.SEPARATOR_SYMBOL;
        String field = productSeq + "_" + processSeq;
        String redisKey = redisKeyPrefix + field;
        return redisCache.getCacheMap(redisKey);
    }

    /**
     * 获取所有产品+工序目前的良品率及日产能指标
     * 返回值参考范例 key值{@link DataWarnTypeEnum}
     * {
     * AVG_PRODUCTION_CAPACITY=2.00000000,
     * PASS_RATE=0.5000,
     * SEQ_KEY=P000002_GX000003
     * }
     *
     * @return Map<String, Map < String, Object>>
     */
    public Map<String, Map<String, Object>> getWarningMetrics() {
        String redisKeyPrefix = CommonConstants.WARNING_KEY + ThreadContext.get(Constants.TARGET_CUSTOMER) + CommonConstants.SEPARATOR_SYMBOL;
        Set<String> match = redisCache.match(redisKeyPrefix);
        if (CollectionUtils.isEmpty(match)) {
//            return flushMetricsToRedis(redisKeyPrefix);
            return Collections.emptyMap();
        }
        Map<String, Map<String, Object>> cacheMap = new HashMap<>(16);
        match.forEach(k -> cacheMap.put(k.substring(redisKeyPrefix.length()), redisCache.getCacheMap(k)));
        return cacheMap;
    }


    public Map<String, Set<MicroSelectEntity>> recommendChangeTag() {
        HashMap<String, Set> timesMap = new HashMap<>(16);
        HashMap<String, Set> seqMap = new HashMap<>(16);
        HotKeyEnum changeEnum = HotKeyEnum.HOT_TAG_STORAGE_CHANGE;
        Map<String, Set<MicroSelectEntity>> result = new HashMap<>(16);
        try {
            timesMap.put(changeEnum.getDesc(), redisUtils.getRankAndScoreByRange(changeEnum, CommonConstants.HOT_KEY_SUFFIX_TIMES, 0, 8));
            seqMap.put(changeEnum.getDesc(), redisUtils.getRankAndScoreByRange(changeEnum, CommonConstants.HOT_KEY_SUFFIX_SEQ, 0, 8));
            timesMap.forEach((k, v) -> result.put(k, sortResultRaw(k, v, seqMap.get(k))));
        } catch (Exception e) {
            log.error("推荐标签失败:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
        return result;
    }

    public Map<String, Object> mergeRecommendData(Map<String, Set<MicroSelectEntity>> result) {
        Map<String, Set<MicroSelectEntity>> hotMap = new HashMap<>(8);
        Map<String, Object> resultMap = new HashMap<>(16);
        try {
            hotMap = this.recommend();
        } catch (Exception e) {
            log.warn("内部推荐失败:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
        for (HotKeyEnum keyEnum : HotKeyEnum.values()) {
            Set<MicroSelectEntity> dbDataSet = result.get(keyEnum.getDesc());
            if (CollectionUtils.isEmpty(dbDataSet)) {
                resultMap.put(keyEnum.getDesc(), Collections.emptyList());
                continue;
            }
            ArrayList<MicroSelectEntity> dbData = new ArrayList<>(dbDataSet);
            List<MicroSelectEntity> copyList = BeanUtil.copyToList(dbData, MicroSelectEntity.class);
            Set<MicroSelectEntity> hotData = hotMap.get(keyEnum.getDesc());
            if (!CollectionUtils.isEmpty(hotData)) {
                //取数据库和热点数据的交集
                dbData.retainAll(hotData);
                //交集小于推荐的4个数量,
                if (dbData.size() < recommendCommonMax) {
                    copyList.removeAll(dbData);
                    dbData.addAll(copyList);
                }
            }
//            if (dbData.size() > RECOMMEND_COMMON_MAX) {
//                dbData = new ArrayList<>(dbData.subList(0, RECOMMEND_COMMON_MAX));
//            } 
            resultMap.put(keyEnum.getDesc(), dbData);
        }
        return resultMap;
    }

    /**
     * 获取日产能上限
     *
     * @param upLimitEntity
     * @return
     */
    public BigDecimal getCapacityUpLimit(MicroCapacityUpLimitEntity upLimitEntity) {
        try {
            String targetCustomer = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
            String userId = upLimitEntity.getUserId();
            String productSeq = upLimitEntity.getProductSeq();
            String operateProcessSeq = upLimitEntity.getOperateProcessSeq();
            String redisKey = getMultiArgRedisKey("micro_f02_capacity_up_limit", targetCustomer, productSeq, operateProcessSeq, userId);
            Object upLimitObject = redisCache.getCacheObject(redisKey);
            if (upLimitObject != null) {
                return NumberUtils.parseNumber(upLimitObject.toString(), BigDecimal.class);
            }
        } catch (Exception e) {
            log.warn("Failed to get capacity up limit by redis:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
        return BigDecimal.valueOf(Long.MAX_VALUE);
    }

    private String getMultiArgRedisKey(String prefix, String... args) {
        org.springframework.util.Assert.notEmpty(args, "redis后缀key值不能为空");
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        for (String arg : args) {
            sb.append(CommonConstants.SEPARATOR_SYMBOL);
            sb.append(arg);
        }
        return sb.toString();
    }

    /**
     * 判断应用标示，确定报工方式
     */
    public Long getSubmitType() {
        // 判断应用标示(工易派还是ku易记)
        String appSign = SecurityUtils.getApplicationSign();
        if (appSign == null) {
            throw new CustomException("小程序应用标示为空");
        }
        Long submitType;
        if (appSign.equals(ApplicationTypeEnum.KU_YI_JI.getCode())) {
            submitType = SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode();
        } else {
            submitType = SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode();
        }
        return submitType;
    }

    /**
     * redis生成的订单号
     * <p>
     * 要判断是否已经存在生成的订单号
     *
     * @param alreadyExistOrderNoList
     * @return
     */
    public String getOrderNo(List<String> alreadyExistOrderNoList) {
        String orderNo = redisCache.incrCodeByDay(CommonConstants.MANUFACTURE_ORDER_GENERATE_PREFIX, 1, 6);
        if (CollectionUtil.isEmpty(alreadyExistOrderNoList)) {
            return orderNo;
        }
        return recursionNumber(alreadyExistOrderNoList, orderNo, CommonConstants.MANUFACTURE_ORDER_GENERATE_PREFIX, 6);
    }

    /**
     * redis生成工单号
     * <p>
     * 要判断是否已经存在生成的工单号
     *
     * @param alreadyExistWorkOderNoList
     * @return
     */
    public String getWorkOderNo(List<String> alreadyExistWorkOderNoList) {
        String workOrderNo = redisCache.incrCodeByDay(CommonConstants.MANUFACTURE_WORK_ORDER_GENERATE_PREFIX, 1, 3);
        if (CollectionUtil.isEmpty(alreadyExistWorkOderNoList)) {
            return workOrderNo;
        }
        return recursionNumber(alreadyExistWorkOderNoList, workOrderNo, CommonConstants.MANUFACTURE_WORK_ORDER_GENERATE_PREFIX, 3);
    }

    /**
     * 递归获取工单或者订单号信息
     * <p>
     * ※※※ 一般情况无需递归即可返回
     * ※※※ 特殊情况最多1-2次递归即可返回
     *
     * @param codes
     * @param number
     * @param prefix
     * @return
     */
    public String recursionNumber(List<String> codes, String number, String prefix, int length) {
        log.info("生成的单号为:{}", number);
        if (codes.contains(number)) {
            number = redisCache.incrCodeByDay(prefix, 1, length);
            return recursionNumber(codes, number, prefix, length);
        }
        return number;
    }
}
