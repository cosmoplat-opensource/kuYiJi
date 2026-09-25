/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;

/**
 * Arith 精确算术工具类单元测试
 */
public class ArithTest {

    @Test
    public void testAdd() {
        assertEquals(0.3, Arith.add(0.1, 0.2), 0.0001);
    }

    @Test
    public void testSub() {
        assertEquals(0.1, Arith.sub(0.3, 0.2), 0.0001);
    }

    @Test
    public void testMul() {
        assertEquals(0.06, Arith.mul(0.2, 0.3), 0.0001);
    }

    @Test
    public void testDiv() {
        assertEquals(2.0, Arith.div(0.6, 0.3), 0.0001);
    }

    @Test
    public void testDiv_scale() {
        assertEquals(0.33, Arith.div(1.0, 3.0, 2), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDiv_negativeScale() {
        Arith.div(1.0, 3.0, -1);
    }

    @Test
    public void testDiv_byZeroReturnsZero() {
        // Arith.div returns 0 when b1 is BigDecimal.ZERO (not divide by zero)
        assertEquals(0.0, Arith.div(0.0, 100.0), 0.0001);
    }

    @Test
    public void testRound() {
        assertEquals(3.14, Arith.round(3.14159, 2), 0.001);
        assertEquals(3.15, Arith.round(3.145, 2), 0.001);
    }

    @Test
    public void testRound_halfUp() {
        assertEquals(3.15, Arith.round(3.145, 2), 0.001);
    }
}
