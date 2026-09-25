/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/23
 */
@Data
public class WaitDealQueryDto {

    // 产品编码或名称 
    private String productSeqOrName;

    // 员工昵称 
    private String nickName;

    // 报工类型(报工类型，1-ku易记， 2-工易派) 
    private Long submitType;

}
