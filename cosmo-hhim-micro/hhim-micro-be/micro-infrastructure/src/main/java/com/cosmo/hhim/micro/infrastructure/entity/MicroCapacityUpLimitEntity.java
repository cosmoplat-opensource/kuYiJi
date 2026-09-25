/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.entity;

import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * 产能上限实体
 *
 * @author cosmo-hhim-open Team
 */
public class MicroCapacityUpLimitEntity implements Serializable {

    private String operateProcessSeq;
    private String productSeq;
    private String userId;

    public String getOperateProcessSeq() {
        return operateProcessSeq;
    }

    public String getProductSeq() {
        return productSeq;
    }

    public String getUserId() {
        return userId;
    }


    public static class Builder {

        private String operateProcessSeq;
        private String productSeq;
        private String userId;

        public Builder withOperateProcessSeq(String operateProcessSeq) {
            Assert.notNull(operateProcessSeq, "操作工序不能为空");
            this.operateProcessSeq = operateProcessSeq;
            return this;
        }

        public Builder withProductSeq(String productSeq) {
            Assert.notNull(productSeq, "操作工序不能为空");
            this.productSeq = productSeq;
            return this;
        }

        public Builder withUserId(String userId) {
            Assert.notNull(userId, "用户id不能为空");
            this.userId = userId;
            return this;
        }


        public MicroCapacityUpLimitEntity build() {
            MicroCapacityUpLimitEntity microCapacityUpLimit = new MicroCapacityUpLimitEntity();
            microCapacityUpLimit.operateProcessSeq = operateProcessSeq;
            microCapacityUpLimit.productSeq = productSeq;
            microCapacityUpLimit.userId = userId;
            return microCapacityUpLimit;
        }

    }

}
