/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-12-02
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficialUseParam {

    private boolean clearData;

}
