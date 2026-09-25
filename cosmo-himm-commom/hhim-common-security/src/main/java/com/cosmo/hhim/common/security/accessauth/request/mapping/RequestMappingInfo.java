/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.accessauth.request.mapping;

import lombok.Data;

import java.util.Comparator;

/**
 * @author cosmo-hhim-open Team
 * @description request请求路径、请求参数映射信息
 * @createTime 2022-10-28
 */
@Data
public class RequestMappingInfo {
    private ParamRequestCondition paramRequestCondition;

    public static class RequestMappingInfoComparator implements Comparator<RequestMappingInfo> {

        @Override
        public int compare(RequestMappingInfo o1, RequestMappingInfo o2) {
            return Integer.compare(o2.getParamRequestCondition().getExpressions().size(),
                    o1.getParamRequestCondition().getExpressions().size());
        }
    }
}
