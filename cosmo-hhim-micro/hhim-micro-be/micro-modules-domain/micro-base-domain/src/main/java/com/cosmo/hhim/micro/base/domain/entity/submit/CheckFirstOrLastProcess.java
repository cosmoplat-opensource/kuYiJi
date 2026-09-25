/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 我是首序或尾序的判断结果
 * @date 2022/12/25 22:05
 */
@Data
public class CheckFirstOrLastProcess {

    private String processSeq;

    private String processCode;

    private String processName;

    /**
     * 是否互斥 ： 0 - 互斥 1 - 不互斥
     */
    private String isOrNotMutex;

    /********************
     * 既是首序又是尾序的情况
     ********************/

    /**
     * 首序相关信息
     */
    private String firstProcessSeq;

    private String firstProcessCode;

    private String firstProcessName;

    /**
     * 尾序相关信息
     */
    private String lastProcessSeq;

    private String lastProcessCode;

    private String lastProcessName;

}
