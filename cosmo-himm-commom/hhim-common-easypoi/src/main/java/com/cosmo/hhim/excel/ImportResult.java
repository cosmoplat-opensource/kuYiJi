/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.excel;

import lombok.Data;

import java.util.List;

@Data
public class ImportResult<T> {

    private int success;
    private int fail;
    private List<T> failList;
    private List<String> successData;
}

