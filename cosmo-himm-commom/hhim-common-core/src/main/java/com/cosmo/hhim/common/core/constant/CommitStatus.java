/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.constant;

/**
 * 事务提交状态状态
 *
 * @author cosmo-hhim-open Team
 */
public class CommitStatus {
    /**
     * 事务提交
     */
    public static final int COMMITTED = 0;


    /**
     * 事务回滚
     */
    public static final int ROLLED_BACK = 1;


    /**
     * 不限制，提交和回滚都执行
     */
    public static final int UNLIMITED = 2;


}
