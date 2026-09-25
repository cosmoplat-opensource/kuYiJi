/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.config;


import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
@Component
public class TabelOfNewProcessor {


    /**
     * 遵循最小匹配原则，过滤衍生
     */
    public static String changeTabelName(String sql, String schema) throws Exception {
//        List<String> tableNames = DataPermissionSqlUtil.getTableNames(sql);
//        for (String table : tableNames) {
//            //处理${entity.schema}
//            sql = sql.replace(table, schema + "." + table + space);
//        }

        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        TableAddSchemaVisitor visitor = new TableAddSchemaVisitor(schema);
        for (SQLStatement stmt : stmtList) {
            stmt.accept(visitor);
        }
        return SQLUtils.toSQLString(stmtList, JdbcConstants.MYSQL);
    }

//    public static void main(String[] args) throws Exception {
//
//        String a = "SELECT count(0) FROM cim_process_common c" +
//                "ommon WHERE ACTIVE_FLAG = ? AND (PROCESS_CODE " +
//                "IN (SELECT PROCESS_CODE FROM cim_process_common_station WHERE WKSD_CODE = ?) OR PROCESS_CODE NOT IN (SELECT PROCESS_CODE FROM cim_process_common_station)) AND common.tenant_code = '000013'";
//        String tableNames = TabelOfNewProcessor.changeTabelName(a, "`hhim-portal-test`");
//        System.out.println(tableNames);
//    }
}
