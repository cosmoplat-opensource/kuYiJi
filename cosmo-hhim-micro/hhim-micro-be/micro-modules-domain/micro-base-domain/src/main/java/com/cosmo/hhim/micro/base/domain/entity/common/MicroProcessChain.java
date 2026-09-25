/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 工艺链对象 micro_process_chain
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-20
 */
@Data
public class MicroProcessChain implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 工艺id(technology_id)
     */
    private Long techId;

    /**
     * 工序id
     */
    private Long processId;

    /**
     * 工序seq
     */
    private String processSeq;
    private String processCode;
    private String processName;

    /**
     * 父工序ID
     */
    private Long parentProcessId;
    private String parentProcessSeq;

    /**
     * 是否最后一道工序1不是,0是
     */
    private String isLastProcess;
    /**
     * 工艺顺序
     */
    private Integer sort;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

    /**
     * 最后修改人
     */
    private String lastUpdBy;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdDate;

    /**
     * 激活标记1是0否
     */
    private String activeFlag;

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        MicroProcessChain that = (MicroProcessChain) o;
        return Objects.equals(processId, that.processId) && Objects.equals(processSeq, that.processSeq) && Objects.equals(processCode, that.processCode) && Objects.equals(processName, that.processName) && Objects.equals(isLastProcess, that.isLastProcess);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processId, processSeq, processCode, processName, isLastProcess);
    }
}
