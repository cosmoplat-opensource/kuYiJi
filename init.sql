-- ============================================================
-- cosmo-hhim micro_* 业务表初始化脚本（共 60 张表）
-- Docker 部署：由 mysql 镜像 /docker-entrypoint-initdb.d/ 自动执行；
-- 手工导入（Navicat 等）：脚本自带建库语句，无需手动创建数据库
-- ============================================================
CREATE DATABASE IF NOT EXISTS `im_micro` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `im_micro`;

/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE TABLE IF NOT EXISTS `async_import_export_task` (
  `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务id',
  `sys_code` varchar(50) DEFAULT NULL COMMENT '系统编码',
  `mq_keys` varchar(100) DEFAULT NULL COMMENT 'mqkeys',
  `task_name` varchar(255) DEFAULT NULL COMMENT '任务名称',
  `tenant_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户名',
  `req_param` varchar(500) DEFAULT NULL COMMENT '请求参数',
  `task_status` tinyint DEFAULT '0' COMMENT '任务状态(0初始化 1执行中 2成功  3失败 4已确认 5已撤销)',
  `url` varchar(255) DEFAULT NULL COMMENT '下载地址',
  `task_code` varchar(255) DEFAULT NULL COMMENT '任务编码 bean',
  `task_type` tinyint DEFAULT NULL COMMENT '任务类型1：上传（导入）2：下载（导出）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `deal_time` int DEFAULT NULL COMMENT '处理时间(毫秒)',
  `exception_msg` text COMMENT '错误描述',
  `msg` varchar(255) DEFAULT NULL COMMENT '导入msg',
  `success_data` varchar(1000) DEFAULT NULL COMMENT '导入成功的数据',
  PRIMARY KEY (`task_id`) USING BTREE,
  UNIQUE KEY `idx_mq_keys` (`mq_keys`)
) ENGINE=InnoDB AUTO_INCREMENT=706 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='异步导入导出任务表';

 

CREATE TABLE IF NOT EXISTS `micro_app_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_code` varchar(64) DEFAULT NULL,
  `app_sign` varchar(64) DEFAULT NULL,
  `customer_product_type` int DEFAULT NULL,
  `config_json` text,
  `status` tinyint DEFAULT '1',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_app_code` (`app_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用配置';

 

CREATE TABLE IF NOT EXISTS `micro_complete_report` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `report_no` varchar(32) NOT NULL COMMENT '完工报告单号',
  `product_seq` varchar(32) NOT NULL COMMENT '产品唯一码',
  `product_name` varchar(32) DEFAULT NULL COMMENT '产品名称',
  `complete_num` decimal(20,4) NOT NULL COMMENT '完工数量',
  `state` char(1) NOT NULL COMMENT '完工单状态（0:正常，1:撤销，2:已结算）',
  `work_submit_id` bigint NOT NULL COMMENT '报工记录ID（尾序）/生产工单ID',
  `complete_time` datetime NOT NULL COMMENT '完工时间',
  `complete_user` bigint NOT NULL COMMENT '完工操作人ID',
  `source_channel` varchar(16) NOT NULL COMMENT '来源渠道',
  `active_flag` char(1) DEFAULT NULL COMMENT '可用标识（0：正常，1：停用）',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_completeUser_sourceChannel` (`complete_user`,`source_channel`) USING BTREE,
  KEY `idx_productSeq_sourceChannel` (`product_seq`,`source_channel`) USING BTREE,
  KEY `idx_reportNo` (`report_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1361 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='完工报告单';

 

CREATE TABLE IF NOT EXISTS `micro_content_tip_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `trigger_roles` varchar(64) NOT NULL COMMENT '触发角色条件集合(角色编码是micro_role表的role_code，多个用,分割)',
  `trigger_actions` varchar(128) NOT NULL COMMENT '触发动作条件集合(TipTriggerActionEnum，多个用,分割',
  `action_type` varchar(8) DEFAULT NULL COMMENT '动作类型(ActionTypeEnums)',
  `trigger_business_bean` varchar(128) DEFAULT NULL COMMENT '触发业务条件beanName',
  `business_condition_config` text COMMENT '业务条件参数配置（格式：key:value，多个用,分割）',
  `priority` bigint unsigned NOT NULL COMMENT '触发优先级',
  `tip_content` varchar(256) NOT NULL COMMENT '提示内容（使用{0}{1}作为占位符）',
  `tip_way` varchar(8) NOT NULL DEFAULT '10' COMMENT '提示方式-供前端显示标识（10:状态栏显示，20:弹窗显示）',
  `period_type` varchar(16) NOT NULL COMMENT '周期类型(ClockTipHandlerTypeEnum)',
  `period_time` bigint DEFAULT NULL COMMENT '周期时间',
  `period_unit` varchar(64) DEFAULT NULL COMMENT '周期时间单位(PeriodTimeUnitEnum)',
  `interval_time` bigint DEFAULT NULL COMMENT '间隔时长',
  `interval_unit` varchar(64) DEFAULT NULL COMMENT '间隔时长单位(PeriodTimeUnitEnum)',
  `period_max_num` bigint DEFAULT NULL COMMENT '周期内触发次数上限',
  `active_flag` char(1) DEFAULT NULL COMMENT '可用标识（0：正常，1：停用）',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容提示配置表';

 

CREATE TABLE IF NOT EXISTS `micro_content_tip_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `user_type` varchar(10) NOT NULL DEFAULT '10' COMMENT '用户类型(10：正式，20：试用)',
  `tip_content` varchar(128) DEFAULT NULL COMMENT '提示内容',
  `tip_date` timestamp NOT NULL COMMENT '提示时间',
  `tip_config_id` bigint NOT NULL COMMENT '提示关联的配置ID',
  PRIMARY KEY (`id`),
  KEY `idx_userId_tipDate` (`user_id`,`tip_date`)
) ENGINE=InnoDB AUTO_INCREMENT=5919 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容提示记录';

 

CREATE TABLE IF NOT EXISTS `micro_customer` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  `customer_code` varchar(11) DEFAULT NULL COMMENT '客户编码',
  `customer_name` varchar(64) NOT NULL COMMENT '客户名称',
  `customer_short_name` varchar(32) DEFAULT NULL COMMENT '客户简称',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `active_flag` varchar(1) DEFAULT '1' COMMENT '激活标记1是0否',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `updated_date` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=140 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户基础表';

 

