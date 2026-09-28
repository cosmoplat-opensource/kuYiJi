/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.cosmo.hhim.excel;

import lombok.Data;

@Data
public class ExportParam {


    private int firstRow;
    private int lastRow;
    private int firstCol;
    private int lastCol;
    private String[] dataArray;
}
