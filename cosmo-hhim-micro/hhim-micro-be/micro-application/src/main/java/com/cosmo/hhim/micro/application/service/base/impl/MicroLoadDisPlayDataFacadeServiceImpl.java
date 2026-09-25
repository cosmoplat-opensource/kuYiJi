/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.micro.application.service.base.IMicroLoadDisplayDataFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.analysis.AnalysisIndex;
import com.cosmo.hhim.micro.base.domain.entity.analysis.FinishedProduct;
import com.cosmo.hhim.micro.base.domain.entity.analysis.SubmitterRank;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageHistory;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitProductCount;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportStatisticsDayResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportStatisticsProductResult;
import com.cosmo.hhim.micro.infrastructure.constant.DisplayDataConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description: 导入演示数据实现类
 * @date 2023/3/29 11:08
 */
@Slf4j
@Service
public class MicroLoadDisPlayDataFacadeServiceImpl implements IMicroLoadDisplayDataFacadeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 演示数据资源文件(懒加载:Redis 无数据时从此文件读取并写回 Redis)
     */
    private static final String DEMO_DATA_RESOURCE = "demo-data.json";
    private static final Object DEMO_DATA_LOCK = new Object();
    private volatile Map<String, String> demoDataCache = null;

    /**
     * 演示数据展示接口
     *
     * @param apiUrl
     * @return
     */
    @Override
    public Object loadDisplayData(String apiUrl) { 
        log.info("请求的参数为:{}", apiUrl);
        // 校验入参
        if (StringUtils.isEmpty(apiUrl)) {
            throw new CustomException("接口url不能为空");
        }
        Object result = new Object();
        String jsonStr;
        // 根据接口url获取演示数据
        switch (apiUrl) {
            case DisplayDataConstants.INDEX_URL:
                jsonStr = getDemoData(DisplayDataConstants.INDEX_URL);
                result = JSONObject.parseObject(jsonStr, AnalysisIndex.class);
                break;
            case DisplayDataConstants.SUBMIT_RECORD_RANK_URL:
                jsonStr = getDemoData(DisplayDataConstants.SUBMIT_RECORD_RANK_URL);
                result = JSONObject.parseArray(jsonStr, SubmitterRank.class);
                break;
            case DisplayDataConstants.FINISHED_PRODUCT_STATISTICS_URL:
                jsonStr = getDemoData(DisplayDataConstants.FINISHED_PRODUCT_STATISTICS_URL);
                result = JSONObject.parseArray(jsonStr, FinishedProduct.class);
                break;
            case DisplayDataConstants.STORAGE_RANK_URL:
                jsonStr = getDemoData(DisplayDataConstants.STORAGE_RANK_URL);
                result = JSONObject.parseArray(jsonStr, MicroProcessStorage.class);
                break;
            case DisplayDataConstants.STORAGE_PRODUCT_LIST_URL:
                jsonStr = getDemoData(DisplayDataConstants.STORAGE_PRODUCT_LIST_URL);
                result = JSONObject.parseArray(jsonStr, MicroProcessStorage.class);
                break;
            case DisplayDataConstants.STORAGE_PRODUCT_PROCESS_LIST_URL:
                jsonStr = getDemoData(DisplayDataConstants.STORAGE_PRODUCT_PROCESS_LIST_URL);
                result = JSONObject.parseArray(jsonStr, MicroProcessStorage.class);
                break;
            case DisplayDataConstants.STORAGE_CHANGE_HISTORY_URL:
                jsonStr = getDemoData(DisplayDataConstants.STORAGE_CHANGE_HISTORY_URL);
                result = JSONObject.parseArray(jsonStr, MicroProcessStorageHistory.class);
                break;
            case DisplayDataConstants.PRODUCTION_TOTAL_COUNT_URL:
                jsonStr = getDemoData(DisplayDataConstants.PRODUCTION_TOTAL_COUNT_URL);
                result = JSONObject.parseObject(jsonStr, MicroWorkSubmitProductCount.class);
                break;
            case DisplayDataConstants.PRODUCTION_QUALITY_TREND_URL:
                jsonStr = getDemoData(DisplayDataConstants.PRODUCTION_QUALITY_TREND_URL);
                result = JSONObject.parseArray(jsonStr, MicroWorkSubmitProductCount.class);
                break;
            case DisplayDataConstants.COMPLETED_PRODUCT_INFORMATION_URL:
                jsonStr = getDemoData(DisplayDataConstants.COMPLETED_PRODUCT_INFORMATION_URL);
                result = JSONObject.parseArray(jsonStr, MicroWorkSubmitDto.class);
                break;
            case DisplayDataConstants.PASS_RATE_ANALYSIS_BY_PRODUCT_URL:
                jsonStr = getDemoData(DisplayDataConstants.PASS_RATE_ANALYSIS_BY_PRODUCT_URL);
                result = JSONObject.parseArray(jsonStr, MicroWorkSubmitProductCount.class);
                break;
            case DisplayDataConstants.PASS_RATE_ANALYSIS_BY_PROCESS_URL:
                jsonStr = getDemoData(DisplayDataConstants.PASS_RATE_ANALYSIS_BY_PROCESS_URL);
                result = JSONObject.parseArray(jsonStr, MicroWorkSubmitProductCount.class);
                break;
            case DisplayDataConstants.PASS_RATE_ANALYSIS_BY_EMPLOYEE_URL:
                jsonStr = getDemoData(DisplayDataConstants.PASS_RATE_ANALYSIS_BY_EMPLOYEE_URL);
                result = JSONObject.parseArray(jsonStr, SubmitterRank.class);
                break;
            case DisplayDataConstants.PASS_RATE_ANALYSIS_BY_PRODUCT_PROCESS_URL:
                jsonStr = getDemoData(DisplayDataConstants.PASS_RATE_ANALYSIS_BY_PRODUCT_PROCESS_URL);
                result = JSONObject.parseArray(jsonStr, MicroWorkSubmitDto.class);
                break;
            case DisplayDataConstants.PASS_RATE_ANALYSIS_BY_PROCESS_PRODUCT_URL:
                jsonStr = getDemoData(DisplayDataConstants.PASS_RATE_ANALYSIS_BY_PROCESS_PRODUCT_URL);
                result = JSONObject.parseArray(jsonStr, MicroWorkSubmitDto.class);
                break;
            case DisplayDataConstants.COMPLETED_PRODUCT_STATISTICS_BY_DAY_URL:
                jsonStr = getDemoData(DisplayDataConstants.COMPLETED_PRODUCT_STATISTICS_BY_DAY_URL);
                result = JSONObject.parseArray(jsonStr, MicroCompleteReportStatisticsDayResult.class);
                break;
            case DisplayDataConstants.COMPLETED_PRODUCT_STATISTICS_URL:
                jsonStr = getDemoData(DisplayDataConstants.COMPLETED_PRODUCT_STATISTICS_URL);
                result = JSONObject.parseArray(jsonStr, MicroCompleteReportStatisticsProductResult.class);
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 懒加载演示数据:
     * 优先读 Redis hash(micro:data:show);Redis 中不存在该 apiUrl 时,
     * 从本地资源文件 demo-data.json 读取对应 JSON 并写入 Redis(下次直接命中)。
     * 这样代码部署与 docker 部署都无需额外灌数据,Redis 清空/重建后也能自动恢复。
     *
     * @param apiUrl 接口url(与 DisplayDataConstants.*_URL 对应)
     * @return 演示数据 JSON 字符串
     */
    private String getDemoData(String apiUrl) {
        String jsonStr = (String) redisTemplate.boundHashOps(DisplayDataConstants.DISPLAY_DATA_KEY).get(apiUrl);
        if (StringUtils.isNotEmpty(jsonStr)) {
            return jsonStr;
        }
        // Redis 无数据 -> 读本地资源,避免并发重复读文件
        String resourceJson = null;
        synchronized (DEMO_DATA_LOCK) {
            jsonStr = (String) redisTemplate.boundHashOps(DisplayDataConstants.DISPLAY_DATA_KEY).get(apiUrl);
            if (StringUtils.isNotEmpty(jsonStr)) {
                return jsonStr;
            }
            Map<String, String> dataMap = loadDemoDataResource();
            resourceJson = dataMap == null ? null : dataMap.get(apiUrl);
            if (StringUtils.isEmpty(resourceJson)) {
                log.warn("演示数据未命中,apiUrl:{}", apiUrl);
                return null;
            }
            // 写回 Redis,后续请求直接命中
            redisTemplate.boundHashOps(DisplayDataConstants.DISPLAY_DATA_KEY).put(apiUrl, resourceJson);
        }
        return resourceJson;
    }

    /**
     * 加载 classpath:demo-data.json(仅首次,结果缓存到内存)
     * 文件格式: {"接口url":"JSON内容", "...":"..."}
     */
    private Map<String, String> loadDemoDataResource() {
        if (demoDataCache != null) {
            return demoDataCache;
        }
        synchronized (DEMO_DATA_LOCK) {
            if (demoDataCache != null) {
                return demoDataCache;
            }
            try (InputStream is = new ClassPathResource(DEMO_DATA_RESOURCE).getInputStream()) {
                byte[] bytes = new byte[is.available()];
                int len = is.read(bytes);
                String content = new String(bytes, 0, len, StandardCharsets.UTF_8);
                Map<String, Object> raw = JSONObject.parseObject(content);
                Map<String, String> map = new HashMap<>();
                for (Map.Entry<String, Object> entry : raw.entrySet()) {
                    map.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().toString());
                }
                demoDataCache = map;
                log.info("演示数据资源加载完成,共 {} 条", map.size());
                return map;
            } catch (Exception e) {
                log.warn("演示数据资源加载失败:{}", e.getMessage(), e);
                return null;
            }
        }
    }
}
