/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.config;

import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGASTVisitorAdapter;

/**
 * 继承不同数据库类型的ASTVisitorAdapter 重写visit方法 即可修改表名
 */
public class TableAddSchemaVisitor extends MySqlASTVisitorAdapter {

    //默认是没有自定义变量的 可以自己加
    private String schema;

    public String getSchema() {
        return schema;
    }

    public TableAddSchemaVisitor() {
    }

    public TableAddSchemaVisitor(String schema) {
        this.schema = schema;
    }

    @Override
    public boolean visit(SQLExprTableSource x) {
        String name = x.getExpr().toString();
        x.setExpr(schema + "." + name);
        return true;
    }
}