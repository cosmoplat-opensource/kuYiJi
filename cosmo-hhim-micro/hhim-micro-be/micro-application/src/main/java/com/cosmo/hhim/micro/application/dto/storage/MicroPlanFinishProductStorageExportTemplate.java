/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.storage;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/9
 */
@Data
public class MicroPlanFinishProductStorageExportTemplate extends MicroPlanFinishProductStorage {

    /**
     * 导入库存
     */
    @Excel(name = "导入库存", orderNum = "9")
    @Digits(integer = 12, fraction = 4, message = "整数位不能超过{integer}位，小数位不能超过{fraction}位！")
    private BigDecimal storageNum;

    // 备注 
    @Excel(name = "备注", orderNum = "10")
    @Size(max = 100, message = "最长为100字符！")
    private String remark;

    // 导入错误信息 
    @Excel(name = "导入说明（无需填写）", orderNum = "11")
    private String errorMsg;
}
