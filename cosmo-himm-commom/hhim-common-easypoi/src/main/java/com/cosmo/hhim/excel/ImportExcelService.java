/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.excel;

import java.util.List;

public interface ImportExcelService<T> {

    /**
     * Excel导入，1000条导入一次
     *
     * @param t
     * @return
     */
    ImportResult importExcel(List<T> t);
}

