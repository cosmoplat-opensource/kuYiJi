/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.ai;

import lombok.Data;

import java.util.Date;

/**
 * AI 生成 SQL 审计表 micro_ai_sql_audit
 *
 * <p>通道 C（LLM 生成 SQL 兜底）的完整留痕：谁在何时问了什么、生成/最终 SQL、安全闸结论、
 * 执行结果与耗时，用于事后审计与问题追溯。
 *
 * <p>**必须以实体（Bean）形式传参，不能用 Map**：项目的 {@code MybatisInterceptor} 会反射给参数对象
 * 注入 created_by/created_date 等基础字段，传 Map 会抛 NPE（只打 WARN、落库仍成功，但日志被污染）。
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroAiSqlAudit {

    private Long id;

    private String tenantCode;

    private String sessionId;

    /** 关联消息ID（可空：生成阶段失败时还没有消息） */
    private String messageId;

    private Long userId;

    private String question;

    /** 问题指纹（md5，用于统计同一问题的生成情况） */
    private String questionHash;

    /** 当时识别的意图编码 */
    private String intent;

    /** 生成 SQL 的用途说明（取自模型输出） */
    private String purpose;

    /** 模型原始输出 */
    private String sqlText;

    /** 过闸并改写后真正执行的 SQL（注入租户、加 LIMIT） */
    private String sqlFinal;

    /** 安全闸是否通过：1/0 */
    private String guardOk;

    /** 拒绝原因（guardOk=0 时） */
    private String rejectReason;

    /** 涉及的表（JSON 数组字符串） */
    private String tablesJson;

    /** 涉及的列（JSON 数组字符串） */
    private String columnsJson;

    /** 是否注入了租户条件：1/0 */
    private String tenantInjected;

    /** 是否改写了 LIMIT：1/0 */
    private String limitRewritten;

    /** 重试次数（把闸拒绝原因回喂给模型后的重生成次数） */
    private Integer retryCount;

    /** 执行状态：SUCCESS / REJECTED / FAILED */
    private String execStatus;

    /** 执行错误（截断） */
    private String execError;

    private Integer rowCount;

    private Long elapsedMs;

    /** 使用的模型标识 */
    private String model;

    private String createdBy;

    private Date createdDate;
}
