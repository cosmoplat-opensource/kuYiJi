/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.accessauth.request.wrapper;

import com.cosmo.hhim.common.core.constant.MediaType;
import com.cosmo.hhim.common.core.utils.BytesUtil;
import com.cosmo.hhim.common.core.utils.HttpUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description 可以重复读取request流数据的HttpServletRequest的wrapper类
 * @createTime 2022-10-28
 */
public class ReuseHttpServletRequest extends HttpServletRequestWrapper implements ReuseHttpRequest {

    private final HttpServletRequest target;

    private byte[] body;

    private Map<String, String[]> stringMap;

    public ReuseHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.target = request;
        this.body = toBytes(request.getInputStream());
        this.stringMap = toDuplication(request);
    }

    @Override
    public Object getBody() throws Exception {
        if (StringUtils.containsIgnoreCase(target.getContentType(), MediaType.MULTIPART_FORM_DATA)) {
            return target.getParts();
        } else {
            String s = BytesUtil.toString(body);
            if (StringUtils.isBlank(s)) {
                return HttpUtil.encodingParams(HttpUtil.translateParameterMap(stringMap), StandardCharsets.UTF_8.name());
            }
            return s;
        }
    }

    /**
     * 将请求流转为字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private byte[] toBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int n = 0;
        while ((n = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, n);
        }
        return bos.toByteArray();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return stringMap;
    }

    @Override
    public String getParameter(String name) {
        String[] values = stringMap.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    @Override
    public String[] getParameterValues(String name) {
        return stringMap.get(name);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(body);

        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }

}
