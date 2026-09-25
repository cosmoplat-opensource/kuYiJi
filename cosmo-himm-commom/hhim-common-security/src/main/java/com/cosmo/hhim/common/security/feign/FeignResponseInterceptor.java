/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.feign;

import com.alibaba.fastjson.JSON;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;
import org.springframework.stereotype.Component;

/**
 * feign 返回拦截-解码器
 *
 * @author cosmo-hhim-open Team
 */
public class FeignResponseInterceptor implements Decoder {


  @Override
  public Object decode(Response response, Type type)
      throws IOException, DecodeException, FeignException {
    if (response.body() == null) {
      return null;
    }
    if (response.status() == 404 || response.status() == 204) {
      return Util.emptyValueOf(type);
    }
    if (byte[].class.equals(type)) {
      return Util.toByteArray(response.body().asInputStream());
    }
    String str = Util.toString(response.body().asReader(Util.UTF_8));
    return JSON.parseObject(str, type);
  }
}