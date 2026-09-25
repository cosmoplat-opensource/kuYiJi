/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.events;

import com.google.common.collect.HashMultimap;
import org.springframework.context.ApplicationEvent;

/**
 * 期初库存导入绑定对应工艺链事件
 *
 * @author cosmo-hhim-open Team
 */
public class StorageImport2TechEvent<K, V> extends ApplicationEvent {
    /**
     * 接收信息
     */
    private final HashMultimap<K, V> map;

    public StorageImport2TechEvent(HashMultimap<K, V> map) {
        super(map);
        this.map = map;
    }

    public HashMultimap<K, V> getMap() {
        return map;
    }
}