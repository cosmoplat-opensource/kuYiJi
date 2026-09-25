/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.config;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DynamicDataSource 动态数据源路由单元测试
 */
public class DynamicDataSourceTest {

    private DynamicDataSource dynamicDataSource;

    @Before
    public void setUp() {
        dynamicDataSource = new DynamicDataSource();
        ThreadContext.clear();
    }

    @After
    public void tearDown() {
        ThreadContext.clear();
    }

    @Test
    public void testDetermineCurrentLookupKey_default() {
        // When ThreadContext has no TARGET_DS, returns null
        assertNull(dynamicDataSource.determineCurrentLookupKey());
    }

    @Test
    public void testDetermineCurrentLookupKey_db0() {
        ThreadContext.put(Constants.TARGET_DS, "db0");
        assertEquals("db0", dynamicDataSource.determineCurrentLookupKey());
    }

    @Test
    public void testDetermineCurrentLookupKey_db1() {
        ThreadContext.put(Constants.TARGET_DS, "db1");
        assertEquals("db1", dynamicDataSource.determineCurrentLookupKey());
    }
}
