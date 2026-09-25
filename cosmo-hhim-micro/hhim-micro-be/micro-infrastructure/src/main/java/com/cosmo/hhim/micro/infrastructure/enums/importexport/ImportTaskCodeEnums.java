/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.importexport;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/5
 */
@Getter
@AllArgsConstructor
public enum ImportTaskCodeEnums implements BaseEnum<String> { 

    MICRO_PROCESS_STORAGE_IMPORT("10","micro_process_storage_import", "工序库存-导入任务"),
    MICRO_FINISHED_STORAGE_IMPORT("20", "micro_finished_storage_import", "产成品库存-导入任务"),
    MICRO_BOM_IMPORT("30", "micro_bom_import", "BOM-导入任务"),
    MICRO_TECHNOLOGY_IMPORT("40", "micro_technology_import", "工艺-导入任务");

    private String key;
    private String code;
    private String desc;

    public static ImportTaskCodeEnums getEnumByKey(String key) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.key, key)).findFirst().orElse(null);
    }

    public static ImportTaskCodeEnums getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        ImportTaskCodeEnums e = getEnum(code);
        return e != null ? e.desc : null;
    }
}
