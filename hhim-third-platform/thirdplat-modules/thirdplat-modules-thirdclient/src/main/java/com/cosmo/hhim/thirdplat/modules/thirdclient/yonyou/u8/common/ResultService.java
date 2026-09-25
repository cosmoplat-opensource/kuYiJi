/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.common;

import cn.hutool.http.HttpUtil;
import com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.exception.OpenAPIException;
import org.springframework.util.StringUtils;


public class ResultService {
    /**
     * 异步新增查看新增结果
     *
     * @param requestid
     * @return
     * @throws OpenAPIException
     */
    public static String getResult(String requestid) throws OpenAPIException {
        String url = "https://api.yonyouup.com/result?requestid={requestid}";
        url = StringUtils.replace(url, "{requestid}", requestid);

        String resultStr;
        try {
            resultStr = HttpUtil.get(url);

        } catch (Exception e) {
            throw new OpenAPIException(e.getMessage(), e);
        }


        return resultStr;
    }


}
