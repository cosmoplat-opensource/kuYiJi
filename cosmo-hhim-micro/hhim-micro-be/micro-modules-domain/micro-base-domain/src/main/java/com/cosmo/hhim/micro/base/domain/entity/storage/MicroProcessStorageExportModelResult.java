/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.storage;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/4/11
 */
@Data
public class MicroProcessStorageExportModelResult {

    // 产品编码 
    @Excel(name = "产品编码", orderNum = "1")
    private String productCode;

    // 产品名称 
    @Excel(name = "产品名称", orderNum = "2")
    private String productName;

    // 工序编码 
    @Excel(name = "工序编码", orderNum = "3")
    private String processCode;

    // 工序名称 
    @Excel(name = "工序名称", orderNum = "4")
    private String processName;

    // 原库存良品数 
    @Excel(name = "良品数", orderNum = "5", groupName = "当前库存数量")
    private BigDecimal passNum;

    // 原库存不良品数 
    @Excel(name = "不良品数", orderNum = "6", groupName = "当前库存数量")
    private BigDecimal ngNum;

    // 盘点良品数 
    @Excel(name = "良品数", orderNum = "7", groupName = "调整导入数量")
    private BigDecimal adjustPassNum;

    // 盘点不良品数 
    @Excel(name = "不良品数", orderNum = "8", groupName = "调整导入数量")
    private BigDecimal adjustNgNum;

    // 盘点备注 
    @Excel(name = "备注", orderNum = "9")
    private String adjustRemark;

    // 导入错误信息 
    @Excel(name = "导入说明（无需填写）", orderNum = "10")
    private String errorMsg;

}
