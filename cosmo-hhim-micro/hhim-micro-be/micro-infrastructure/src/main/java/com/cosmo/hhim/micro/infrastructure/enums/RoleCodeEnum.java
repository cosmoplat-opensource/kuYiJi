/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums;

import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-22
 */
@Getter
@AllArgsConstructor
public enum RoleCodeEnum implements BaseEnum<String> { 

    MANAGER(MicroBusinessConstants.MANAGER_ROLE, "企业管理员"),
    AUDITOR(MicroBusinessConstants.AUDITOR_ROLE, "审产员/计划员"),
    WORKER(MicroBusinessConstants.WORKER_ROLE, "员工"),
    QUALITY_INSPECTOR(MicroBusinessConstants.QUALITY_INSPECTOR, "质检员");

    private String code;
    private String desc;

    public static RoleCodeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }
}
