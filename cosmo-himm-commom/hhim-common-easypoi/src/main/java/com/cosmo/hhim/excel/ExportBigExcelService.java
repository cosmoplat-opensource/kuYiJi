/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.excel;

import java.util.List;

public interface ExportBigExcelService<T,R> {

    public List<R> exportQuery (T t);
}
