/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.planning;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @description: 订单/工单 操作枚举
 * @classname: OrderOperateEnum
 * @date: 2023/3/10 17:48
 * @author cosmo-hhim-open Team
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum OrderOperateEnum implements BaseEnum<String> {

    /**
     * 关单
     */
    ORDER_OPERATE_CLOSE("CLOSE", "关单"),

    /**
     * 重启
     */
    ORDER_OPERATE_REBOOT("REBOOT", "重启"),

    /**
     * 完工
     */
    ORDER_OPERATE_COMPLETE("COMPLETE", "完工"),

    /**
     * 新增
     */
    ORDER_OPERATE_ADD("ADD", "新增"),

    /**
     * 修改
     */
    ORDER_OPERATE_UPDATE("UPDATE", "修改");


    private String code;

    private String desc;


    public static OrderOperateEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        OrderOperateEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}
