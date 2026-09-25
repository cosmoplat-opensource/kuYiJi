/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 工序节点信息
 * @date 2023/2/22 13:22
 */
@Data
public class ProcessNode {

    /**
     * 节点深度, 存在并序的情况，所以需要一个深度
     */
    private Integer depth;

    /**
     * 当前序列码
     */
    private String operateProcessSeq;

    /**
     * 前工序的序列码
     */
    private String preProcessSeq;

    /**
     * 尾序标示
     */
    private String isLastProcess;

    /**
     * 下一个节点, 可能大于两个
     */
    private List<ProcessNode> childrenProcess;
}
