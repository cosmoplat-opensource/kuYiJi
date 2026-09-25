/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.base;

import lombok.Data;

import java.io.Serializable;

/**
 * 根据用户使用情况推荐按钮DTO
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroUserRecommendButtonDTO implements Serializable {
    private String operateProcessSeq;
    private String productSeq;
}
