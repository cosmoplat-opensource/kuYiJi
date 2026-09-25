/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.ai;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * AI 问数 · 已审口径聚合查询 Mapper（人写登记 SQL）
 *
 * <p>口径统一为"已审"（submit_status=0 + 质检数 check_*），与质量趋势页
 * （记工总数/良品率列表/趋势图）完全同源；支撑 SUMMARY×day 与产品良品率组合查询。
 *
 * @author cosmo-hhim-open Team
 */
public interface MicroAiDailyMapper {

    /** 按日聚合（产量=已审合格数）：submitDay/passNum/ngNum/totalNum */
    List<Map<String, Object>> selectDailySummary(@Param("tenantCode") String tenantCode,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate);

    /** 报工汇总（全量口径=已审+未审；"报了多工"语义）：passNum/ngNum/totalNum */
    Map<String, Object> selectSubmitSummaryRaw(@Param("tenantCode") String tenantCode,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    /** 按日报工聚合（全量口径含未审）：submitDay/passNum/ngNum/totalNum */
    List<Map<String, Object>> selectDailySummaryRaw(@Param("tenantCode") String tenantCode,
                                                    @Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);

    /** 未审核报工汇总（已审为空时的话术弥合）：passNum/ngNum/totalNum */
    Map<String, Object> selectPendingSummary(@Param("tenantCode") String tenantCode,
                                             @Param("startDate") String startDate,
                                             @Param("endDate") String endDate);

    /** 记工排名（全量口径=报工数，含未审）：nickName/userName/submitNum */
    List<Map<String, Object>> selectWorkRankRaw(@Param("tenantCode") String tenantCode,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    /**
     * 维度良品率（已审口径+防除零+跨维度可选过滤），三条查询参数签名统一：
     * 目标维度由调用方选择（product / process / employee），另外两个维度作为可选过滤条件，
     * 支撑"法兰盘各工序的良品率""车削工序下各产品的良品率""张三各产品的良品率"这类组合问法。
     */
    List<Map<String, Object>> selectProcessPassRate(@Param("tenantCode") String tenantCode,
                                                    @Param("startDate") String startDate,
                                                    @Param("endDate") String endDate,
                                                    @Param("processNameOrCode") String processNameOrCode,
                                                    @Param("productNameOrCode") String productNameOrCode,
                                                    @Param("employeeName") String employeeName);

    List<Map<String, Object>> selectEmployeePassRate(@Param("tenantCode") String tenantCode,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate,
                                                     @Param("employeeName") String employeeName,
                                                     @Param("productNameOrCode") String productNameOrCode,
                                                     @Param("processNameOrCode") String processNameOrCode);

    List<Map<String, Object>> selectProductPassRate(@Param("tenantCode") String tenantCode,
                                                    @Param("startDate") String startDate,
                                                    @Param("endDate") String endDate,
                                                    @Param("productNameOrCode") String productNameOrCode,
                                                    @Param("processNameOrCode") String processNameOrCode,
                                                    @Param("employeeName") String employeeName);

    /**
     * AI 分析层取数：指标 × 维度 × 期间（已审口径）。
     *
     * <p>返回 groupName/groupKey/passNum/ngNum/totalNum/rows；维度由 dim 决定
     * （process/product/employee/day），另外三个实体槽位是可选过滤 —— 下钻 = 带上父维度过滤再跑一次。
     */
    List<Map<String, Object>> selectMetricByDimPeriod(@Param("tenantCode") String tenantCode,
                                                      @Param("startDate") String startDate,
                                                      @Param("endDate") String endDate,
                                                      @Param("dim") String dim,
                                                      @Param("productNameOrCode") String productNameOrCode,
                                                      @Param("processNameOrCode") String processNameOrCode,
                                                      @Param("employeeName") String employeeName);

    /**
     * AI 分析层取数：不良类型 × 期间（源=质检记录 micro_quality_control_record）。
     *
     * <p>质检开关未开启时结果为空，上层须如实告知"无不良类型数据"，不得当成 0 不良。
     */
    List<Map<String, Object>> selectNgTypeByPeriod(@Param("tenantCode") String tenantCode,
                                                   @Param("startDate") String startDate,
                                                   @Param("endDate") String endDate,
                                                   @Param("productNameOrCode") String productNameOrCode,
                                                   @Param("processNameOrCode") String processNameOrCode);

    /**
     * 表结构元数据（information_schema）：供 LLM 生成 SQL 时构造 schema 摘要。
     *
     * <p>只接受白名单表；敏感表/敏感列的过滤由 Java 侧 safety 策略完成（此处不裁剪，便于复用与单测）。
     */
    List<Map<String, Object>> selectTableColumns(@Param("tables") java.util.List<String> tables);

    /**
     * 执行"已过 SqlGuard 闸"的生成 SQL（statementType=STATEMENT，5 秒超时）。
     *
     * <p>⚠️ 只允许被生成 SQL 通道调用；SQL 进入此处之前必须已完成：语句类型/白名单/敏感列校验
     * + 租户条件注入 + LIMIT 改写。除此之外全仓不得使用 ${} 拼接。
     */
    List<Map<String, Object>> execGuardedSql(@Param("sql") String sql);

    /** 生成 SQL 审计落库 */
    /**
     * 生成 SQL 审计落库。
     *
     * <p>必须用实体传参：MybatisInterceptor 会反射给参数对象注入基础字段，Map 会抛 NPE（日志噪音）。
     */
    int insertSqlAudit(com.cosmo.hhim.micro.base.domain.entity.ai.MicroAiSqlAudit audit);

    /** 工序在制品（车间在制余额）按产品汇总：与车间库存页面同源 */
    java.util.List<java.util.Map<String, Object>> selectProcessStockByProduct(@org.apache.ibatis.annotations.Param("tenantCode") String tenantCode);

    /* ---------- 业务数据图（实例级：真实业务记录 + 真实发生的关系） ---------- */

    java.util.List<java.util.Map<String, Object>> selectDataGraphProducts(
            @org.apache.ibatis.annotations.Param("tenantCode") String tenantCode,
            @org.apache.ibatis.annotations.Param("startDate") String startDate);

    java.util.List<java.util.Map<String, Object>> selectDataGraphProcesses(
            @org.apache.ibatis.annotations.Param("tenantCode") String tenantCode,
            @org.apache.ibatis.annotations.Param("startDate") String startDate);

    java.util.List<java.util.Map<String, Object>> selectDataGraphEmployees(
            @org.apache.ibatis.annotations.Param("tenantCode") String tenantCode,
            @org.apache.ibatis.annotations.Param("startDate") String startDate);

    java.util.List<java.util.Map<String, Object>> selectDataGraphProductProcess(
            @org.apache.ibatis.annotations.Param("tenantCode") String tenantCode,
            @org.apache.ibatis.annotations.Param("startDate") String startDate);

    java.util.List<java.util.Map<String, Object>> selectDataGraphEmployeeProcess(
            @org.apache.ibatis.annotations.Param("tenantCode") String tenantCode,
            @org.apache.ibatis.annotations.Param("startDate") String startDate);

    /** 数据图补充统计：字典规模 + 各环节数据量（一条 SQL 出全部，避免多次往返） */
    java.util.Map<String, Object> selectDataGraphCounts(
            @org.apache.ibatis.annotations.Param("tenantCode") String tenantCode);

    /** 最近一次报工日（空数据引导用）：返回 submitDay 字符串 or null */
    String selectLatestSubmitDay(@Param("tenantCode") String tenantCode);

    /** 成品库存（租户隔离）：finishedNum 总数量 / productCnt 涉及产品数 */
    Map<String, Object> selectFinishedStock(@Param("tenantCode") String tenantCode);

    /** 产品维度成品库存排行（租户隔离、同 selectFinishedStock 口径 num>0）：productName/productSeq/finishedNum */
    List<Map<String, Object>> selectFinishedStockByProduct(@Param("tenantCode") String tenantCode);

    /** 近 30 天日均产能（已审口径，延期预测用）：= sum(check_pass+ng)/30 */
    java.math.BigDecimal selectAvgDailyCapacity(@Param("tenantCode") String tenantCode);

    /** 订单延期预警（登记 SQL）：未完成订单（plan_num>finish_num，状态 10/20/30）按"预计完成日>交付日"筛出延期清单 */
    List<Map<String, Object>> selectDeliveryRisk(@Param("tenantCode") String tenantCode,
                                                 @Param("dailyCap") java.math.BigDecimal dailyCap);

    /** 近期交付订单（未延期且 7 天内交付）："哪些订单快交付了"语义 */
    List<Map<String, Object>> selectDeliverySoon(@Param("tenantCode") String tenantCode,
                                                 @Param("dailyCap") java.math.BigDecimal dailyCap);
}

