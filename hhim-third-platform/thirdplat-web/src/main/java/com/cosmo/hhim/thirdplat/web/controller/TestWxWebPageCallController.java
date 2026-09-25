/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-11
 */
@Slf4j
@RestController
@RequestMapping("/webpage/callback")
public class TestWxWebPageCallController {

    @GetMapping("/hello")
    public APIResponse<String> getWebPageAuthorizeUrl(@RequestParam("code") String code, @RequestParam(value = "state", required = false) String state) {
        // 回显参数使用 OWASP Encoder 做 HTML 上下文输出编码（扫描报告推荐 API），并去除换行，防止反射型 XSS
        String param = "code:" + Encode.forHtml(removeCrlf(code)) + ", state:" + Encode.forHtml(removeCrlf(state));
        return APIResponse.success("callback param:" + param);
    }

    /**
     * 去除 CR/LF 控制字符（配合 OWASP Encode.forHtml 输出编码，防止 CRLF 注入与反射型 XSS）
     */
    private static String removeCrlf(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\r", "").replace("\n", "");
    }

}
