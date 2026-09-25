-- ============================================================
-- hyzz_* 三方对接配置表（im-portal 库，thirdplat 后端② db0 数据源专用）
-- 首次初始化自动建库 + 建表（与 init.sql 一起由 mysql 镜像自动执行）
-- ============================================================
CREATE DATABASE IF NOT EXISTS `im-portal` DEFAULT CHARACTER SET utf8mb4;
USE `im-portal`;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- 导出  表 im-portal.hyzz_customer 结构
CREATE TABLE IF NOT EXISTS `hyzz_customer` (
  `row_id` int(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `customer_code` varchar(50) NOT NULL COMMENT '客户编码',
  `customer_name` varchar(64) NOT NULL COMMENT '客户名称',
  `customer_short_name` varchar(32) DEFAULT NULL COMMENT '客户简称',
  `trade_type` varchar(20) DEFAULT '' COMMENT '行业类型（机械加工、机械设备、家电....）',
  `province_id` int(20) DEFAULT NULL COMMENT '所属省id',
  `province_name` varchar(64) DEFAULT NULL COMMENT '所属省名称',
  `city_id` int(20) DEFAULT NULL COMMENT '所属市id',
  `city_name` varchar(64) DEFAULT NULL COMMENT '所属市名称',
  `county_id` int(20) DEFAULT NULL COMMENT '所属区县id',
  `county_name` varchar(64) DEFAULT NULL COMMENT '所属区县名称',
  `street_id` int(20) DEFAULT NULL COMMENT '所属街道id',
  `street_name` varchar(64) DEFAULT NULL COMMENT '所属街道名称',
  `customer_address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `contact_name` varchar(32) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系人电话',
  `main_business` varchar(255) DEFAULT NULL COMMENT '主营业务',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `channel_type` varchar(6) DEFAULT NULL COMMENT '渠道分类（内部、外部）',
  `channel_code` varchar(10) DEFAULT NULL COMMENT '渠道商编码',
  `channel_name` varchar(64) DEFAULT NULL COMMENT '渠道商名称',
  `customer_status` char(2) DEFAULT NULL COMMENT '客户状态（预留）',
  `active_flag` char(1) DEFAULT '1' COMMENT '在用标志（1在用；0停用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `impower_type` varchar(2) DEFAULT '1' COMMENT '授权类型（0-本地部署；1-pssa部署）',
  `databaseName` varchar(30) DEFAULT NULL COMMENT '数据库',
  `datasource` varchar(10) DEFAULT NULL COMMENT '数据源',
  `ext1` varchar(255) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(255) DEFAULT NULL COMMENT '扩展字段2',
  `logo` longblob COMMENT '企业LOGO',
  `send_flag` varchar(1) DEFAULT 'N' COMMENT '是否下发（Y/N）',
  `send_time` datetime DEFAULT NULL COMMENT '下发时间',
  `sender` varchar(20) DEFAULT NULL COMMENT '下发人',
  `scope` varchar(100) DEFAULT NULL COMMENT '规模',
  `auth_user_num` int(11) DEFAULT NULL COMMENT '授权用户数',
  `sales_manager` varchar(50) DEFAULT NULL COMMENT '销售经理',
  `sales_phone` varchar(20) DEFAULT NULL COMMENT '销售电话',
  `create_db_state` int(255) DEFAULT '0' COMMENT '建库状态0：未建库，1:建库中，2：建库成功，3：建库失败',
  `create_db_time` datetime DEFAULT NULL COMMENT '建库时间',
  `create_db_by` varchar(50) DEFAULT NULL COMMENT '建库人',
  `create_db_exception` varchar(5000) DEFAULT NULL COMMENT '建库失败异常原因',
  `v_id` bigint(20) DEFAULT '21' COMMENT '所属版本ID',
  `cuba_id` varchar(50) DEFAULT '' COMMENT 'kubaid',
  `use_state` int(11) NOT NULL DEFAULT '1' COMMENT '使用状态（0:未开通，1：使用，2：停用）',
  `customer_valid_date` datetime DEFAULT NULL COMMENT '试用有效期',
  `research_url` varchar(255) DEFAULT NULL COMMENT '调研地址',
  `use_type` char(1) NOT NULL DEFAULT '0' COMMENT '用户状态（0：试用，1：订阅，2：演示）',
  `customer_type` varchar(10) NOT NULL DEFAULT '5' COMMENT '客户状态（数据字典hyzz_customer_type）',
  `chance_owner` varchar(50) DEFAULT NULL COMMENT '机会所有人',
  `chance_owner_name` varchar(50) DEFAULT NULL COMMENT '机会所有人昵称',
  `charger` varchar(50) DEFAULT NULL COMMENT '负责人',
  `charger_name` varchar(50) DEFAULT NULL COMMENT '负责人昵称',
  `use_remark` varchar(255) DEFAULT NULL COMMENT '客户使用情况',
  `use_follow_strategy` varchar(255) DEFAULT NULL COMMENT '用户跟进策略',
  `customer_source` varchar(10) DEFAULT NULL COMMENT '客户来源',
  `ty_order_no` varchar(50) DEFAULT NULL COMMENT '天云订单号',
  `ty_purchase_days` int(6) DEFAULT NULL COMMENT '天云购买天数',
  `subscribe_date` date DEFAULT NULL COMMENT '订阅时间',
  `customer_product_type` varchar(1) NOT NULL DEFAULT '0' COMMENT '客户产品类型(0:MOM,1:微应用)',
  `industry_attribute` varchar(10) NOT NULL DEFAULT '0' COMMENT '行业属性(0:MOM,1:电子行业)',
  `invite_code` varchar(20) DEFAULT NULL COMMENT '邀请码',
  `ip_address` varchar(255) DEFAULT NULL COMMENT 'ip地址',
  `mac_address` varchar(255) DEFAULT NULL COMMENT 'mac地址',
  PRIMARY KEY (`row_id`) USING BTREE,
  UNIQUE KEY `customer_code` (`customer_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1018 DEFAULT CHARSET=utf8mb4 COMMENT='客户调查报告';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_customer_application 结构
CREATE TABLE IF NOT EXISTS `hyzz_customer_application` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `customer_code` varchar(50) DEFAULT NULL COMMENT '租户编码',
  `app_id` varchar(50) DEFAULT NULL COMMENT '应用id',
  `app_name` varchar(50) DEFAULT NULL COMMENT '应用名称',
  `customer_valid_date` datetime DEFAULT NULL COMMENT '应用有效期',
  `customer_product_type` int(2) DEFAULT NULL COMMENT '客户产品类型(0:MOM,1:微应用)',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `use_status` char(1) NOT NULL DEFAULT '0' COMMENT '应用使用状态（0：试用，1：正式使用）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_application` (`customer_code`,`app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=254 DEFAULT CHARSET=utf8 COMMENT='企业订购应用表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_customer_user 结构
CREATE TABLE IF NOT EXISTS `hyzz_customer_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `customer_code` varchar(20) DEFAULT NULL COMMENT '客户编号',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) DEFAULT '20' COMMENT '用户类型（20企业用户）',
  `account_type` varchar(2) DEFAULT '2' COMMENT '账号类型（1-企业管理员；2普通账号）',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(20) NOT NULL DEFAULT '' COMMENT '手机号码',
  `sex` char(1) DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` text COMMENT '头像地址',
  `password` varchar(100) DEFAULT '' COMMENT '密码',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `role_ids` varchar(255) DEFAULT NULL COMMENT '用户角色ID（多个逗号隔开）-暂仅用于企业管理员角色',
  `valid_date` datetime DEFAULT NULL COMMENT '用户有效期',
  `play_user` varchar(30) DEFAULT NULL COMMENT '扮演用户',
  `uuc_user_id` bigint(20) DEFAULT NULL COMMENT 'uuc userid',
  `is_authentication` int(2) DEFAULT '0' COMMENT '用户是否认证，0未认证，1已认证',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `uni_uuc_id` (`uuc_user_id`),
  KEY `idx_customer_code` (`customer_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=123041 DEFAULT CHARSET=utf8mb4 COMMENT='客户用户信息表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_customer_user_mapping 结构
CREATE TABLE IF NOT EXISTS `hyzz_customer_user_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `customer_id` bigint(20) NOT NULL COMMENT '租户ID',
  `customer_code` varchar(20) CHARACTER SET utf8 NOT NULL COMMENT '租户编码',
  `account_type` varchar(2) CHARACTER SET utf8 DEFAULT NULL COMMENT '账号类型（1-企业管理员；2普通账号）',
  `valid_date` datetime DEFAULT NULL COMMENT '用户有效期',
  `play_user` varchar(30) DEFAULT NULL COMMENT '扮演用户',
  `customer_product_type` int(2) NOT NULL COMMENT '客户产品类型(0:MOM,1:微应用)',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_mapping` (`user_id`,`customer_id`) USING BTREE COMMENT '映射唯一索引\n不允许同一企业在客户表存在不同应用(租户有效期问题)'
) ENGINE=InnoDB AUTO_INCREMENT=471 DEFAULT CHARSET=utf8mb4 COMMENT='用户租户映射表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_custom_job 结构
CREATE TABLE IF NOT EXISTS `hyzz_custom_job` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `system_code` varchar(32) DEFAULT NULL COMMENT '系统编码',
  `task_desc` varchar(255) DEFAULT NULL COMMENT '任务描述',
  `db` varchar(10) NOT NULL COMMENT '数据源',
  `schema` varchar(50) NOT NULL COMMENT '数据库',
  `tenant_code` varchar(20) DEFAULT NULL COMMENT '租户编码',
  `cron` varchar(150) NOT NULL COMMENT '定时任务表达式',
  `task_advance_time` int(2) DEFAULT '0' COMMENT '提前时间（默认值0）分钟',
  `task_next_time` bigint(50) NOT NULL COMMENT '下次执行时间',
  `task_last_time` datetime DEFAULT NULL COMMENT '上次执行时间',
  `topic` varchar(50) NOT NULL COMMENT 'mq主题',
  `tag` varchar(20) NOT NULL COMMENT 'mq标签',
  `task_param` tinytext COMMENT '执行参数',
  `task_status` int(2) DEFAULT '1' COMMENT '调度状态：0-停止，1-运行',
  `miss_time` int(2) NOT NULL DEFAULT '0' COMMENT '错过时间  单位分钟 ',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `rownum` int(20) DEFAULT NULL COMMENT '伪列',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_task_next_time` (`task_next_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4;

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_device_user_bind 结构
CREATE TABLE IF NOT EXISTS `hyzz_device_user_bind` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `device_id` varchar(64) NOT NULL COMMENT '设备ID',
  `user_name` varchar(32) NOT NULL COMMENT '用户账号',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '可用状态（0：不可用，1：可用）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `device_id_idx` (`device_id`) USING BTREE,
  KEY `user_id_idx` (`user_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4938 DEFAULT CHARSET=utf8mb4 COMMENT='设备与用户绑定关系表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_faq_pair_record 结构
CREATE TABLE IF NOT EXISTS `hyzz_faq_pair_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `question` varchar(255) NOT NULL COMMENT '问题',
  `answer` text NOT NULL COMMENT '回答',
  `role_code` varchar(20) DEFAULT NULL COMMENT '角色编码',
  `weight` decimal(2,1) NOT NULL DEFAULT '1.0' COMMENT '权重',
  `answer_type` varchar(10) NOT NULL COMMENT '回答类型,文本TEXT,链接LINK,图片PICTURE,视频VIDEO',
  `customer_code` varchar(40) DEFAULT NULL,
  `customer_sign` int(11) DEFAULT '1' COMMENT '客户产品类型(0:MOM,1:微应用)',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COMMENT='智能客服问答对表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_files 结构
CREATE TABLE IF NOT EXISTS `hyzz_files` (
  `row_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `file_table_name` varchar(40) NOT NULL COMMENT '相关表名',
  `file_table_id` varchar(40) NOT NULL COMMENT '相关表row_id',
  `file_name` varchar(200) NOT NULL COMMENT '文件名称',
  `file_path` varchar(500) NOT NULL COMMENT '文件上传路径',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `file_type` varchar(10) DEFAULT '1' COMMENT '附件类型(1、图片 2、音频 3、视频 4、文件)',
  `file_size` varchar(10) DEFAULT NULL COMMENT '附件大小',
  `file_mins` varchar(10) DEFAULT NULL COMMENT '附件时长(音视频使用)',
  `active_flag` varchar(4) DEFAULT '1' COMMENT '在用标志0-删除；1-在用',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `ext1` varchar(10) DEFAULT NULL COMMENT '扩展字段1',
  `ext2` varchar(10) DEFAULT NULL COMMENT '扩展字段2',
  `ext3` varchar(10) DEFAULT NULL COMMENT '扩展字段3',
  PRIMARY KEY (`row_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=688 DEFAULT CHARSET=utf8mb4 COMMENT='附件表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_inverse_index 结构
CREATE TABLE IF NOT EXISTS `hyzz_inverse_index` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `index` varchar(20) NOT NULL COMMENT '索引值',
  `pinyin` varchar(100) DEFAULT NULL COMMENT '拼音',
  `object_id` bigint(20) NOT NULL COMMENT '对象ID',
  `weight` int(11) NOT NULL DEFAULT '3' COMMENT '权重',
  `index_source` varchar(10) NOT NULL COMMENT '索引来源(问答faq/....)',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=382 DEFAULT CHARSET=utf8mb4 COMMENT='倒排索引表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_micro_application_config 结构
CREATE TABLE IF NOT EXISTS `hyzz_micro_application_config` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `application_name` varchar(64) DEFAULT NULL COMMENT '应用名称',
  `application_sign` varchar(64) CHARACTER SET utf8mb4 NOT NULL COMMENT '应用标识',
  `application_code` varchar(128) CHARACTER SET utf8mb4 NOT NULL COMMENT '应用编码',
  `deleted` char(1) DEFAULT NULL COMMENT '是否删除 0-正常，1-删除',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_applicationCode` (`application_code`) USING BTREE,
  UNIQUE KEY `idx_applicationSign` (`application_sign`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='微应用-应用配置信息';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_micro_miniapp_config 结构
CREATE TABLE IF NOT EXISTS `hyzz_micro_miniapp_config` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `app_id` varchar(64) NOT NULL COMMENT '小程序AppId',
  `app_secret` varchar(128) NOT NULL COMMENT '小程序AppSecret',
  `mini_app_name` varchar(64) DEFAULT NULL COMMENT '小程序名称',
  `application_sign` varchar(16) NOT NULL COMMENT '应用标识（micro_process:在制品库存-微应用）',
  `platform_type` varchar(16) NOT NULL COMMENT '平台类型（wechat：微信，cosmo：卡奥斯）',
  `deleted` char(1) DEFAULT NULL COMMENT '是否删除 0-正常，1-删除',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_appId` (`app_id`),
  UNIQUE KEY `idx_applicationSign` (`application_sign`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='微信小程序配置表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_portal_suggestion 结构
CREATE TABLE IF NOT EXISTS `hyzz_portal_suggestion` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_no` varchar(20) DEFAULT NULL COMMENT '编码',
  `type` varchar(10) NOT NULL COMMENT '类型(数据字典hyzz_suggestion_type)',
  `status` varchar(255) NOT NULL COMMENT '状态(数据字典hyzz_suggestion_status)',
  `content` varchar(255) DEFAULT NULL COMMENT '问题描述',
  `deal_way` varchar(255) DEFAULT NULL COMMENT '处理方式',
  `deal_result` varchar(255) DEFAULT NULL COMMENT '处理结果',
  `deal_time` datetime DEFAULT NULL COMMENT '处理时间',
  `finish_by` varchar(64) DEFAULT NULL COMMENT '完成人',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `active_flag` char(1) NOT NULL DEFAULT '1' COMMENT '在用标志（1在用；0停用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_by_name` varchar(100) DEFAULT NULL COMMENT '创建者昵称',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `tenant_code` varchar(20) NOT NULL DEFAULT '000013' COMMENT '租户编码',
  `tenant_name` varchar(100) NOT NULL DEFAULT '000013' COMMENT '租户名称',
  `source_plat` varchar(10) NOT NULL DEFAULT 'HYZZ' COMMENT '来源平台(海云智造/微应用)',
  `auto_deal` int(11) NOT NULL DEFAULT '0' COMMENT '是否自动处理0否1是',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=244 DEFAULT CHARSET=utf8mb4 COMMENT='门户的意见反馈表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_portal_suggestion_log 结构
CREATE TABLE IF NOT EXISTS `hyzz_portal_suggestion_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_no` varchar(20) NOT NULL COMMENT '编码',
  `operate_name` varchar(40) NOT NULL COMMENT '操作名称(创建，处理，完成)',
  `operate_result` varchar(255) DEFAULT NULL COMMENT '操作结果',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `active_flag` char(1) DEFAULT '1' COMMENT '在用标志（1在用；0停用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_by_name` varchar(50) DEFAULT NULL COMMENT '创建者昵称',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `customer_code_2` (`order_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=324 DEFAULT CHARSET=utf8mb4 COMMENT='意见反馈日志表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_tenant_job 结构
CREATE TABLE IF NOT EXISTS `hyzz_tenant_job` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `system_code` varchar(32) DEFAULT NULL COMMENT '系统编码',
  `task_desc` varchar(255) DEFAULT NULL COMMENT '任务描述',
  `cron` varchar(50) NOT NULL COMMENT '定时任务表达式',
  `task_next_time` bigint(50) NOT NULL COMMENT '下次执行时间',
  `task_last_time` datetime DEFAULT NULL COMMENT '上次执行时间',
  `topic` varchar(50) NOT NULL COMMENT 'mq主题',
  `tag` varchar(50) NOT NULL COMMENT 'mq标签',
  `task_param` tinytext COMMENT '执行参数',
  `task_status` int(2) DEFAULT '1' COMMENT '调度状态：0-停止，1-运行',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `expand` varchar(255) DEFAULT NULL COMMENT '拓展sql',
  `rownum` int(20) DEFAULT NULL COMMENT '伪列',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_task_next_time` (`task_next_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4;

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_third_interface_log 结构
CREATE TABLE IF NOT EXISTS `hyzz_third_interface_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `request_id` varchar(40) NOT NULL COMMENT '请求唯一码',
  `message_key` varchar(60) DEFAULT NULL COMMENT '消息KEY',
  `tenant_code` varchar(40) DEFAULT NULL COMMENT '租户编码',
  `client_support` varchar(255) DEFAULT NULL,
  `method` varchar(60) DEFAULT NULL COMMENT '请求方法',
  `version` varchar(10) DEFAULT NULL COMMENT '请求方法版本',
  `content` longtext COMMENT '请求消息体',
  `expand_content` longtext COMMENT '扩展数据',
  `request_url` varchar(255) DEFAULT NULL COMMENT '请求地址',
  `request_time` timestamp NULL DEFAULT NULL COMMENT '首次请求时间',
  `response_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最终相应返回时间',
  `send_status` varchar(2) DEFAULT '-1' COMMENT '发送状态(0发送失败 1发送成功 -1未发送)',
  `client_status` varchar(2) DEFAULT '0' COMMENT '0为客户端处理失败 1为客户端处理成功',
  `retry_count` int(11) DEFAULT NULL COMMENT '重试次数',
  `created_by` varchar(40) DEFAULT NULL COMMENT '创建人',
  `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_request_client` (`request_id`,`client_support`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=23186 DEFAULT CHARSET=utf8 COMMENT='调用第三方接口日志表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_third_interface_log_detail 结构
CREATE TABLE IF NOT EXISTS `hyzz_third_interface_log_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `request_id` varchar(60) DEFAULT NULL COMMENT '请求唯一码',
  `message_id` varchar(100) DEFAULT NULL COMMENT '消息ID',
  `message_key` varchar(60) DEFAULT NULL COMMENT '消息KEY',
  `tenant_code` varchar(40) DEFAULT NULL COMMENT '租户编码',
  `client_support` varchar(255) DEFAULT NULL,
  `method` varchar(60) DEFAULT NULL COMMENT '请求方法',
  `version` varchar(10) DEFAULT NULL COMMENT '请求方法版本',
  `request_url` varchar(255) DEFAULT NULL COMMENT '请求地址',
  `request_time` timestamp NULL DEFAULT NULL COMMENT '请求时间',
  `response_time` timestamp NULL DEFAULT NULL COMMENT '请求返回时间',
  `response_body` longtext COMMENT '响应体',
  `send_status` varchar(2) DEFAULT '-1' COMMENT '发送状态(0发送失败 1发送成功 -1未发送)',
  `created_by` varchar(40) DEFAULT NULL COMMENT '创建人',
  `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=23190 DEFAULT CHARSET=utf8 COMMENT='调用第三方接口日志明细表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_third_interface_tenant 结构
CREATE TABLE IF NOT EXISTS `hyzz_third_interface_tenant` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `app_id` varchar(40) DEFAULT NULL COMMENT '应用ID',
  `tenant_code` varchar(40) DEFAULT NULL COMMENT '租户编码',
  `tenant_name` varchar(255) DEFAULT NULL COMMENT '租户名称',
  `client_support` varchar(255) DEFAULT NULL,
  `sign_type` varchar(40) DEFAULT NULL COMMENT '签名类型',
  `public_key` text COMMENT '公钥',
  `private_key` text COMMENT '私钥',
  `request_url` varchar(255) DEFAULT NULL COMMENT '请求地址',
  `auth_info` text COMMENT '外部平台额外的认证信息',
  `created_by` varchar(40) DEFAULT NULL COMMENT '创建人',
  `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_upd_by` varchar(40) DEFAULT NULL COMMENT '更新人',
  `last_upd_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `active_flag` varchar(1) DEFAULT '1',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `UNIQ_TENANT_INFO` (`tenant_code`,`client_support`) USING BTREE COMMENT '应用ID+租户+客户端类型全局唯一'
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8 COMMENT='调用第三方接口租户配置表';

-- 导出  表 im-portal.hyzz_third_interface_method_mapping 结构
-- 说明：三方接口异步调用链路的方法映射表（消费者按 method+version+tenant_code 查询可调用渠道），
-- 与 hyzz_third_interface_tenant（租户对接配置）通过 interface_tenant_id 关联。
CREATE TABLE IF NOT EXISTS `hyzz_third_interface_method_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(40) NOT NULL COMMENT '租户编码',
  `client_support` varchar(255) NOT NULL COMMENT '客户端类型（如 thirdCommonClient / yonyouU8）',
  `method` varchar(64) NOT NULL COMMENT '方法名',
  `version` varchar(16) NOT NULL COMMENT '方法版本',
  `request_type` varchar(16) DEFAULT NULL COMMENT '请求方式（POST/GET）',
  `callback` varchar(4) DEFAULT NULL COMMENT '是否需要回调（0/1）',
  `callback_topic` varchar(64) DEFAULT NULL COMMENT '回调主题',
  `callback_tag` varchar(64) DEFAULT NULL COMMENT '回调标签',
  `interface_tenant_id` bigint(20) DEFAULT NULL COMMENT '关联 hyzz_third_interface_tenant.id',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '状态（0-停用，1-启用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_method` (`method`,`version`,`tenant_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方接口方法映射表';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_wechat_config 结构
CREATE TABLE IF NOT EXISTS `hyzz_wechat_config` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `app_id` varchar(64) DEFAULT NULL COMMENT '公众号AppId',
  `app_secret` varchar(128) DEFAULT NULL COMMENT '公众号密钥',
  `app_name` varchar(64) DEFAULT NULL COMMENT '公众号名称',
  `app_type` varchar(32) DEFAULT NULL COMMENT '公众号类型',
  `main_info` varchar(32) DEFAULT NULL COMMENT '主体信息',
  `server_token` varchar(128) DEFAULT NULL COMMENT '微信服务器接入认证token',
  `server_encode_aeskey` varchar(128) DEFAULT NULL COMMENT '微信服务器接入消息密钥',
  `customer_code` varchar(50) NOT NULL COMMENT '所属企业编码',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_customerCode` (`customer_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='微信公众号配置信息';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_wechat_user 结构
CREATE TABLE IF NOT EXISTS `hyzz_wechat_user` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `openid` varchar(255) DEFAULT NULL COMMENT '用户的标识，对当前公众号唯一',
  `nickname` varchar(128) DEFAULT NULL COMMENT '用户昵称',
  `sex` tinyint(1) unsigned DEFAULT NULL COMMENT '用户的性别（1：男性，2：女性，0：未知）',
  `city` varchar(64) DEFAULT NULL COMMENT '普通用户个人资料填写的城市',
  `province` varchar(64) DEFAULT NULL COMMENT '用户个人资料填写的省份',
  `country` varchar(16) DEFAULT NULL COMMENT '国家，如中国为CN',
  `head_img_url` text COMMENT '用户头像地址',
  `subscribe` varchar(2) DEFAULT NULL COMMENT '用户是否订阅该公众号标识(0：此用户没有关注该公众号拉取不到其余信息，1：已关注可正常拉取用户信息)',
  `language` varchar(32) DEFAULT NULL COMMENT '用户的语言，简体中文为zh_CN',
  `subscribe_time` timestamp NULL DEFAULT NULL COMMENT '用户关注时间，为时间戳。如果用户曾多次关注，则取最后关注时间',
  `unionid` varchar(32) DEFAULT NULL COMMENT '只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。',
  `remark` varchar(128) DEFAULT NULL COMMENT '公众号运营者对粉丝的备注，公众号运营者可在微信公众平台用户管理界面对粉丝添加备注',
  `groupid` varchar(32) DEFAULT NULL COMMENT '用户所在的分组ID（暂时兼容用户分组旧接口）',
  `tagid_list` varchar(64) DEFAULT NULL COMMENT '用户被打上的标签ID列表',
  `subscribe_scene` varchar(32) DEFAULT NULL COMMENT '返回用户关注的渠道来源，ADD_SCENE_SEARCH 公众号搜索，ADD_SCENE_ACCOUNT_MIGRATION 公众号迁移，ADD_SCENE_PROFILE_CARD 名片分享，ADD_SCENE_QR_CODE 扫描二维码，ADD_SCENE_PROFILE_LINK 图文页内名称点击，ADD_SCENE_PROFILE_ITEM 图文页右上角菜单，ADD_SCENE_PAID 支付后关注，ADD_SCENE_WECHAT_ADVERTISEMENT 微信广告，ADD_SCENE_REPRINT 他人转载 ，ADD_SCENE_LIVESTREAM 视频号直播， ADD_SCENE_CHANNELS 视频号, ADD_SCENE_OTHERS 其他',
  `qr_scene` varchar(128) DEFAULT NULL COMMENT '二维码扫码场景（开发者自定义）',
  `qr_scene_str` varchar(128) DEFAULT NULL COMMENT '二维码扫码场景描述（开发者自定义）',
  `last_login_time` datetime DEFAULT NULL COMMENT '上次登录时间',
  `last_login_ip` varchar(16) DEFAULT NULL COMMENT '上次登录IP',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_openId` (`openid`) USING BTREE,
  KEY `idx_unionid` (`unionid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COMMENT='微信用户信息';

-- 数据导出被取消选择。

-- 导出  表 im-portal.hyzz_wechat_user_bind 结构
CREATE TABLE IF NOT EXISTS `hyzz_wechat_user_bind` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `openid` varchar(255) DEFAULT NULL COMMENT '用户的标识，对当前公众号唯一',
  `user_name` varchar(32) DEFAULT NULL COMMENT '用户账号（司机是手机号，非司机账号）',
  `user_identity` varchar(255) DEFAULT NULL COMMENT '用户身份(0：司机，1：供应商，2：仓管员，3：TE货代)',
  `driver_name` varchar(32) DEFAULT NULL COMMENT '司机名',
  `tenant_code` varchar(50) NOT NULL COMMENT '所属租户编码',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_openid` (`openid`) USING BTREE,
  KEY `idx_tenantCode` (`tenant_code`) USING BTREE,
  KEY `idx_userName` (`user_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=864 DEFAULT CHARSET=utf8mb4 COMMENT='微信与企业用户绑定信息';

-- 数据导出被取消选择。

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

-- ============================================================
-- 微信小程序配置（必配：开源部署后必须执行）
-- ------------------------------------------------------------
-- hyzz_micro_miniapp_config 表结构已在上方创建（只建表、无数据），
-- 本段提供初始化模板：部署时请将占位符替换为你的小程序真实信息，
-- 否则后端②（hhim-third-platform）无法解析小程序身份，会导致：
--   · 微信一键登录 / 手机号授权登录失败
--   · 邀请二维码 / 小程序码生成失败（接口返回 500）
--
-- 填写说明：
--   1. app_id / app_secret：微信公众平台（小程序 → 开发管理 → 开发设置）获取；
--   2. app_id 必须与前端 cosmo-hhim-micro/hhim-micro-app/src/manifest.json 中 mp-weixin.appid 一致；
--   3. application_sign 为应用标识，必须与前端请求头保持一致：micro_process；
--   4. platform_type 必须为 wechatMiniApp（代码按此值匹配，勿写成注释里的 wechat）；
--   5. 若表内已有数据（如演示环境残留），请先 UPDATE 或 DELETE 再执行，避免唯一键冲突。
-- ------------------------------------------------------------
INSERT INTO `hyzz_micro_miniapp_config`
(`app_id`, `app_secret`, `mini_app_name`, `application_sign`, `platform_type`, `deleted`, `create_by`, `create_time`)
VALUES
('请替换为你的小程序AppId', '请替换为你的小程序AppSecret', '请替换为小程序名称', 'micro_process', 'wechatMiniApp', '0', 'admin', NOW());
