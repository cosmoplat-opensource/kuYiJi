/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.integration;

import com.cosmo.hhim.micro.application.dto.integration.MicroNoticeConfigDetailInfoResult;
import com.cosmo.hhim.micro.application.dto.integration.MicroNoticeConfigEditParam;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/6/8
 */
public interface IMicroNoticeConfigFacadeService {

    /**
     * 根据业务标识查询通知配置
     * @param businessSign
     * @return
     */
    MicroNoticeConfigDetailInfoResult selectMicroNoticeConfigByBusinessSign(NotifyEnums.NoticeBusinessSignEnum businessSign);


    /**
     * 更新通知配置
     * @param editParam
     */
    void updateMicroNoticeConfig(MicroNoticeConfigEditParam editParam);

}
