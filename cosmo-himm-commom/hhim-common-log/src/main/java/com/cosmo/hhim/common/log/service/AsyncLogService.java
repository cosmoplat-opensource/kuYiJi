/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.log.service;

import com.cosmo.hhim.common.log.dto.SysOperLog;
import com.cosmo.hhim.common.log.mapper.SysOperLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;


/**
 * 异步调用日志服务
 *
 * @author cosmo-hhim-open Team
 */
public class AsyncLogService {

    @Autowired
    private SysOperLogMapper sysOperLogMapper;

    /**
     * 保存系统日志记录
     */
    @Async
    public void saveSysLog(SysOperLog sysOperLog) {
        sysOperLogMapper.insertOperlog(sysOperLog);
    }
}
