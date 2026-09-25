/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cosmo-hhim-open Team
 * 常量类
 */
public final class CommonConstants {
    private static final Set<String> ACTIVE_ENV_SET_INTERNAL = new HashSet<>(Arrays.asList("test", "prod"));
    public static final Set<String> ACTIVE_ENV_SET = Collections.unmodifiableSet(ACTIVE_ENV_SET_INTERNAL);

    /**
     * 热点KEY前缀
     */
    public static final String HOT_KEY = "micro:hot_key:";
    /**
     * 报警指标
     */
    public static final String WARNING_KEY = "micro:warning_metrics:";
    /**
     * 分隔符
     */
    public static final String SEPARATOR_SYMBOL = ":";
    /**
     * 通配符
     */
    public static final String MATCH_SYMBOL = "*";
    /**
     * 前工序热点KEY
     */
    public static final String HOT_KEY_PRE_PROCESS = "micro:hot_key:pre_process:";

    /**
     * 操作工序热点KEY
     */
    public static final String HOT_KEY_OPERATE_PROCESS = "micro:hot_key:operate_process:";
    /**
     * 高频产品KEY
     */
    public static final String HOT_KEY_HIGH_FREQUENCY_PRODUCT = "micro:hot_key:high_frequency_product:";
    /**
     * 高频tag（Redis key 前缀，值保持兼容历史缓存数据）
     */
    public static final String HOT_TAG_STORAGE_CHANGE = "micro:hot_key:tag:storage_change:";
    /**
     * 次数
     */
    public static final String HOT_KEY_SUFFIX_TIMES = "times:";
    /**
     * 时间序列
     */
    public static final String HOT_KEY_SUFFIX_SEQ = "seq:";
    /**
     * 产品流水号
     */
    public static final String SEQ_NUMBER_KEY_PRODUCT = "micro:seq_key:product:";
    /**
     * 工序流水号前缀
     */
    public static final String SEQ_NUMBER_KEY_PROCESS = "micro:seq_key:process:";
    /**
     * 工序流水号前缀
     */
    public static final String SUBMIT_TIMES = "micro:submit_times:";
    /**
     * 用户缓存
     */
    public static final String USER_CACHE_INFO = "micro:user_info:";
    /**
     * 工序新增默认前缀
     */
    public static final String PROCESS_GENERATE_PREFIX = "GX";
    /**
     * 产品新增默认前缀
     */
    public static final String PRODUCT_GENERATE_PREFIX = "P";

    /**
     * 生产订单编号前缀
     */
    public static final String MANUFACTURE_ORDER_GENERATE_PREFIX = "SC";

    /**
     * 生产工单编号前缀
     */
    public static final String MANUFACTURE_WORK_ORDER_GENERATE_PREFIX = "MO";

    /**
     * 客户编号前缀
     */
    public static final String CUSTOMER_GENERATE_PREFIX = "CSR";

    /**
     * 车间编号前缀
     */
    public static final String MANUFACTURE_WORKSHOP_GENERATE_PREFIX = "CJ";

    /**
     * 产线编号前缀
     */
    public static final String MANUFACTURE_LINE_GENERATE_PREFIX = "CX";

    /**
     * 产线编号前缀
     */
    public static final String CUSTOM_FIELD_GENERATE_PREFIX = "ext";


    /**
     * 库存变动节点(报工入库)
     */
    public static final String STORAGE_SUBMIT_INBOUND = "报工入库";
    /**
     * 库存变动节点(库存变动)
     */
    public static final String STORAGE_MANUAL_MODIFY = "库存变动";
    /**
     * 库存变动节点(工序流转)
     */
    public static final String STORAGE_PROCESS_FLOW_MODIFY = "工序流转";
    /**
     * 库存变动节点(不良品返修)
     */
    public static final String STORAGE_REPAIR_QUALITY_CONTROL = "不良品返修";

    public static final String STORAGE_PROCESS_IMPORT = "库存导入";
    public static final String STORAGE_PROCESS_COMPLETE_INBOUND = "完工入库";
    public static final String STORAGE_PROCESS_COMPLETE_OUTBOUND = "完工撤销";

    /**
     * 库存变动节点(库存回退)
     */
    public static final String STORAGE_UNDO_FALLBACK = "审核撤销";

    /**
     * 库存变动节点(质检判异)
     */
    public static final String STORAGE_QC_UNDO_FALLBACK = "质检判异";

