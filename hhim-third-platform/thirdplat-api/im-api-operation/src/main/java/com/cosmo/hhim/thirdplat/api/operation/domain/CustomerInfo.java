/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.cosmo.hhim.common.core.web.page.PageDomain;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-11-01
 */
@Data
public class CustomerInfo extends PageDomain {
    private String customerName;
}
