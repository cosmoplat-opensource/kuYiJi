/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.util;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.micro.base.domain.entity.CertificateInfoPC;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import org.springframework.util.StringUtils;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/18
 */
public class CertificateUtil {

    /**
     * 从凭证中提取任务编码
     * @return
     */
    public static String extractTaskCodeFromCertificate() {
        String certificateInfoStr = (String) ThreadContext.get(CommonConstants.CERTIFICATE_INFO);
        if(!StringUtils.hasText(certificateInfoStr)){
            throw new CustomException("请求凭证信息不存在！");
        }
        CertificateInfoPC certificateInfo = JSON.parseObject(certificateInfoStr, CertificateInfoPC.class);
        return certificateInfo.getTaskCode();
    }

}