    /**
     * 在制品库存微应用标识
     */
    public static final String MICRO_PROCESS_APPLICATION = "micro_process";

    /**
     * 库存变动类型 - 报工 - 增加
     */
    public static final String STORAGE_CHANGE_TYPE_SUBMIT = "0";

    /**
     * 库存变动类型 - 冲销 - 减少
     */
    public static final String STORAGE_CHANGE_TYPE_ELIMINATION = "1";

    /**
     * 库存变动类型 - 不良品返修 - 增加
     */
    public static final String STORAGE_CHANGE_TYPE_REPAIR = "2";

    /**
     * 库存变动类型 - 质检判异 - 减少
     */
    public static final String STORAGE_CHANGE_TYPE_QC_ELIMINATION = "3";

    /* ******************** *
     *  报工历史记录变动操作原因 *
     * ******************** */

    /**
     * 报工历史记录变动节点(记工)
     */
    public static final String SUBMIT_HISTORY_RECORDS_ADD = "生产记工";

    /**
     * 报工历史记录变动节点(编辑)
     */
    public static final String SUBMIT_HISTORY_RECORDS_EDIT = "记工编辑";

    /**
     * 报工历史记录变动节点(审核)
     */
    public static final String SUBMIT_HISTORY_RECORDS_CHECK = "记工审核";

    /**
     * 报工历史记录变动节点(审核撤销)
     */
    public static final String SUBMIT_HISTORY_RECORDS_UNDO_CHECK = "审核撤销";

    /**
     * 报工历史记录变动节点(质检判异撤销)
     */
    public static final String SUBMIT_HISTORY_RECORDS_QC_UNDO_CHECK = "质检判异撤销";

    /**
     * 报工历史记录变动节点(数据治理)
     */
    public static final String SUBMIT_HISTORY_RECORDS_DATA_GOVERNANCE = "数据治理";

    /**
     * 报工历史记录变动节点(审核驳回)
     */
    public static final String SUBMIT_HISTORY_RECORDS_REJECT = "审核驳回";

    /**
     * 报工历史记录变动节点(完工入库)
     */
    public static final String SUBMIT_HISTORY_INBOUND_COMPLETE = "完工入库";

    /**
     * 报工历史记录变动节点(完工撤销)
     */
    public static final String SUBMIT_HISTORY_CANCEL_COMPLETE = "完工撤销";

    /**
     * 报工历史记录变动节点(质检)
     */
    public static final String SUBMIT_HISTORY_QUALITY_CONTROL = "质检";

    /**
     * 报工历史记录变动节点(返修复核)
     */
    public static final String SUBMIT_HISTORY_REPAIR = "不良返修";

    /**
     * 报工历史记录变动节点(结算)
     */
    public static final String SUBMIT_HISTORY_SETTLED = "记工结算";

    /* ******************** *
     *  数据治理-工序异常类型   *
     * ******************** */
    /**
     * 无首序异常
     */
    public static final String NOT_HAVE_FIRST_PROCESS_WARN_TYPE = "1";

    /**
     * 无尾序异常
     */
    public static final String NOT_HAVE_LAST_PROCESS_WARN_TYPE = "2";

    /**
     * 多尾序异常
     */
    public static final String MULTI_LAST_PROCESS_WARN_TYPE = "3";

    /**
     * 标示位为0 - YES
     */
    public static final String YES = "0";

/**
     * 标示位为1 - NO
     */
    public static final String NO = "1";

    /**
     * 根工序标识（无父工序）
     */
    public static final String ROOT_PROCESS_SEQ = "0";

    // 设备标识
    public static final String DETAILS_TYPE = "wechatMiniApp";
    public static final String STANDARD_KEY = "standard";
    public static final String TECH_DATA_VALUE = "techData";

 
    // 请求临时凭证 
    public static final String CERTIFICATE = "certificate";
    public static final String CERTIFICATE_INFO = "certificateInfo";
    /**
     * 微应用app_sign与app_code映射关系redis_key
     */
    public static final String REDIS_APP_SIGN_CODE_MAPPING_KEY = "micro:app_sign_code_mapping";
    public static final String REDIS_KEY_MICRO_PREFIX = "micro:";
    /**
     * 微应用数据库schema名称
     */
    public static final String MICRO_SCHEMA = "`im_micro`";

 
    // 导入业务标识 
    public static final String IMPORT_TEMPLATE = "_template";
    public static final String IMPORT_CONFIRM = "_confirm";
    public static final String IMPORT_CANCEL = "_cancel";
}