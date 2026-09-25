/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.excel;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * excel导入处理接口
 */
public interface IExcelImportService {

     /**
      * 业务数据操作
      * @param list
      * @return
      */
     int importData(List<?> list);
}
