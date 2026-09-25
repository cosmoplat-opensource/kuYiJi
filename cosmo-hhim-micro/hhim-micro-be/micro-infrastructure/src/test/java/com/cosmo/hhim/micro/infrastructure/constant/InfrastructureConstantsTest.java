/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.constant;

import org.junit.Test;
import static org.junit.Assert.*;

public class InfrastructureConstantsTest {

    @Test public void testCommonConstants() {
        assertEquals("`im_micro`", CommonConstants.MICRO_SCHEMA);
        assertNotNull(CommonConstants.SUBMIT_TIMES);
        assertEquals("1", CommonConstants.NOT_HAVE_FIRST_PROCESS_WARN_TYPE);
        assertEquals("2", CommonConstants.NOT_HAVE_LAST_PROCESS_WARN_TYPE);
        assertEquals("3", CommonConstants.MULTI_LAST_PROCESS_WARN_TYPE);
        assertEquals("0", CommonConstants.YES);
        assertEquals("1", CommonConstants.NO);
        assertEquals("P", CommonConstants.PRODUCT_GENERATE_PREFIX);
        assertEquals("GX", CommonConstants.PROCESS_GENERATE_PREFIX);
        assertEquals("SC", CommonConstants.MANUFACTURE_ORDER_GENERATE_PREFIX);
        assertEquals("MO", CommonConstants.MANUFACTURE_WORK_ORDER_GENERATE_PREFIX);
        assertTrue(CommonConstants.ACTIVE_ENV_SET.contains("test"));
        assertTrue(CommonConstants.ACTIVE_ENV_SET.contains("prod"));
    }

    @Test public void testContentTipConstants() {
        assertEquals("submitUser", ContentTipConstants.KEY_A);
        assertEquals("totalNum", ContentTipConstants.KEY_B);
        assertEquals("1", ContentTipConstants.FIRST_ENTER_FLAG);
    }
}
