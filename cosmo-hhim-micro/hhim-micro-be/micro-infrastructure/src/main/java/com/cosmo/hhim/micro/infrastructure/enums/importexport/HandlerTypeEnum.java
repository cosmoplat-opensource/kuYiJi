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
 * @createTime 2023-01-31
 */
@Getter
@AllArgsConstructor
public enum HandlerTypeEnum implements BaseEnum<String> {

    // 内容提示模块-时钟规则
    CLOCK_TIP_UNLIMITED("10", "无限制（eg：可多次提示）"),
    CLOCK_TIP_MORE_PERIOD("20", "时间周期内仅提示n次（eg:一天内仅提示一次）"),
    CLOCK_TIP_MORE_INTERVAL("30", "至少间隔指定时间可多次提示（eg:可多次提示，提示间隔时间至少3小时）"),
    CLOCK_TIP_MORE_PERIOD_INTERVAL("40", "时间周期内，至少间隔指定时间，可多次提示（eg:一天内可多次提示，间隔时间至少3小时）"),

    // 导入模块-任务标识
    IMPORT_TASK_PROCESS_STORAGE_TEMPLATE("micro_process_storage_import_template", "导入模版-工序库存"),
    IMPORT_TASK_FINISHED_STORAGE_TEMPLATE("micro_finished_storage_import_template", "导入模版-产成品库存"),
    IMPORT_TASK_BOM_TEMPLATE("micro_bom_import_template", "导入模版-BOM"),
    IMPORT_TASK_TECHNOLOGY_TEMPLATE("micro_technology_import_template", "导入模版-工艺"),

    IMPORT_TASK_PROCESS_STORAGE_CONFIRM("micro_process_storage_import_confirm", "确认导入-工序库存"),
    IMPORT_TASK_FINISHED_STORAGE_CONFIRM("micro_finished_storage_import_confirm", "确认导入-产成品库存"),
    IMPORT_TASK_BOM_CONFIRM("micro_bom_import_confirm", "确认导入-BOM"),
    IMPORT_TASK_TECHNOLOGY_CONFIRM("micro_technology_import_confirm", "确认导入-工艺"),

    IMPORT_TASK_PROCESS_STORAGE_CANCEL("micro_process_storage_import_cancel", "取消导入-工序库存"),
    IMPORT_TASK_FINISHED_STORAGE_CANCEL("micro_finished_storage_import_cancel", "取消导入-产成品库存"),
    IMPORT_TASK_TECHNOLOGY_CANCEL("micro_technology_import_cancel", "取消导入-工艺"),
    IMPORT_TASK_BOM_CANCEL("micro_bom_import_cancel", "取消导入-BOM");

    private String code;
    private String desc;

    public static HandlerTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        HandlerTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}
