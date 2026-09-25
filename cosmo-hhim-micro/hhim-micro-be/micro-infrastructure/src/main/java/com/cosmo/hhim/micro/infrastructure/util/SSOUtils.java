/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
public class SSOUtils {
    /**
     * 读取远程文件并替换文本
     */
    public static void readUrl(String FileName, String orign, String nowStr, BufferedWriter writer) {
        HttpURLConnection urlCon = null;
        try {
            // 白名单校验：仅允许 http/https 协议的常规 URL 字符（白名单策略，防止 file:// 等协议读取本地任意文件及 CRLF 注入）
            if (FileName == null || !FileName.matches("^https?://[a-zA-Z0-9.\\-:/?#\\[\\]@!$&'()*+,;=%_~]+$")) {
                log.error("拒绝访问非 http/https 协议的远程地址: {}", FileName);
                return;
            }
            // 通过 URI 解析并二次校验 scheme，避免直接构造 URL 触发资源注入
            URI uri = URI.create(FileName);
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                log.error("拒绝访问非 http/https 协议的远程地址: {}", FileName);
                return;
            }
            URL url = uri.toURL();
            urlCon = (HttpURLConnection) url.openConnection();
            urlCon.setConnectTimeout(5000);
            urlCon.setReadTimeout(5000);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream()))) {
                String read;
                while ((read = br.readLine()) != null) {
                    if (read.indexOf(";") > 0 && read.indexOf(";") + 1 < read.trim().length()) {
                        read = read.trim();
                        read = read.substring(0, read.length() - 1).replace(";", "；") + read.substring(read.length() - 1);
                    }
                    writer.write(read.replace(orign, nowStr));
                    writer.newLine();
                }
            }
        } catch (Exception e) {
            log.error("读取远程文件并替换文本失败", e);
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
        }
    }
}
