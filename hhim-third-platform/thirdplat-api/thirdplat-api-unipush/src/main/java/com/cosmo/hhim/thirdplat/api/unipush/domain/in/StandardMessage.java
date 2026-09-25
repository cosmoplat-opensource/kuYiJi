/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-30
 */
@Data
public class StandardMessage extends Message {

    /**
     * 推送目标用户列表
     */
    @NotEmpty(message = "推送目标用户列表不允许为空")
    private List<String> toUsernames;
}
