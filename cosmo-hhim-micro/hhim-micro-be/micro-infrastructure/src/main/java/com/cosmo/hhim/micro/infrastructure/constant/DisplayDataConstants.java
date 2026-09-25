/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.constant;

/**
 * @author cosmo-hhim-open Team
 * @description: 演示数据相关的常量
 * @date 2022/11/18 9:13 上午
 */
public class DisplayDataConstants {

    /**
     * redis中演示数据的key
     */
    public static final String DISPLAY_DATA_KEY = "micro:data:show";

    /**
     * 分析首页的接口url
     */
    public static final String INDEX_URL = "/analysis/index";

    /**
     * 记工排行的接口url
     */
    public static final String SUBMIT_RECORD_RANK_URL = "/analysis/submitRecordRank";

    /**
     * 完工产品统计的接口url
     */
    public static final String FINISHED_PRODUCT_STATISTICS_URL = "/analysis/finishedProductStatistics";

    /**
     * 库存排行的接口url
     */
    public static final String STORAGE_RANK_URL = "/analysis/storageRank";

    /**
     * 在制品查询的接口url
     */
    public static final String STORAGE_PRODUCT_LIST_URL = "/storage/product/list";

    /**
     * 在制品明细查询的接口url
     */
    public static final String STORAGE_PRODUCT_PROCESS_LIST_URL = "/storage/condition/list";

    /**
     * 库存变动详情的接口url
     */
    public static final String STORAGE_CHANGE_HISTORY_URL = "/storage/history";

    /**
     * 质量分析 - 总数接口url
     */
    public static final String PRODUCTION_TOTAL_COUNT_URL = "/analysis/showTotalCount";

    /**
     * 生产质量趋势分析的接口url
     */
    public static final String PRODUCTION_QUALITY_TREND_URL = "/analysis/productionQualityTrend";

    /**
     * 生产质量趋势-完工列表的接口url
     */
    public static final String COMPLETED_PRODUCT_INFORMATION_URL = "/submit/completedProductInformation";

    /**
     * 产品维度良品率分析的接口url
     */
    public static final String PASS_RATE_ANALYSIS_BY_PRODUCT_URL = "/analysis/passRateAnalysisByProduct";

    /**
     * 工序维度良品率分析的接口url
     */
    public static final String PASS_RATE_ANALYSIS_BY_PROCESS_URL = "/analysis/passRateAnalysisByProcess";

    /**
     * 员工维度良品率分析的接口url
     */
    public static final String PASS_RATE_ANALYSIS_BY_EMPLOYEE_URL = "/analysis/passRateAnalysisByEmployee";

    /**
     * 产品维度下面某个产品的所有工序良品率分析的接口url
     */
    public static final String PASS_RATE_ANALYSIS_BY_PRODUCT_PROCESS_URL = "/submit/completedProductInformation/product";

    /**
     * 工序维度下面某个工序的所有产品良品率分析的接口url
     */
    public static final String PASS_RATE_ANALYSIS_BY_PROCESS_PRODUCT_URL = "/submit/completedProductInformation/process";

    /**
     * 完工产品按天统计的接口url
     */
    public static final String COMPLETED_PRODUCT_STATISTICS_BY_DAY_URL = "/complete/report/statistics/day";

    /**
     * 完工产品数量统计接口
     */
    public static final String COMPLETED_PRODUCT_STATISTICS_URL = "/complete/report/statistics/product";
}