CREATE TABLE IF NOT EXISTS `micro_experience_guide_group` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `group_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '节点组编码',
  `group_name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '节点组名称',
  `status` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '节点组状态（0：未完成, 1：已完成）',
  `remark` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `active_flag` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '可用标识（0：正常，1：停用）',
  `created_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '000013' COMMENT '租户编码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uni_groupCode_tenantCode` (`group_code`,`tenant_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=608 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体验引导节点组';

 

CREATE TABLE IF NOT EXISTS `micro_experience_guide_node` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `node_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '节点编码',
  `node_name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '节点名称',
  `node_group_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '节点所属组编码',
  `node_sort` int NOT NULL COMMENT '节点排序',
  `skip_page` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '跳转页面',
  `elapsed_time` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '操作耗时',
  `status` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '节点状态（0：未完成，1：已完成）',
  `remark` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `active_flag` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '可用标识（0：正常，1：停用）',
  `created_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '000013' COMMENT '租户编码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uni_nodeCode_tenantCode` (`node_code`,`tenant_code`) USING BTREE,
  KEY `idx_nodeGroupCode` (`node_group_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1234 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体验引导节点配置';

 

CREATE TABLE IF NOT EXISTS `micro_extend_field` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ext_field` varchar(32) DEFAULT NULL COMMENT '扩展字段',
  `ext_field_label` varchar(64) DEFAULT NULL COMMENT '扩展字段名称',
  `ext_field_option` varchar(100) DEFAULT NULL COMMENT '自定义字段选项（#分割）',
  `field_type` varchar(2) DEFAULT NULL COMMENT '字段类型（1-字符串；2-数字；3-日期时间；4-枚举-单选；5-集合-多选 ）',
  `active_flag` varchar(2) DEFAULT '1' COMMENT '在用标志（1-在用；0-删除）',
  `created_by` varchar(64) NOT NULL COMMENT '创建者',
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(64) NOT NULL COMMENT '更新者',
  `last_upd_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=242 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='扩展字段表';

 

CREATE TABLE IF NOT EXISTS `micro_extend_field_relation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ext_field_id` int DEFAULT NULL COMMENT '扩展字段id',
  `business_code` varchar(64) DEFAULT NULL COMMENT '所属领域编码',
  `active_flag` varchar(2) DEFAULT '1' COMMENT '在用标志（1-在用；0-删除）',
  `created_by` varchar(64) NOT NULL COMMENT '创建者',
  `created_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(64) NOT NULL COMMENT '更新者',
  `last_upd_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `sequence` int DEFAULT NULL COMMENT '序号',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=116 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='扩展字段关系表';

 

CREATE TABLE IF NOT EXISTS `micro_factory` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_code` varchar(40) NOT NULL COMMENT '工厂编号',
  `factory_name` varchar(100) NOT NULL COMMENT '工厂名称',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `active_flag` varchar(1) NOT NULL DEFAULT '1' COMMENT '激活标记 1是 0否',
  `created_by` varchar(100) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(100) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` timestamp NULL DEFAULT NULL COMMENT '最后修改时间',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工厂基础信息';

 

CREATE TABLE IF NOT EXISTS `micro_faq` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(64) DEFAULT NULL,
  `title` varchar(256) DEFAULT NULL,
  `content` text,
  `sort` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='FAQ';

 

CREATE TABLE IF NOT EXISTS `micro_finished_product_storage` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `product_seq` varchar(32) NOT NULL COMMENT '产品唯一码',
  `product_name` varchar(256) NOT NULL COMMENT '产品名称',
  `num` decimal(20,4) NOT NULL COMMENT '库存数量',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_productSeq_tenantCode` (`product_seq`,`tenant_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=301 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成品库存表';

 

CREATE TABLE IF NOT EXISTS `micro_finished_product_storage_history` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `product_seq` varchar(32) NOT NULL COMMENT '产品唯一码',
  `product_name` varchar(256) NOT NULL COMMENT '产品名称',
  `change_type` varchar(8) NOT NULL COMMENT '变更类型（10:完工入库，20:完工撤销，30:库存变动，40:手动出库，50:库存导入，60:手动入库，70:生产投料，80:生产退料）',
  `change_num` decimal(20,4) NOT NULL COMMENT '变更数量',
  `finish_change_num` decimal(20,4) NOT NULL COMMENT '变更后库存数量',
  `change_time` datetime NOT NULL COMMENT '变更时间',
  `change_user` bigint NOT NULL COMMENT '变更人',
  `change_reason` varchar(128) DEFAULT NULL COMMENT '变更原因',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_productSeq_changeType` (`product_seq`,`change_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1781 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成品库存变更历史表';

 

CREATE TABLE IF NOT EXISTS `micro_finished_storage_imported_temp` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `product_code` varchar(32) NOT NULL COMMENT '产品编码',
  `product_name` varchar(256) NOT NULL COMMENT '产品名称',
  `product_type` varchar(10) DEFAULT NULL COMMENT '产品类型(成品CP、半成品BCP、原材料YCL)',
  `stock_upper_limit` decimal(20,4) DEFAULT NULL COMMENT '安全库存上限',
  `stock_lower_limit` decimal(20,4) DEFAULT NULL COMMENT '安全库存下限',
  `unit` varchar(10) DEFAULT NULL COMMENT '单位',
  `standards` varchar(60) DEFAULT NULL COMMENT '规格',
  `storage_num` decimal(20,4) NOT NULL COMMENT '导入库存数量',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`),
  KEY `idx_productCode` (`product_code`) USING BTREE,
  KEY `idx_createdBy_tenantCode` (`created_by`,`tenant_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产成品库存导入临时表';

 

CREATE TABLE IF NOT EXISTS `micro_follow` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `self_id` bigint NOT NULL COMMENT '自身ID',
  `follow_id` bigint NOT NULL COMMENT '被关注对象的ID',
  `follow_type` varchar(10) NOT NULL COMMENT '被关注对象的类型(EMPLOYEE/PRODUCT/PROCESS等)',
  `tenant_code` varchar(40) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=344 DEFAULT CHARSET=utf8mb3 COMMENT='我的关注';

 

CREATE TABLE IF NOT EXISTS `micro_manufacture_line` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mline_code` varchar(40) NOT NULL COMMENT '生产线编号',
  `mline_name` varchar(500) NOT NULL COMMENT '生产线名称',
  `wshop_code` varchar(40) NOT NULL COMMENT '车间编号',
  `wshop_name` varchar(100) DEFAULT NULL COMMENT '车间名称',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `active_flag` varchar(1) DEFAULT '1' COMMENT '激活标记 1是 0否',
  `created_by` varchar(100) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(100) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_line` (`mline_code`,`wshop_code`,`tenant_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='生产线基础信息';

 

CREATE TABLE IF NOT EXISTS `micro_manufacture_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(40) NOT NULL COMMENT '生产订单号',
  `ow_code` varchar(40) DEFAULT NULL COMMENT '客户编码',
  `product_seq` varchar(255) NOT NULL COMMENT '产品编码',
  `plan_num` decimal(20,4) DEFAULT '0.0000' COMMENT '计划产量\n',
  `delivery_date` datetime NOT NULL COMMENT '交付日期',
  `start_date` datetime DEFAULT NULL COMMENT '生产开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '生产完工时间',
  `order_status` varchar(3) NOT NULL DEFAULT '10' COMMENT '订单状态：10待排期、20待生产、30生产中、40已完成、50 关闭',
  `before_status` varchar(3) DEFAULT NULL COMMENT '原状态（上一状态）',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `finish_num` decimal(20,4) DEFAULT '0.0000' COMMENT '完成数量',
  `ng_num` decimal(20,4) DEFAULT '0.0000' COMMENT '不良品数量',
  `close_reason` varchar(200) DEFAULT NULL COMMENT '关单原因',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  `extend_content` json DEFAULT NULL COMMENT '扩展字段\n',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) NOT NULL COMMENT '最后修改人',
  `last_upd_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `active_flag` varchar(1) DEFAULT '1' COMMENT '激活标记1是0否',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=652 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='生产订单';

 

CREATE TABLE IF NOT EXISTS `micro_manufacture_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `tenant_code` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '租户编码',
  `task_no` varchar(40) NOT NULL COMMENT '任务单号',
  `work_order_no` varchar(40) NOT NULL COMMENT '工单号',
  `order_no` varchar(40) DEFAULT NULL COMMENT '生产订单号',
  `product_seq` varchar(60) NOT NULL COMMENT '产品seq',
  `process_seq` varchar(60) NOT NULL DEFAULT '0' COMMENT '工序seq',
  `task_plan_num` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '计划数量',
  `submitable_num` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '可报工数量',
  `pass_num` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '实际报工合格数量',
  `ng_num` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '不良品数量',
  `pend_ng_num` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '待处理不合格数量',
  `submit_status` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '00' COMMENT '报工状态（00-待报工；10-报工中；20-报工完成，30-已关闭）',
  `before_status` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '上一状态\n',
  `wshop_code` varchar(40) DEFAULT NULL COMMENT '车间编码',
  `mline_code` varchar(60) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '产线编码',
  `remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `tech_type` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'STANDARD标准  CRAFT草稿',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) NOT NULL COMMENT '最后修改人',
  `last_upd_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `active_flag` varchar(1) DEFAULT '1' COMMENT '激活标记1是0否',
  `is_last_process` varchar(1) DEFAULT NULL COMMENT '是否最后一道工序1不是,0是',
  `is_first_process` varchar(1) DEFAULT NULL COMMENT '是否首序标示，0是，1否',
  `sort` int DEFAULT NULL COMMENT '工序任务下发顺序',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_work_order` (`tenant_code`,`work_order_no`,`order_no`,`is_last_process`,`is_first_process`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1364 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='生产任务表';

 

CREATE TABLE IF NOT EXISTS `micro_manufacture_work_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  `work_order_no` varchar(40) NOT NULL COMMENT '工单号',
  `order_no` varchar(40) DEFAULT NULL COMMENT '生产订单号',
  `word_order_type` varchar(4) DEFAULT 'ZP' COMMENT '工单类型',
  `product_seq` varchar(60) NOT NULL COMMENT '产品编码',
  `wshop_code` varchar(40) DEFAULT NULL COMMENT '车间编码',
  `mline_code` varchar(40) DEFAULT NULL COMMENT '生产线编码',
  `work_order_num` decimal(20,4) DEFAULT '0.0000' COMMENT '工单数量',
  `delivery_date` datetime DEFAULT NULL COMMENT '订单交付日期',
  `plan_start_date` datetime DEFAULT NULL COMMENT '计划开始时间',
  `plan_end_date` datetime DEFAULT NULL COMMENT '计划完工时间',
  `produce_start_date` datetime DEFAULT NULL COMMENT '生产开始时间',
  `produce_end_date` datetime DEFAULT NULL COMMENT '生产完工时间',
  `work_order_status` varchar(2) NOT NULL DEFAULT '10' COMMENT '工单状态：10待生产、20生产中、30已完成、40关闭',
  `before_status` varchar(4) DEFAULT NULL COMMENT '原状态（上一状态）',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `send_by` bigint DEFAULT NULL COMMENT '开工人',
  `send_date` datetime DEFAULT NULL COMMENT '开工时间',
  `inbound_date` datetime DEFAULT NULL COMMENT '入库时间',
  `finish_num` decimal(20,4) DEFAULT '0.0000' COMMENT '完成数量',
  `ng_num` decimal(20,4) DEFAULT NULL COMMENT '不良品数量',
  `close_reason` varchar(200) DEFAULT NULL COMMENT '关单原因',
  `extend_content` json DEFAULT NULL COMMENT '扩展字段',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) NOT NULL COMMENT '最后修改人',
  `last_upd_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `active_flag` varchar(1) DEFAULT '1' COMMENT '激活标记1是0否',
  `main_flag` varchar(1) DEFAULT '0' COMMENT '主工单标记:1是0否',
  `is_complete` char(1) NOT NULL DEFAULT '1' COMMENT '是否已完工(0:是，1:否)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wshop_mline` (`tenant_code`,`wshop_code`,`mline_code`) USING BTREE,
  KEY `idx_work_order` (`tenant_code`,`work_order_no`,`order_no`),
  KEY `idx_start_date` (`plan_start_date`,`plan_end_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=105592 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='生产工单';

 

CREATE TABLE IF NOT EXISTS `micro_notice_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `business_sign` varchar(8) NOT NULL COMMENT '业务标识(10:生产日报，20:生产周报)',
  `notice_roles` varchar(64) DEFAULT NULL COMMENT '通知角色编码（多个用,分割）',
  `notice_users` varchar(128) DEFAULT NULL COMMENT '通知用户(多个用,分割)',
  `app_sign` varchar(32) NOT NULL COMMENT '应用标识(micro_process:KU易记，micro_plan:工易派)',
  `notice_channels` varchar(64) NOT NULL COMMENT '通知渠道(多个用,分割)（10:微信公众号，20:微信小程序，30:sms）',
  `notice_conditions` varchar(128) DEFAULT NULL COMMENT '通知条件配置（格式：key1:value1,key2:value2…）',
  `active_flag` char(1) DEFAULT NULL COMMENT '可用标识（0：正常，1：停用）',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_tenantCode_businessSign_appSign` (`business_sign`,`app_sign`,`tenant_code`) USING BTREE,
  KEY `idx_tenantCode_businessSign` (`business_sign`,`tenant_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1025 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知配置表';

 

CREATE TABLE IF NOT EXISTS `micro_pick_materials` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `picking_no` varchar(20) DEFAULT NULL COMMENT '投退料单号（投料单LL开头；退料单TL开头）',
  `order_no` varchar(40) DEFAULT NULL COMMENT '生产订单号',
  `work_order_no` varchar(40) NOT NULL COMMENT '工单号',
  `product_seq` varchar(60) NOT NULL COMMENT '物料序列号',
  `materials_type` varchar(1) NOT NULL COMMENT '类型：0:投料单，1：退料单',
  `picking_number` decimal(20,4) DEFAULT '0.0000' COMMENT '实际投料/退料',
  `activity_flag` varchar(2) NOT NULL DEFAULT '1' COMMENT '激活标记 1是 0否',
  `created_by` varchar(100) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(100) DEFAULT NULL COMMENT '最后修改人',
  `last_up_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=484 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投料单/退料单';

 

CREATE TABLE IF NOT EXISTS `micro_process_chain` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tech_id` bigint NOT NULL COMMENT '工艺id(technology_id)',
  `process_id` bigint NOT NULL COMMENT '工序id',
  `parent_process_id` bigint NOT NULL COMMENT '父工序ID',
  `process_seq` varchar(40) NOT NULL COMMENT '工序seq',
  `process_code` varchar(40) NOT NULL COMMENT '工序编码',
  `parent_process_seq` varchar(40) NOT NULL COMMENT '父节点工序序列码',
  `is_last_process` varchar(1) DEFAULT '1' COMMENT '是否最后一道工序1不是,0是',
  `sort` int DEFAULT NULL COMMENT '工艺链顺序',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `active_flag` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '1' COMMENT '激活标记1是0否',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `IDX_TECH_TENANT` (`tech_id`,`tenant_code`,`active_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=74827 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工艺链';

 

CREATE TABLE IF NOT EXISTS `micro_process_chain_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tech_id` bigint NOT NULL COMMENT '工艺id(technology_id)',
  `process_id` bigint NOT NULL COMMENT '工序id',
  `parent_process_id` bigint NOT NULL COMMENT '父工序ID',
  `process_seq` varchar(40) NOT NULL COMMENT '工序seq',
  `process_code` varchar(40) NOT NULL COMMENT ' 工序编码',
  `parent_process_seq` varchar(40) NOT NULL COMMENT '父节点工序序列码',
  `is_last_process` varchar(1) DEFAULT '1' COMMENT '是否最后一道工序1不是,0是',
  `sort` int DEFAULT NULL COMMENT '工艺链顺序',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `active_flag` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '激活标记1是0否',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `IDX_TECH_TENANT` (`tech_id`,`tenant_code`,`active_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=74756 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工艺链';

 

CREATE TABLE IF NOT EXISTS `micro_process_chain_imported_temp` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `product_code` varchar(32) NOT NULL COMMENT '产品编码',
  `product_name` varchar(256) NOT NULL COMMENT '产品名称',
  `produce_mode` varchar(32) NOT NULL COMMENT '生产模式（SEQUENCE:顺序，PARALLEL:并序）',
  `process_name` varchar(256) NOT NULL COMMENT '工序名称',
  `sort` bigint DEFAULT NULL COMMENT '生产顺序',
  `first_or_last_process` varchar(32) DEFAULT NULL COMMENT '首尾序标识（FIRST_PROCESS:首序，LAST_PROCESS:尾序）',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`),
  KEY `idx_tenantCode_productCode` (`tenant_code`,`product_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工艺导入临时表';

 

CREATE TABLE IF NOT EXISTS `micro_process_common` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `process_seq` varchar(40) NOT NULL COMMENT '工序唯一码（以此字段关联）',
  `process_code` varchar(40) NOT NULL COMMENT '工序编码',
  `process_name` varchar(256) NOT NULL COMMENT '工序名称',
  `process_desc` varchar(80) DEFAULT NULL COMMENT '工序描述',
  `process_group` varchar(40) DEFAULT NULL COMMENT '工序分组',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_type` varchar(2) NOT NULL DEFAULT '0' COMMENT '创建类型0自动1手动',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_process` (`tenant_code`,`process_code`) USING BTREE,
  UNIQUE KEY `uniq_process_name` (`tenant_code`,`process_name`) USING BTREE,
  UNIQUE KEY `uniq_process_seq` (`tenant_code`,`process_seq`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5829 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础工序表';

 

CREATE TABLE IF NOT EXISTS `micro_process_storage` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `product_seq` varchar(40) NOT NULL COMMENT '产品唯一码(用来关联产品)',
  `process_seq` varchar(40) NOT NULL COMMENT '工序唯一码（用来关联工序）',
  `pass_num` decimal(20,4) DEFAULT NULL COMMENT '良品数量',
  `ng_num` decimal(20,4) DEFAULT NULL COMMENT '不良品数量',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_storage` (`tenant_code`,`product_seq`,`process_seq`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5425 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='在制品库存表';

 

CREATE TABLE IF NOT EXISTS `micro_process_storage_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `product_seq` varchar(255) NOT NULL COMMENT '产品唯一码',
  `product_code` varchar(40) NOT NULL COMMENT '产品编码',
  `product_name` varchar(256) NOT NULL COMMENT '产品名称',
  `process_seq` varchar(40) NOT NULL COMMENT '工序唯一码',
  `process_code` varchar(40) NOT NULL COMMENT '工序编码',
  `process_name` varchar(256) NOT NULL COMMENT '工序名称',
  `operate_node` varchar(40) NOT NULL COMMENT '操作节点(手动调整、出入库、工序流转)',
  `pass_from_num` decimal(20,4) NOT NULL COMMENT '良品数量变化前',
  `pass_to_num` decimal(20,4) NOT NULL COMMENT '良品数量变化后',
  `ng_from_num` decimal(20,4) NOT NULL COMMENT '不良品数量变化前',
  `ng_to_num` decimal(20,4) NOT NULL COMMENT '不良品数量变化后',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_product_process` (`tenant_code`,`product_seq`,`process_seq`,`operate_node`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=26783 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='在制品库存变动表';

 

CREATE TABLE IF NOT EXISTS `micro_process_storage_imported_temp` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `product_code` varchar(32) NOT NULL COMMENT '产品编码',
  `product_name` varchar(256) NOT NULL COMMENT '产品名称',
  `process_code` varchar(32) NOT NULL COMMENT '工序编码',
  `process_name` varchar(256) NOT NULL COMMENT '工序名称',
  `pass_num_adjusted` decimal(20,4) NOT NULL COMMENT '调整后良品数量',
  `ng_num_adjusted` decimal(20,4) NOT NULL COMMENT '调整后不良品数量',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`),
  KEY `idx_productCode_processCode` (`product_code`,`process_code`) USING BTREE,
  KEY `idx_createdBy_tenantCode` (`created_by`,`tenant_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='期初工序库存导入临时表';

 

CREATE TABLE IF NOT EXISTS `micro_product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `product_seq` varchar(40) NOT NULL COMMENT '产品唯一码',
  `product_code` varchar(40) NOT NULL COMMENT '产品编码',
  `product_name` varchar(256) NOT NULL COMMENT '产品名称',
  `product_desc` varchar(300) DEFAULT NULL COMMENT '产品描述',
  `product_type` varchar(10) DEFAULT NULL COMMENT '产品类型(成品CP、半成品BCP、原材料YCL)',
  `production_mode` varchar(4) NOT NULL DEFAULT '10' COMMENT '生产方式(10自制20外购30委外)',
  `picture` longtext COMMENT '产品图片',
  `unit` varchar(10) DEFAULT NULL COMMENT '单位',
  `standards` varchar(60) DEFAULT NULL COMMENT '规格',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `stock_upper_limit` decimal(20,4) DEFAULT NULL COMMENT '安全库存上限',
  `stock_lower_limit` decimal(20,4) DEFAULT NULL COMMENT '安全库存下限',
  `created_type` varchar(2) NOT NULL DEFAULT '0' COMMENT '创建类型0自动1手动',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_product_code` (`tenant_code`,`product_code`) USING BTREE,
  UNIQUE KEY `uniq_product_seq` (`tenant_code`,`product_seq`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5735 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础产品表';

 

CREATE TABLE IF NOT EXISTS `micro_product_bom` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  `product_seq` varchar(60) NOT NULL COMMENT '产品编码',
  `product_type` varchar(10) DEFAULT NULL COMMENT '产品类型(成品CP、半成品BCP、原材料YCL)',
  `production_mode` varchar(5) DEFAULT NULL COMMENT '生产方式(10自制20外购30委外)',
  `product_number` decimal(20,4) DEFAULT '1.0000' COMMENT '使用数量',
  `parent_product_seq` varchar(60) DEFAULT NULL COMMENT '上级编码',
  `parent_id` bigint DEFAULT NULL COMMENT '上级id',
  `if_children` varchar(1) DEFAULT '0' COMMENT '是否存在下级  0否  1是',
  `bom_type` varchar(10) DEFAULT NULL COMMENT 'STANDARD标准 DRAFT 草稿',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) NOT NULL COMMENT '最后修改人',
  `last_upd_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `active_flag` varchar(1) DEFAULT '1' COMMENT '激活标记1是0否',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `IDX_PRODUCT_CODE` (`product_seq`) USING BTREE,
  KEY `IDX_PARENT_PROD_CODE` (`parent_product_seq`) USING BTREE,
  KEY `IDX_PARENT_ID` (`parent_id`) USING BTREE,
  KEY `idx_tenant_code` (`tenant_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=54923198106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品BOM表';

 

CREATE TABLE IF NOT EXISTS `micro_product_bom_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  `product_seq` varchar(60) NOT NULL COMMENT '产品编码',
  `product_type` varchar(10) DEFAULT NULL COMMENT '产品类型(成品CP、半成品BCP、原材料YCL)',
  `production_mode` varchar(20) DEFAULT NULL COMMENT '制造方式',
  `product_number` decimal(20,4) DEFAULT '1.0000' COMMENT '使用数量 ',
  `parent_product_seq` varchar(60) DEFAULT NULL COMMENT '上级编码',
  `parent_id` bigint DEFAULT NULL COMMENT '上级id',
  `if_children` varchar(1) DEFAULT '0' COMMENT '是否存在下级  0否  1是',
  `bom_type` varchar(10) DEFAULT NULL COMMENT 'STANDARD标准 DRAFT 草稿',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `version` varchar(40) DEFAULT NULL COMMENT '历史版本',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) NOT NULL COMMENT '最后修改人',
  `last_upd_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `active_flag` varchar(1) DEFAULT '1' COMMENT '激活标记1是0否',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `IDX_PRODUCT_CODE` (`product_seq`) USING BTREE,
  KEY `IDX_PARENT_PROD_CODE` (`parent_product_seq`) USING BTREE,
  KEY `IDX_PARENT_ID` (`parent_id`) USING BTREE,
  KEY `idx_tenant_code` (`tenant_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=54923197486 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品BOM表';

 

CREATE TABLE IF NOT EXISTS `micro_product_bom_imported_temp` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `product_code` varchar(32) NOT NULL COMMENT '产品编码',
  `product_name` varchar(256) NOT NULL COMMENT '产品名称',
  `hierarchy` int NOT NULL COMMENT '层级',
  `component_quantity` decimal(20,4) NOT NULL COMMENT '组成数量',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`),
  KEY `idx_tenantCode_productCode` (`tenant_code`,`product_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='BOM导入临时表';

 

CREATE TABLE IF NOT EXISTS `micro_quality_control_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键id',
  `quality_control_record_no` varchar(40) NOT NULL COMMENT '质检记录单号',
  `submit_id` bigint NOT NULL COMMENT '报工记录id',
  `product_seq` varchar(40) NOT NULL COMMENT '产品序列码',
  `process_seq` varchar(40) NOT NULL COMMENT '工序序列码',
  `pre_process_seq` varchar(40) DEFAULT NULL COMMENT '前工序序列码',
  `ng_type` varchar(40) DEFAULT NULL COMMENT '不良类型',
  `pass_num` decimal(20,4) NOT NULL COMMENT '良品数量',
  `ng_num` decimal(20,4) NOT NULL COMMENT '不良数量',
  `submit_user` int DEFAULT NULL COMMENT '报工人',
  `submit_date` datetime DEFAULT NULL COMMENT '报工时间',
  `created_by` int NOT NULL COMMENT '质检人员',
  `created_date` datetime NOT NULL COMMENT '质检时间',
  `remark` varchar(50) DEFAULT NULL COMMENT '备注',
  `last_upd_by` int DEFAULT NULL COMMENT '最后更新人',
  `last_upd_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间\n',
  `active_flag` varchar(1) DEFAULT '1' COMMENT '激活标记 1是 0否',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=558 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质检记录表';

 

CREATE TABLE IF NOT EXISTS `micro_recommend_hit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `username` varchar(40) NOT NULL COMMENT '用户名',
  `origin_data` text COMMENT '源数据',
  `use_data` varchar(255) DEFAULT NULL COMMENT '使用数据',
  `data_id` varchar(40) DEFAULT NULL COMMENT '数据ID',
  `data_type` varchar(40) DEFAULT NULL COMMENT '数据类型',
  `hit_precision` decimal(10,2) DEFAULT '0.00' COMMENT '命中精准度',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=31672 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='推荐数据命中信息表';

 

CREATE TABLE IF NOT EXISTS `micro_repair_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键id',
  `repair_no` varchar(40) NOT NULL COMMENT '返修单号',
  `submit_id` bigint NOT NULL COMMENT '报工记录id',
  `product_seq` varchar(255) NOT NULL COMMENT '产品序列码',
  `process_seq` varchar(255) NOT NULL COMMENT '工序序列码',
  `submit_user` int NOT NULL COMMENT '报工人',
  `pre_process_seq` varchar(255) DEFAULT NULL COMMENT '前工序序列码',
  `repair_user` int NOT NULL COMMENT '返修人',
  `repair_num` decimal(20,4) DEFAULT NULL COMMENT '返修完成数量',
  `concession_num` decimal(20,4) DEFAULT NULL COMMENT '让步接收数量',
  `abandoned_num` decimal(20,4) DEFAULT NULL COMMENT '报废数量',
  `abandoned_type` varchar(40) DEFAULT NULL COMMENT '报废类型',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` int NOT NULL COMMENT '复核人员',
  `created_date` datetime NOT NULL COMMENT '复核时间',
  `last_upd_by` int DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  `active_flag` varchar(1) DEFAULT '1' COMMENT '激活标记 1是 0否',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`),
  KEY `idx_productSeq_processSeq_submitUser` (`product_seq`,`process_seq`,`submit_user`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=323 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='返修记录表';

 

CREATE TABLE IF NOT EXISTS `micro_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `role_code` varchar(40) DEFAULT NULL COMMENT '角色码',
  `role_name` varchar(40) DEFAULT NULL COMMENT '角色名称',
  `status` char(1) DEFAULT '0' COMMENT '角色状态(0正常 1停用)',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_role_tenant` (`tenant_code`,`role_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=379 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

 

CREATE TABLE IF NOT EXISTS `micro_settlement_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `submit_id` bigint NOT NULL COMMENT '报工ID',
  `employee_id` bigint NOT NULL COMMENT '报工人',
  `adjusted_from_num` decimal(20,4) DEFAULT '0.0000' COMMENT '调整前数量',
  `adjusted_num` decimal(20,4) DEFAULT NULL COMMENT '调整后可结数量',
  `product_seq` varchar(40) NOT NULL COMMENT '产品唯一码',
  `product_code` varchar(40) NOT NULL COMMENT '产品编码',
  `product_name` varchar(256) NOT NULL COMMENT '产品名称',
  `product_unit` varchar(40) DEFAULT NULL COMMENT '产品单位',
  `operate_process_seq` varchar(255) NOT NULL COMMENT '操作工序唯一码',
  `operate_process_code` varchar(40) NOT NULL COMMENT '操作工序编码',
  `operate_process_name` varchar(256) NOT NULL COMMENT '操作工序名称',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `version` varchar(40) NOT NULL COMMENT '调整流水号',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3583 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计件结算调整历史';

 

CREATE TABLE IF NOT EXISTS `micro_settlement_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `submit_id` bigint NOT NULL COMMENT '报工ID',
  `employee_id` bigint NOT NULL COMMENT '报工人',
  `checked_num` decimal(20,4) NOT NULL COMMENT '审核后数量',
  `settled_num` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '已结算数量',
  `adjusted_num` decimal(20,4) DEFAULT NULL COMMENT '调整后可结数量',
  `settlement_day` date DEFAULT NULL COMMENT '结算日',
  `product_seq` varchar(40) NOT NULL COMMENT '产品唯一码',
  `operate_process_seq` varchar(255) NOT NULL COMMENT '操作工序唯一码',
  `product_complete_num` decimal(20,4) DEFAULT NULL COMMENT '产品完工数量(记录结算时扣减的完工报告的产品数量)',
  `product_complete_report_no` text COMMENT '结算时扣减的完工报告单号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `settlement_status` varchar(10) DEFAULT NULL COMMENT '结算类型(未结算10,已结算20)',
  `settlement_no` varchar(40) DEFAULT NULL COMMENT '结算报告号',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3651 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计件结算报告';

 

CREATE TABLE IF NOT EXISTS `micro_suggestion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) DEFAULT NULL,
  `tenant_code` varchar(32) DEFAULT NULL,
  `content` text,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_tenant` (`tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户反馈';

 

CREATE TABLE IF NOT EXISTS `micro_tags` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '标签名称',
  `tag_type` varchar(40) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '标签类型',
  `tenant_code` varchar(40) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '租户编码',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_tag` (`tag_name`,`tag_type`,`tenant_code`,`created_by`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=298 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';

 

CREATE TABLE IF NOT EXISTS `micro_technology` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tech_code` varchar(40) NOT NULL COMMENT '工艺编码',
  `tech_name` varchar(80) NOT NULL COMMENT '工艺名称',
  `tech_group` varchar(40) DEFAULT NULL COMMENT '工艺组',
  `tech_desc` varchar(255) DEFAULT NULL COMMENT '工艺描述',
  `tech_version` varchar(20) NOT NULL COMMENT '工艺版本',
  `product_seq` varchar(40) DEFAULT NULL COMMENT '产品seq',
  `product_id` bigint DEFAULT NULL COMMENT '产品ID',
  `relation_type` varchar(10) NOT NULL COMMENT '关联类型(产品/产品组)',
  `tech_type` varchar(10) DEFAULT 'STANDARD' COMMENT 'STANDARD标准 DRAFT 草稿',
  `tech_pattern` bigint DEFAULT '10' COMMENT '工艺形式 10顺序20乱序30推荐35工单下发草稿40期初导入',
  `class_code` varchar(40) DEFAULT NULL COMMENT '产品组编码',
  `class_name` varchar(80) DEFAULT NULL COMMENT '产品组名称',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) NOT NULL COMMENT '最后修改人',
  `last_upd_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `active_flag` varchar(1) DEFAULT '1' COMMENT '激活标记1是0否',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `UNI_TECH` (`product_id`,`product_seq`,`tech_type`,`relation_type`,`active_flag`) USING BTREE COMMENT '唯一'
) ENGINE=InnoDB AUTO_INCREMENT=22645312797921 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工艺表';

 

CREATE TABLE IF NOT EXISTS `micro_technology_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tech_code` varchar(40) NOT NULL COMMENT '工艺编码',
  `tech_name` varchar(80) NOT NULL COMMENT '工艺名称',
  `tech_group` varchar(40) DEFAULT NULL COMMENT '工艺组',
  `tech_desc` varchar(255) DEFAULT NULL COMMENT '工艺描述',
  `tech_version` varchar(20) NOT NULL COMMENT '工艺版本',
  `product_seq` varchar(40) DEFAULT NULL COMMENT '产品seq',
  `product_id` bigint DEFAULT NULL COMMENT '产品ID',
  `relation_type` varchar(10) NOT NULL COMMENT '关联类型(产品/产品组)',
  `tech_type` varchar(10) NOT NULL DEFAULT 'STANDARD' COMMENT 'STANDARD标准 DRAFT 草稿',
  `tech_pattern` bigint DEFAULT '10' COMMENT '工艺形式 10顺序20乱序30未确认(推荐/工易派)40期初导入',
  `class_code` varchar(40) DEFAULT NULL COMMENT '产品组编码',
  `class_name` varchar(80) DEFAULT NULL COMMENT '产品组名称',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) NOT NULL COMMENT '最后修改人',
  `last_upd_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `active_flag` varchar(1) DEFAULT '0' COMMENT '激活标记1是0否',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `IDX_REL_PRODUCT` (`product_seq`,`product_id`,`relation_type`,`active_flag`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9898448767521 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工艺表';

 

CREATE TABLE IF NOT EXISTS `micro_tenant` (
  `tenant_code` varchar(32) NOT NULL,
  `tenant_name` varchar(128) NOT NULL,
  `customer_name` varchar(128) DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户元数据';

 

CREATE TABLE IF NOT EXISTS `micro_tenant_individuation_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `submit_inspect_switch` char(1) NOT NULL DEFAULT '1' COMMENT '记工送检开关（0:开启，1:关闭）',
  `batch_submit_switch` char(1) NOT NULL DEFAULT '0' COMMENT '批量记工开关（0:开启，1:关闭）',
  `worker_base_data_confine` char(1) NOT NULL DEFAULT '1' COMMENT '员工基础数据限制（0:开启，1:关闭）',
  `created_by` varchar(64) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_code`)
) ENGINE=InnoDB AUTO_INCREMENT=135 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业个性化设置配置表';

 

CREATE TABLE IF NOT EXISTS `micro_trial_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_name` varchar(64) DEFAULT NULL COMMENT '用户名',
  `phone_number` varchar(11) NOT NULL COMMENT '手机号',
  `company_name` varchar(64) DEFAULT NULL COMMENT '企业名称',
  `purchase_state` char(1) NOT NULL COMMENT '购买标记（0：未购买，1：已购买）',
  `industry` varchar(64) NOT NULL COMMENT '行业',
  `enterprise_scale` varchar(64) NOT NULL COMMENT '企业规模',
  `user_role` varchar(64) NOT NULL COMMENT '用户角色',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` varchar(32) NOT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_phoneNumber` (`phone_number`)
) ENGINE=InnoDB AUTO_INCREMENT=150 DEFAULT CHARSET=utf8mb3 COMMENT='体验登记信息';

 

CREATE TABLE IF NOT EXISTS `micro_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `tenant_name` varchar(80) DEFAULT NULL COMMENT '租户名称',
  `user_name` varchar(40) NOT NULL COMMENT '账号',
  `nick_name` varchar(30) NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) DEFAULT NULL COMMENT '用户类型',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `sex` char(1) DEFAULT '2' COMMENT '用户性别（0男 1女 2未知）',
  `phonenumber` varchar(20) DEFAULT NULL COMMENT '手机号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `valid_date` datetime DEFAULT NULL COMMENT '账号有效期（已废弃，使用app_re表）',
  `status` char(1) DEFAULT '0' COMMENT '账号状态(0正常 1停用)账号有效期（已废弃，使用app_re表）',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_user_tenant` (`tenant_code`,`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=615 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

 

CREATE TABLE IF NOT EXISTS `micro_user_app_re` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `user_name` varchar(40) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `tenant_code` varchar(40) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `app_code` varchar(80) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `user_status` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '0' COMMENT '账号状态(0正常 1停用)',
  `valid_date` datetime NOT NULL COMMENT '账号有效期',
  `created_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=534 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与小程序映射关系';

 

CREATE TABLE IF NOT EXISTS `micro_user_event_tracking` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `event_code` varchar(40) NOT NULL COMMENT '事件编码',
  `event_name` varchar(80) NOT NULL COMMENT '事件中文名称',
  `event_type` varchar(10) NOT NULL COMMENT '事件类型,TP(time on page)页面停留时长事件,EP(expose)曝光PV事件,CLICK点击事件',
  `event_content` varchar(255) DEFAULT NULL COMMENT '事件内容',
  `event_source` varchar(255) NOT NULL COMMENT '事件来源PAGE/VIDEO/IMAGE',
  `duration` decimal(20,4) DEFAULT NULL COMMENT '针对页面停留时间-停留时长秒,曝光事件/点击事件是次数',
  `weight` decimal(2,1) NOT NULL DEFAULT '1.0' COMMENT '权重',
  `user_type` varchar(10) NOT NULL COMMENT '用户体验或正式EXPERIENCE/OFFICIAL',
  `tenant_code` varchar(40) DEFAULT NULL COMMENT '租户编码',
  `tenant_name` varchar(80) DEFAULT NULL COMMENT '租户名称',
  `phonenumber` bigint NOT NULL COMMENT '用户手机号',
  `user_ip` varchar(255) DEFAULT NULL COMMENT '用户IP',
  `user_location` varchar(255) DEFAULT NULL COMMENT '用户位置',
  `user_agent` mediumtext NOT NULL COMMENT 'user-agent',
  `current_page` varchar(255) NOT NULL COMMENT '当前页面',
  `refer_page` varchar(255) DEFAULT NULL COMMENT '上一级页面',
  `created_day` int NOT NULL COMMENT '20230101',
  `created_month` int NOT NULL COMMENT '202301',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `application_sign` varchar(32) NOT NULL COMMENT '应用标识',
  PRIMARY KEY (`id`),
  KEY `idx_common_query` (`event_code`,`event_type`,`tenant_code`,`application_sign`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=14235 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户行为事件埋点';

 

CREATE TABLE IF NOT EXISTS `micro_user_individuation_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户id',
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `personalized` int NOT NULL DEFAULT '0' COMMENT '个性化推荐(0关闭,1开始)',
  `statistics_begin_time` timestamp NULL DEFAULT NULL COMMENT '统计数据开始时间,如果为空,说明全量统计,如果有值,那从这个时间节点向后统计,为了角色发生变化导致的推荐不准确问题',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_config` (`user_id`,`tenant_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户个性化配置表';

 

CREATE TABLE IF NOT EXISTS `micro_user_platfrom_re` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `tenant_code` varchar(40) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '000013' COMMENT '租户编码',
  `open_id` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '平台用户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `application_sign` varchar(32) NOT NULL COMMENT '应用标识',
  `platform_type` varchar(16) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '平台类型',
  `remark` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `active_flag` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '可用标识（0：正常，1：停用）',
  `created_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '更新人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_userId` (`user_id`,`platform_type`,`application_sign`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=401 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户平台信息关联表';

 

CREATE TABLE IF NOT EXISTS `micro_user_role_re` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `role_id` varchar(40) DEFAULT NULL COMMENT '角色ID',
  `user_id` varchar(40) DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=811 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

 

CREATE TABLE IF NOT EXISTS `micro_user_tenant_index` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) NOT NULL,
  `tenant_code` varchar(32) NOT NULL,
  `local_user_id` bigint DEFAULT NULL,
  `is_primary` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone_tenant` (`phone`,`tenant_code`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户-租户关联';

 

CREATE TABLE IF NOT EXISTS `micro_wechat_msg_template_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `template_type` varchar(16) DEFAULT NULL COMMENT '模版类型（10:公众号，20:小程序）',
  `message_type` varchar(16) DEFAULT NULL COMMENT '消息类型（10:生产日报，20:生产周报）',
  `message_clazz` text COMMENT '消息类型clazz（包全路径）',
  `template_id` varchar(64) DEFAULT NULL COMMENT '微信模版ID',
  `message_title` varchar(128) DEFAULT NULL COMMENT '消息标题',
  `skip_url` longtext COMMENT '点击跳转链接',
  `service_sign` varchar(32) DEFAULT NULL COMMENT '服务标识（micro_process:KU易记，micro_plan:工易派）',
  `active_flag` char(1) DEFAULT NULL COMMENT '可用标识（0：正常，1：停用）',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_messageType_serviceSign` (`message_type`,`service_sign`,`template_type`) USING BTREE,
  KEY `idx_serviceSign` (`service_sign`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='微信模版消息配置表';

-- 微信订阅消息模板配置说明（订阅功能必配：小程序端"订阅"按钮依赖本表的 template_id 弹窗）
-- 申请方式：微信公众平台 → 功能 → 订阅消息 → 公共模板库，搜索"生产日报/生产周报"类模板选用，
-- 将返回的 template_id 替换下方占位符后执行（template_type=20 小程序 / 10 公众号；
-- message_type=10 生产日报 / 20 生产周报；service_sign 与小程序 manifest 应用标识一致）。
-- 参考 README「微信订阅消息模板」说明。
-- INSERT INTO `micro_wechat_msg_template_config`
--   (`template_type`, `message_type`, `message_clazz`, `template_id`, `message_title`, `service_sign`, `active_flag`)
-- VALUES
--   ('20', '10', 'com.cosmo.hhim.micro.integration.domain.notify.event.TodayReportMessageEvent', '请替换为真实模板ID', '生产日报', 'micro_process', '0'),
--   ('20', '20', 'com.cosmo.hhim.micro.integration.domain.notify.event.WeekReportMessageEvent',  '请替换为真实模板ID', '生产周报', 'micro_process', '0');

 

CREATE TABLE IF NOT EXISTS `micro_wechat_push_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `to_users` varchar(128) DEFAULT NULL COMMENT '推送用户列表(多个用,分割)',
  `title` varchar(64) DEFAULT NULL COMMENT '推送标题',
  `content` longtext COMMENT '推送内容(json格式)',
  `skip_url` varchar(256) DEFAULT NULL COMMENT '推送跳转URL',
  `wechat_model_id` varchar(128) DEFAULT NULL COMMENT '推送使用的微信模版ID',
  `push_time` datetime DEFAULT NULL COMMENT '推送时间',
  `push_finish_time` datetime DEFAULT NULL COMMENT '推送完成时间',
  `push_status` varchar(16) DEFAULT NULL COMMENT '推送状态(0：待推送，1：已推送)',
  `push_result` varchar(16) DEFAULT NULL COMMENT '推送结果（0：失败，1：成功）',
  `template_type` varchar(16) DEFAULT NULL COMMENT '模版类型（0：小程序，1：公众号）',
  `service_sign` varchar(32) DEFAULT NULL COMMENT '服务标识（在制品库存微应用:micro）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `last_upd_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `last_upd_date` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_serviceSign_templateType` (`service_sign`,`template_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=23044 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='微信消息推送记录';

 

CREATE TABLE IF NOT EXISTS `micro_workshop` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  `wshop_code` varchar(40) NOT NULL COMMENT '车间编号',
  `wshop_name` varchar(100) NOT NULL COMMENT '车间名称',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `active_flag` varchar(1) DEFAULT '1' COMMENT '激活标记 1是 0否',
  `created_by` varchar(100) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(100) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_workshop` (`tenant_code`,`wshop_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='生产车间基础信息';

 

CREATE TABLE IF NOT EXISTS `micro_work_submit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `order_no` varchar(40) DEFAULT NULL COMMENT '生产订单号',
  `work_order_no` varchar(40) DEFAULT NULL COMMENT '工单号',
  `task_no` varchar(40) DEFAULT NULL COMMENT '任务单号',
  `submit_type` int DEFAULT '1' COMMENT '报工类型，1-ku易记， 2-工易派',
  `product_seq` varchar(40) NOT NULL COMMENT '产品唯一码',
  `pre_process_seq` varchar(40) DEFAULT NULL COMMENT '前工序唯一码',
  `operate_process_seq` varchar(255) NOT NULL COMMENT '操作工序唯一码',
  `pre_process_group` varchar(255) DEFAULT NULL COMMENT '前工序分组',
  `operate_process_group` varchar(255) DEFAULT NULL COMMENT '操作工序分组',
  `is_last_process` varchar(2) DEFAULT NULL COMMENT '是否最后一道工序',
  `pass_num` decimal(20,4) NOT NULL COMMENT '良品数量',
  `ng_num` decimal(20,4) NOT NULL COMMENT '不良品数量',
  `submit_user` varchar(40) NOT NULL COMMENT '报工人',
  `submit_day` date NOT NULL COMMENT '报工日',
  `submit_status` int NOT NULL COMMENT '报工状态（1-待审核、0-审核通过、2-驳回）',
  `expired_record_flag` int DEFAULT NULL COMMENT '补录标识(0-是，1-否)',
  `submit_no` varchar(40) NOT NULL COMMENT '报工号(每次生成一个)',
  `data_status` int DEFAULT '1' COMMENT '数据状态(1初始,0修改)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `check_user` varchar(40) DEFAULT NULL COMMENT '审产人',
  `check_date` datetime DEFAULT NULL COMMENT '审产时间',
  `check_pass_num` decimal(20,4) DEFAULT NULL COMMENT '良品审产数',
  `check_ng_num` decimal(20,4) DEFAULT NULL COMMENT '不良品审产数',
  `submit_pictures` text COMMENT '报工图片',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '最后修改人',
  `last_upd_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `is_first_process` varchar(2) DEFAULT NULL COMMENT '是否为首序',
  `is_complete` char(1) NOT NULL DEFAULT '1' COMMENT '是否已完工(0:是，1:否)',
  `check_status` int DEFAULT '0' COMMENT '质检状态，0-待送检， 1-送检， 2-质检完成',
  `repair_num` decimal(20,4) DEFAULT '0.0000' COMMENT '返修数量',
  `abandoned_num` decimal(20,4) DEFAULT '0.0000' COMMENT '报废数量',
  `qc_date` datetime DEFAULT NULL COMMENT '质检时间',
  `qc_user` varchar(40) DEFAULT NULL COMMENT '质检人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_product_process` (`tenant_code`,`product_seq`,`pre_process_seq`,`operate_process_seq`) USING BTREE,
  KEY `idx_user` (`tenant_code`,`submit_user`,`check_user`) USING BTREE,
  KEY `idx_process_num` (`tenant_code`,`product_seq`,`operate_process_seq`,`submit_status`) USING BTREE,
  KEY `idx_status` (`tenant_code`,`submit_type`,`submit_status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20547 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报工记录表';

 

CREATE TABLE IF NOT EXISTS `micro_work_submit_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `product_seq` varchar(40) NOT NULL COMMENT '产品唯一码',
  `product_code` varchar(40) DEFAULT NULL COMMENT '产品编码',
  `product_name` varchar(256) NOT NULL COMMENT '产品名称',
  `pre_process_seq` varchar(40) DEFAULT NULL COMMENT '前工序唯一码',
  `pre_process_code` varchar(40) DEFAULT NULL COMMENT '前工序编码',
  `pre_process_name` varchar(256) DEFAULT NULL COMMENT '操作工序名称',
  `pre_process_group` varchar(255) DEFAULT NULL COMMENT '前工序分组',
  `operate_process_seq` varchar(255) NOT NULL COMMENT '操作工序唯一码',
  `operate_process_name` varchar(256) DEFAULT NULL COMMENT '前工序名称',
  `operate_process_code` varchar(40) DEFAULT NULL COMMENT '操作工序编码',
  `operate_process_group` varchar(255) DEFAULT NULL COMMENT '操作工序分组',
  `is_last_process` varchar(2) DEFAULT NULL COMMENT '是否最后一道工序',
  `pass_num` decimal(20,4) NOT NULL COMMENT '良品数量',
  `ng_num` decimal(20,4) NOT NULL COMMENT '不良品数量',
  `repair_num` decimal(20,4) DEFAULT '0.0000' COMMENT '返修数量',
  `concession_num` decimal(20,4) DEFAULT '0.0000' COMMENT '让步接收数量',
  `settled_num` decimal(20,4) DEFAULT '0.0000' COMMENT '结算数量',
  `abandoned_num` decimal(20,4) DEFAULT '0.0000' COMMENT '报废数量',
  `submit_user` varchar(40) NOT NULL COMMENT '报工人',
  `submit_day` date NOT NULL COMMENT '报工日',
  `submit_status` int NOT NULL COMMENT '报工状态（1-待审核、0-审核通过、2-驳回）',
  `submit_no` varchar(40) NOT NULL COMMENT '报工号(每次生成一个)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `data_status` int DEFAULT '1' COMMENT '数据状态(1初始,0修改)',
  `created_by` varchar(40) NOT NULL COMMENT '创建人',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `operate_node` varchar(40) DEFAULT NULL COMMENT '操作节点(记工、编辑、审核、撤销)',
  `submit_pictures` text COMMENT '报工图片',
  `is_first_process` varchar(2) DEFAULT NULL COMMENT '是否为首序',
  `is_complete` char(1) DEFAULT NULL COMMENT '是否已完工(0:是，1:否)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_product_process` (`tenant_code`,`product_seq`,`pre_process_seq`,`operate_process_seq`) USING BTREE,
  KEY `idx_user` (`tenant_code`,`submit_user`,`created_by`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=31495 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报工记录历史表';

 

CREATE TABLE IF NOT EXISTS `sys_logininfor` (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) DEFAULT '' COMMENT '登录IP地址',
  `status` char(1) DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) DEFAULT '' COMMENT '提示信息',
  `access_time` datetime DEFAULT NULL COMMENT '访问时间',
  `platform_source` varchar(64) DEFAULT NULL COMMENT '来源操作平台',
  `belong_sys` varchar(64) DEFAULT NULL COMMENT '隶属系统',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`info_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6848 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统访问记录';

 

CREATE TABLE IF NOT EXISTS `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(256) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `oper_platform_source` varchar(16) DEFAULT '0' COMMENT '操作来源平台',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) DEFAULT '' COMMENT '部门名称',
  `oper_url` text COMMENT '请求URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(4000) DEFAULT '' COMMENT '请求参数',
  `json_result` longtext COMMENT '返回参数',
  `status` int DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `belong_sys` varchar(64) DEFAULT NULL COMMENT '隶属系统',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`oper_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=30909 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志记录';

 

-- =====================================================
-- 初始化数据（开源演示环境）
-- -----------------------------------------------------
-- 1) 初始租户：A9K3Q7（6 位大写字母数字，与业务生成格式一致）
-- 2) 5 个角色：10 管理员 / 20 审产员 / 25 质检员 / 30 员工 / 40 试用
-- 3) 初始管理员：手机号 13800000000
--    登录方式：短信验证码（本地无短信时，验证码在【后端控制台日志】中查看）
-- 4) 默认工序：下料 / 车削 / 攻丝（可选，方便开箱即用）
-- =====================================================

-- 1. 初始租户
INSERT INTO `micro_tenant` (`tenant_code`, `tenant_name`, `customer_name`, `status`, `created_at`, `updated_at`)
VALUES ('A9K3Q7', '开源演示企业', '演示客户', 1, NOW(), NOW());

-- 2. 角色（企业管理员 / 审产员 / 质检员 / 员工 / 试用角色）
INSERT INTO `micro_role` (`tenant_code`, `role_code`, `role_name`, `status`, `created_by`, `created_date`, `last_upd_by`, `last_upd_date`)
VALUES
('A9K3Q7', '10', '企业管理员', '0', 'SYSTEM', NOW(), 'SYSTEM', NOW()),
('A9K3Q7', '20', '审产员', '0', 'SYSTEM', NOW(), 'SYSTEM', NOW()),
('A9K3Q7', '25', '质检员', '0', 'SYSTEM', NOW(), 'SYSTEM', NOW()),
('A9K3Q7', '30', '员工', '0', 'SYSTEM', NOW(), 'SYSTEM', NOW()),
('A9K3Q7', '40', '试用角色', '0', 'SYSTEM', NOW(), 'SYSTEM', NOW());

-- 3. 初始管理员账号（手机号 13800000000，登录验证码看后端日志）
INSERT INTO `micro_user`
(`tenant_code`, `tenant_name`, `user_name`, `nick_name`, `user_type`, `avatar`, `sex`, `phonenumber`, `remark`, `valid_date`, `status`, `created_by`, `created_date`, `last_upd_by`, `last_upd_date`)
VALUES
('A9K3Q7', '开源演示企业', '13800000000', '演示管理员', NULL, NULL, '2', '13800000000', '开源初始管理员', NULL, '0', 'SYSTEM', NOW(), 'SYSTEM', NOW());
SET @admin_id = LAST_INSERT_ID();

-- 4. 管理员角色关联（role_code = 10）
INSERT INTO `micro_user_role_re` (`tenant_code`, `role_id`, `user_id`)
SELECT 'A9K3Q7', `id`, @admin_id FROM `micro_role` WHERE `tenant_code` = 'A9K3Q7' AND `role_code` = '10';

-- 5. 手机号-租户索引（登录链路必需：手机号 → 租户）
INSERT INTO `micro_user_tenant_index` (`phone`, `tenant_code`, `local_user_id`, `is_primary`, `created_at`)
VALUES ('13800000000', 'A9K3Q7', @admin_id, 1, NOW());

-- 6. 默认工序（可选：也可在界面"基础数据 → 工序管理"自行创建）
INSERT INTO `micro_process_common`
(`tenant_code`, `process_seq`, `process_code`, `process_name`, `created_by`, `created_date`, `last_upd_by`, `last_upd_date`)
VALUES
('A9K3Q7', 'GX000001', 'GX000001', '下料', 'SYSTEM', NOW(), 'SYSTEM', NOW()),
('A9K3Q7', 'GX000002', 'GX000002', '车削', 'SYSTEM', NOW(), 'SYSTEM', NOW()),
('A9K3Q7', 'GX000003', 'GX000003', '攻丝', 'SYSTEM', NOW(), 'SYSTEM', NOW());

-- =====================================================
-- AI 问数 · 会话存储表（原独立脚本 hhim-ai-chat-schema.sql，已并入本文件）
-- -----------------------------------------------------
-- 会话表
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

-- 消息表
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

-- =====================================================
-- 验证查询（执行后应得到预期结果）
-- -----------------------------------------------------
-- SELECT * FROM micro_tenant;                            -- 预期 1 条
-- SELECT * FROM micro_role WHERE tenant_code = 'A9K3Q7'; -- 预期 5 条（10/20/25/30/40）
-- SELECT * FROM micro_user WHERE phonenumber = '13800000000';   -- 预期 1 条
-- SELECT * FROM micro_user_tenant_index WHERE phone = '13800000000'; -- 预期 1 条
-- =====================================================
