-- ============================================================
-- Ku易记 · AI 问数会话存储 建表脚本（im_micro 库）
-- 说明：与问数本体/SDK 解耦，可先行落地；表规范沿用项目 micro_* 惯例
-- 接口契约对应：会话 5 接口 + POST /ai/ask（见《问一问接口契约》）
-- ============================================================

-- 1) 会话表
CREATE TABLE IF NOT EXISTS `micro_ai_chat_session` (
  `id`            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id`    varchar(36)  NOT NULL COMMENT '会话唯一ID（服务端生成）',
  `tenant_code`   varchar(20)  NOT NULL COMMENT '租户编码',
  `user_id`       bigint       NOT NULL COMMENT '所属用户ID',
  `title`         varchar(64)  NOT NULL DEFAULT '新会话' COMMENT '会话名称（首问截断 或 用户改名）',
  `msg_count`     int          NOT NULL DEFAULT '0' COMMENT '消息条数（上限=200，服务端写入时校验）',
  `status`        varchar(2)   NOT NULL DEFAULT '10' COMMENT '状态：10进行中 20已满 30已结束',
  `last_msg_time` datetime     DEFAULT NULL COMMENT '最后消息时间（列表排序/展示用）',
  `created_by`    varchar(40)  NOT NULL COMMENT '创建人',
  `created_date`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by`   varchar(40)  NOT NULL COMMENT '最后修改人',
  `last_upd_date` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `active_flag`   varchar(1)   DEFAULT '1' COMMENT '激活标记1是0否',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_tenant_user_time` (`tenant_code`,`user_id`,`last_msg_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI问数会话表';

-- 2) 消息表
CREATE TABLE IF NOT EXISTS `micro_ai_chat_message` (
  `id`            bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id`    varchar(36)  NOT NULL COMMENT '会话唯一ID（关联 micro_ai_chat_session.session_id）',
  `tenant_code`   varchar(20)  NOT NULL COMMENT '租户编码',
  `role`          varchar(10)  NOT NULL COMMENT '消息角色：user / ai / clarify',
  `content`       text         NOT NULL COMMENT '消息文本',
  `route_json`    json         DEFAULT NULL COMMENT '答案卡片-图表跳转（查看图表按钮）',
  `evidence_json` json         DEFAULT NULL COMMENT '答案卡片-三级血缘依据（指标口径/数据来源/结果快照）',
  `clarify_json`  json         DEFAULT NULL COMMENT '答案卡片-澄清选项 chips',
  `intent`        varchar(1024) DEFAULT NULL COMMENT 'AI意图（槽位继承：语义字段 JSON；旧数据为指标 code）',
  `elapsed_ms`    int          DEFAULT NULL COMMENT '本次问答耗时毫秒（ai消息）',
  `msg_time`      datetime     NOT NULL COMMENT '消息时间（服务端记录，前端时间戳展示来源）',
  `created_by`    varchar(40)  NOT NULL COMMENT '创建人',
  `created_date`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_time` (`session_id`,`msg_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI问数消息表';

-- 3) 建议补的清理策略（可后续用定时任务/RocketMQ 实现，此处仅注释）
-- - 会话 TTL：90 天未活跃的会话与消息，批量删除（按 last_msg_time 分页处理）
-- - 上限：msg_count >= 200 时置 status='20'，新提问时拒绝并提示开启新会话（与前端一致）
