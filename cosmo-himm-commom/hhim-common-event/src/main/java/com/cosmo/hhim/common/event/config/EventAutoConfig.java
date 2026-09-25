/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.config;

import com.cosmo.hhim.common.event.EventManager;
import com.cosmo.hhim.common.event.listener.CommonListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description 事件自动配置类，实现启动spring自动加载监听器后，自动注册监听器
 * @createTime 2022-07-19
 */
@Slf4j
@Configuration
public class EventAutoConfig implements ApplicationRunner {

//    @Autowired(required = false)
//    private List<CommonListener> commonListenerList;
    @Autowired
    private ObjectProvider<List<CommonListener>> commonListenerObjectProvider;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<CommonListener> commonListenerList = commonListenerObjectProvider.getIfAvailable();
        if (!CollectionUtils.isEmpty(commonListenerList)) {
            for (CommonListener listener : commonListenerList) {
                EventManager.registerListener(listener);
            }
        }
    }
}
