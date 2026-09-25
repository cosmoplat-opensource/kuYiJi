/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
/**
 * DDD: interfaces 用户界面层（或表示层）  最顶层
 * 负责向用户显示信息和解释用户命令
 * 请求应用层以获取用户所需要展现的数据
 * 发送命令给应用层要求其执行某个用户命令(实现某个业务逻辑,比如用户要进行入库操作等)
 * 它的主要工作就是将一个用户请求委派给一个或多个Service进行处理,也就是我们常说的Controller。
 */
package com.cosmo.hhim.micro;