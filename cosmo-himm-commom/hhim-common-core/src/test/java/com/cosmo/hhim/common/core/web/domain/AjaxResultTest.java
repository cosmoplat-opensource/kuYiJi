/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.web.domain;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * AjaxResult 统一响应体单元测试
 */
public class AjaxResultTest {

    @Test
    public void testSuccess_noArgs() {
        AjaxResult result = AjaxResult.success();
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMsg());
    }

    @Test
    public void testSuccess_withData() {
        // Cast to Object to call success(Object data) instead of success(String msg)
        AjaxResult result = AjaxResult.success((Object) "hello");
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMsg());
        assertEquals("hello", result.getData());
    }

    @Test
    public void testSuccess_withMsg() {
        AjaxResult result = AjaxResult.success("自定义成功消息");
        assertEquals(200, result.getCode());
        assertEquals("自定义成功消息", result.getMsg());
    }

    @Test
    public void testSuccess_withMsgAndData() {
        AjaxResult result = AjaxResult.success("成功", "data");
        assertEquals(200, result.getCode());
        assertEquals("成功", result.getMsg());
        assertEquals("data", result.getData());
    }

    @Test
    public void testSuccess_withNullData() {
        AjaxResult result = AjaxResult.success("成功", null);
        assertEquals(200, result.getCode());
        assertEquals("成功", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    public void testError_noArgs() {
        AjaxResult result = AjaxResult.error();
        assertEquals(500, result.getCode());
        assertEquals("操作失败", result.getMsg());
    }

    @Test
    public void testError_withMsg() {
        AjaxResult result = AjaxResult.error("出错了");
        assertEquals(500, result.getCode());
        assertEquals("出错了", result.getMsg());
    }

    @Test
    public void testError_withMsgAndData() {
        AjaxResult result = AjaxResult.error("错误", "errorData");
        assertEquals(500, result.getCode());
        assertEquals("错误", result.getMsg());
        assertEquals("errorData", result.getData());
    }

    @Test
    public void testError_withCodeAndMsg() {
        AjaxResult result = AjaxResult.error(404, "未找到");
        assertEquals(404, result.getCode());
        assertEquals("未找到", result.getMsg());
    }

    @Test
    public void testError_withCodeMsgAndData() {
        AjaxResult result = AjaxResult.error(911, "无权限", null);
        assertEquals(911, result.getCode());
        assertEquals("无权限", result.getMsg());
    }

    @Test
    public void testIsSuccess() {
        assertTrue(AjaxResult.success().isSuccess());
        assertFalse(AjaxResult.error().isSuccess());
    }

    @Test
    public void testChainPut() {
        AjaxResult result = AjaxResult.success();
        AjaxResult chained = result.put("extraKey", "extraValue");
        assertSame(result, chained);
        assertEquals("extraValue", chained.get("extraKey"));
    }

    @Test
    public void testGetData_withInteger() {
        AjaxResult result = AjaxResult.success((Object) 123);
        assertEquals(123, result.getData());
    }

    @Test
    public void testGetMsg() {
        AjaxResult result = AjaxResult.error("错误信息");
        assertEquals("错误信息", result.getMsg());
    }

    @Test
    public void testGetCode() {
        assertEquals(200, AjaxResult.success().getCode());
        assertEquals(500, AjaxResult.error().getCode());
    }
}
