/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils.utils;


import com.cosmo.hhim.common.core.utils.reflect.ReflectUtils;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;

public class DataPermissionSqlUtil {
    private static CCJSqlParserManager pm = new CCJSqlParserManager();
    private static Logger logger = LoggerFactory.getLogger(ReflectUtils.class);


    /**
     * detect table names from given table
     * ATTENTION : WE WILL SKIP SCALAR SUBQUERY IN PROJECTION CLAUSE
     */
    public static List<String> getTableNames(String sql) throws Exception {
        try {
            List<String> tablenames = null;
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            Statement statement = pm.parse(new StringReader(sql));
            if (statement instanceof Select) {
                tablenames = tablesNamesFinder.getTableList((Select) statement);
            } else if (statement instanceof Update) {
                return tablenames = tablesNamesFinder.getTableList((Update) statement);
            } else if (statement instanceof Delete) {
                return tablenames = tablesNamesFinder.getTableList((Delete) statement);
            } else if (statement instanceof Replace) {
                return tablenames = tablesNamesFinder.getTableList((Replace) statement);
            } else if (statement instanceof Insert) {
                return tablenames = tablesNamesFinder.getTableList((Insert) statement);
            }
            return tablenames;
        } catch (Exception e) {
            logger.error("sql解析异常：{}", sql, e);
        }
        throw new RuntimeException("sql解析异常");
    }

    public static void main(String[] args) throws Exception {
        getTableNames("");
    }


}
