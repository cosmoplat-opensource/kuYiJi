/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.base;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroFollowDTO {
    private List<Long> followIds;
    private String followType;
}
