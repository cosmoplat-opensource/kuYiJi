/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.domain;

import com.cosmo.hhim.common.core.annotation.Excel;
import lombok.Data;
import java.io.Serializable;
import java.util.LinkedHashMap;


/**
 * 序列化条码入参
 */
@Data
public class BarcodeSerializeParam implements Serializable {

    @Excel(name = "物料编码")
    private String matCode;//物料编码

    @Excel(name = "批次号")
    private String batchNo;//批次号

    @Excel(name = "供应商编码")
    private String supplierCode;//供应商编码

    @Excel(name = "客编码户")
    private String customerCode;//客编码户

    @Excel(name = "规格")
    private String specifications;//规格

    @Excel(name = "是否解析成功")
    private boolean flag = true;//是否解析成功

    @Excel(name = "原始字符串")
    private String oriString;//原始字符串，没有值的时候说明是合法字符串，有值的时候说明是一个不合法的，原样输出

    private LinkedHashMap<String,Object> barcodeMap;//按照指定的顺序生成的map

}
