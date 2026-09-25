/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.clock.handler;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCacheCustomer;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.tips.MicroContentTipRecord;
import com.cosmo.hhim.micro.base.domain.entity.tips.MicroContentTipRecordParam;
import com.cosmo.hhim.micro.base.domain.entity.tips.TimeTriggerInfo;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroContentTipRecordMapper;
import com.cosmo.hhim.micro.infrastructure.enums.UserTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-02-01
 */
@Slf4j
public abstract class AbstractClockTipHandler implements ClockTipHandler {

    @Autowired
    private RedisCacheCustomer redisCacheCustomer;

    @Autowired
    private MicroContentTipRecordMapper microContentTipRecordMapper;

    // decouple-from-ops-platform-cleanup (A.2): trial service 引用已删除（trial 类整体下线）

    /**
     * 获取当前用户上次触发时间
     *
     * @return
     */
    protected Date getLastTriggerDate(TimeTriggerInfo timeTriggerInfo) {
        // decouple-from-ops-platform-cleanup (A.2): trial 分支已下线
        Long userId = SecurityUtils.getUserId();
        String userType = UserTypeEnum.OFFICIAL_USER.getCode();

        // 老的 trial 分支（SecurityUtils.getTrialPhone）已 no-op：永远走 OFFICIAL_USER 路径
        // 老代码：if (StringUtils.hasText(SecurityUtils.getTrialPhone())) { ... getTrialUserIdByTrialPhone ... }

        // 查询上次触发时间
        MicroContentTipRecord microContentTipRecord = microContentTipRecordMapper
                .selectLastMicroContentTipRecord(userId, userType, timeTriggerInfo.getTipConfigId());
        if (null == microContentTipRecord) {
            return null;
        }

        return microContentTipRecord.getTipDate();
    }

    /**
     * 获取限定时间范围内已提示次数
     *
     * @param timeTriggerInfo
     * @return
     */
    protected Long getDateRangeTriggerNum(TimeTriggerInfo timeTriggerInfo) {
        List<MicroContentTipRecord> microContentTipRecords = this.getContentTipRecordListByDateRange(timeTriggerInfo);
        if (!CollectionUtils.isEmpty(microContentTipRecords)) {
            return (long) microContentTipRecords.size();
        }
        return 0L;
    }

    /**
     * 获取限定时间范围内提示记录列表
     *
     * @param timeTriggerInfo
     * @return
     */
    protected List<MicroContentTipRecord> getContentTipRecordListByDateRange(TimeTriggerInfo timeTriggerInfo) {
        // 正式和试用用户信息区别设置（decouple-from-ops-platform-cleanup (A.2)：trial 分支已下线，与 getLastTriggerDate 保持一致）
        Long userId = SecurityUtils.getUserId();
        String userType = UserTypeEnum.OFFICIAL_USER.getCode();

        Date nowDate = DateUtils.getNowDate();
        Date endDate = DateUtil.endOfDay(nowDate).toJdkDate();
        Date startDate = com.cosmo.hhim.micro.infrastructure.util.DateUtil
                .plus(endDate, -1 * timeTriggerInfo.getPeriod(), timeTriggerInfo.getPeriodTimeUnit());

        // 查询该用户在指定时间范围内的触发记录
        MicroContentTipRecordParam param = new MicroContentTipRecordParam();
        param.setUserId(userId);
        param.setUserType(userType);
        param.setTipConfigId(timeTriggerInfo.getTipConfigId());
        param.setStartDate(startDate);
        param.setEndDate(endDate);
        return microContentTipRecordMapper.selectMicroContentTipRecordList(param);
    }

}
