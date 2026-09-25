/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.util;

import com.cosmo.hhim.micro.base.domain.entity.common.DataSourceEntity;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 */
@Slf4j
public class DBUtils { 
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("JDBC驱动类未找到！errorMsg:{}", Throwables.getStackTraceAsString(e));
        }
    }


    public static Connection getConnection(DataSourceEntity dataSource) {
        try {
            return DriverManager.getConnection(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
        } catch (SQLException e) {
            log.error("获取JDBC连接失败！errorMsg:{}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    public static void execSqlFileByMysql(InputStream in, DataSourceEntity dataSource) throws Exception {
        Exception error = null;

        //由于使用的是springboot框架,所以直接使用数据源获取连接对象
        Connection conn = DBUtils.getConnection(dataSource);
        if (null == conn) {
            log.error("获取JDBC连接失败！");
            return;
        }

        try {
            //设置不自动提交
            conn.setAutoCommit(false);
            ScriptRunner runner = new ScriptRunner(conn);
            /* 设置不自动提交
             * <p>runner.setAutoCommit(false) - 不自动提交
             * <p>setStopOnError参数作用：遇见错误是否停止；
             * <p>（1）false，遇见错误不会停止，会继续执行，会打印异常信息，并不会抛出异常，当前方法无法捕捉异常无法进行回滚操作，无法保证在一个事务内执行；
             * <p>（2）true，遇见错误会停止执行，打印并抛出异常，捕捉异常，并进行回滚，保证在一个事务内执行；
             */
            runner.setStopOnError(true);
            /* 按照那种方式执行
             * 		方式一：true则获取整个脚本并执行；
             * 		方式二：false则按照自定义的分隔符每行执行；
             */
            runner.setSendFullScript(false);
            //定义命令间的分隔符
            runner.setDelimiter(";");
            runner.setFullLineDelimiter(false);
            //设置是否输出日志，null不输出日志，不设置自动将日志输出到控制台
            runner.setLogWriter(null);
            //如果又多个sql文件，可以写多个runner.runScript(xxx),
            runner.runScript(new InputStreamReader(in, "utf-8"));
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            error = e;
        } finally {
            close(conn);
        }
        if (error != null) {
            throw error;
        }
    }

    public static void execSqlFileByMysql(String s, DataSourceEntity dataSource) throws Exception {
        Exception error = null;
        Connection conn = conn = DBUtils.getConnection(dataSource);
        if (null == conn) {
            log.error("获取JDBC连接失败！");
            return;
        }

        try {
            //设置不自动提交
            conn.setAutoCommit(false);
            ScriptRunner runner = new ScriptRunner(conn);
            /* 设置不自动提交
             * <p>runner.setAutoCommit(false) - 不自动提交
             * <p>setStopOnError参数作用：遇见错误是否停止；
             * <p>（1）false，遇见错误不会停止，会继续执行，会打印异常信息，并不会抛出异常，当前方法无法捕捉异常无法进行回滚操作，无法保证在一个事务内执行；
             * <p>（2）true，遇见错误会停止执行，打印并抛出异常，捕捉异常，并进行回滚，保证在一个事务内执行；
             */
            runner.setStopOnError(true);
            /* 按照那种方式执行
             * 		方式一：true则获取整个脚本并执行；
             * 		方式二：false则按照自定义的分隔符每行执行；
             */
            runner.setSendFullScript(false);
            //定义命令间的分隔符
            runner.setDelimiter(";");
            runner.setFullLineDelimiter(false);
            //设置是否输出日志，null不输出日志，不设置自动将日志输出到控制台
            runner.setLogWriter(null);
            //如果又多个sql文件，可以写多个runner.runScript(xxx),
            runner.runScript(new StringReader(s));
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            error = e;
        } finally {
            close(conn);
        }
        if (error != null) {
            throw error;
        }
    }

    /**
     * 关闭连接（调用方仅传入 Connection；set/st 在历史调用中恒为 null，属死参数，已简化）
     */
    private static void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            log.error("JDBC资源关闭失败！errorMsg:{}", Throwables.getStackTraceAsString(e));
        }
    }
}
