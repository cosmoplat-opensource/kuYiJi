# log包使用说明

## 1.功能一：操作日志异步DB记录（责任人：张耀晖）
使用参考项目：在制品库存-微应用

### 第一步：pom引入common-log包

### 第二步：添加表

```sql
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int(2) DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `oper_platform_source` varchar(16) DEFAULT '0' COMMENT '操作来源平台',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(4000) DEFAULT '' COMMENT '请求参数',
  `json_result` longtext COMMENT '返回参数',
  `status` int(1) DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `belong_sys` varchar(64) DEFAULT NULL COMMENT '隶属系统',
  `tenant_code` varchar(20) NOT NULL COMMENT '租户编码',
  PRIMARY KEY (`oper_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录'
```

### 第三步：启动类添加注解@EnableLogAspect

### 第四步：nacos配置中心 mybatis扫描xml文件配置调整

```yml
# mybatis配置
mybatis:
  # 配置mapper的扫描，找到所有的mapper.xml映射文件
  mapperLocations: classpath*:mapper/**/*.xml
```

注意：classpath后需要加`*`，作用是扫描当前项目所以依赖的jar包中的所有classpath。

### 第五步：在需要记录日志的核心方法上添加@Log标识