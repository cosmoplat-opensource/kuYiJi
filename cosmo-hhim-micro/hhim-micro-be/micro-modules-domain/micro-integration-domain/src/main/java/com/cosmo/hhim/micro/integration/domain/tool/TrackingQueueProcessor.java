/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.tool;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.utils.utils.DBControlUtil;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserEventTrackingEntity;
import com.cosmo.hhim.micro.integration.domain.mapper.MicroUserEventTrackingMapper;
import com.google.common.collect.Queues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
@EnableScheduling
public class TrackingQueueProcessor {
    private static final String UNKNOWN = "unknown";
    private final LinkedBlockingQueue<MicroUserEventTrackingEntity> collectQueue = new LinkedBlockingQueue<>();
    private final MicroUserEventTrackingMapper trackingMapper;
    public static final int MAX_SIZE = 100;
    public static final int TIME_OUT = 2;


    public TrackingQueueProcessor(MicroUserEventTrackingMapper trackingMapper) {
        this.trackingMapper = trackingMapper;
    }


    /**
     * 存入数据
     *
     * @param bufferList
     */
    private void insertDB(List<MicroUserEventTrackingEntity> bufferList) { 
        if (!CollectionUtils.isEmpty(bufferList)) {
            Map<String, List<MicroUserEventTrackingEntity>> collect = bufferList.stream().collect(Collectors.groupingBy(MicroUserEventTrackingEntity::getTenantCode));
            for (Map.Entry<String, List<MicroUserEventTrackingEntity>> entry : collect.entrySet()) {
                String tenantCode = entry.getKey();
                List<MicroUserEventTrackingEntity> value = entry.getValue();
                // decouple-from-ops-platform：单库场景，datasource 固定 db0
                DBControlUtil.setDbAndSchema("db0", CommonConstants.MICRO_SCHEMA, tenantCode);
                if (!CollectionUtils.isEmpty(value)) {
                    try {
                        // 兜底：上游埋点可能缺 NOT NULL 字段（如 phonenumber 为 null），
                        // 补默认值避免整批插入失败导致行为数据丢失（正常上报数据不受影响）
                        for (MicroUserEventTrackingEntity e : value) {
                            fillNotNullDefaults(e);
                            truncateOverLength(e);
                        }
                        trackingMapper.insertMicroUserEventTrackingBatch(value);
                    } catch (Exception e) {
                        log.warn("Failed to insert tracking data:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
                    }
                }
            }
        }
    }


    @Scheduled(cron = "0/10 * * * * ?")
    private void collectTracking() {
        if (!collectQueue.isEmpty()) {
            log.debug("tracking: start collecting queued events");
            List<MicroUserEventTrackingEntity> bufferList = new ArrayList<>();
            try {
                Queues.drain(collectQueue, bufferList, MAX_SIZE, TIME_OUT, TimeUnit.SECONDS);
                insertDB(bufferList);
            } catch (InterruptedException e) {
                log.warn("Failed to get tracking queue data:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
                Thread.currentThread().interrupt();
            }
        }

    }

    /**
     * 兜底 NOT NULL 字段（正常上报数据不会走到这里，仅防止脏数据导致整批插入失败）。
     * 对应 micro_user_event_tracking 表中无默认值的 NOT NULL 列。
     */
    private void fillNotNullDefaults(MicroUserEventTrackingEntity e) {
        if (e.getEventCode() == null) e.setEventCode(UNKNOWN);
        if (e.getEventName() == null) e.setEventName(UNKNOWN);
        if (e.getEventType() == null) e.setEventType(UNKNOWN);
        if (e.getEventSource() == null) e.setEventSource(UNKNOWN);
        if (e.getUserType() == null) e.setUserType(UNKNOWN);
        if (e.getPhonenumber() == null) e.setPhonenumber(0L);
        if (e.getUserAgent() == null) e.setUserAgent("");
        if (e.getCurrentPage() == null) e.setCurrentPage("");
        if (e.getApplicationSign() == null) e.setApplicationSign(UNKNOWN);
        // createdDay/createdMonth 优先从 createdDate 推导，无则置 0
        if (e.getCreatedDay() == null) {
            e.setCreatedDay(e.getCreatedDate() != null
                    ? Long.parseLong(DateUtil.format(e.getCreatedDate(), "yyyyMMdd")) : 0L);
        }
        if (e.getCreatedMonth() == null) {
            e.setCreatedMonth(e.getCreatedDate() != null
                    ? Long.parseLong(DateUtil.format(e.getCreatedDate(), "yyyyMM")) : 0L);
        }
    }

    /**
     * 超长字段截断兜底:按 micro_user_event_tracking 表列宽截断,
     * 避免 Data too long 导致整批插入失败、行为数据丢失(正常上报数据不受影响)。
     */
    private void truncateOverLength(MicroUserEventTrackingEntity e) {
        e.setEventCode(truncate(e.getEventCode(), 40));
        e.setEventName(truncate(e.getEventName(), 80));
        e.setEventType(truncate(e.getEventType(), 10));
        e.setEventContent(truncate(e.getEventContent(), 255));
        e.setEventSource(truncate(e.getEventSource(), 255));
        e.setUserType(truncate(e.getUserType(), 10));
        e.setTenantCode(truncate(e.getTenantCode(), 40));
        e.setTenantName(truncate(e.getTenantName(), 80));
        e.setUserIp(truncate(e.getUserIp(), 255));
        e.setUserLocation(truncate(e.getUserLocation(), 255));
        e.setCurrentPage(truncate(e.getCurrentPage(), 255));
        e.setReferPage(truncate(e.getReferPage(), 255));
        e.setApplicationSign(truncate(e.getApplicationSign(), 32));
    }

    private String truncate(String s, int maxLength) {
        if (s == null) {
            return null;
        }
        return s.length() > maxLength ? s.substring(0, maxLength) : s;
    }

    public void addToQueue(MicroUserEventTrackingEntity entity) {
        collectQueue.add(entity);
    }

}
