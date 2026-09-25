/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.submit;

import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 批量报工或者送检的前端传递实体
 * @date 2023/4/14 15:28
 */
@Data
public class BatchSubmitOrQcDto {

    /**
     * 批量报工 ： 1L
     * 批量送检 ： 2L
     */
    private Long submitWay;

    private List<MicroWorkSubmitMultiMixedDTO> multiMixedList;
}
