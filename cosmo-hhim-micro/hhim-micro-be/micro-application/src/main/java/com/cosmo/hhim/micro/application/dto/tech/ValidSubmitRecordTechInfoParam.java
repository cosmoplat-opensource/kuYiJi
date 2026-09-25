/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.tech;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author cosmo-hhim-open Team
 * @description: 校验报工记录标准工艺路线的参数
 * @date 2023/4/27 09:34
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidSubmitRecordTechInfoParam {

    private String productSeq;

    private String processSeq;

    private String preProcessSeq;
}
