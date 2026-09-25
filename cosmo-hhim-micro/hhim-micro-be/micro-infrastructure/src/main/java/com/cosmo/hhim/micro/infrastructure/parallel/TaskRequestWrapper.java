/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.parallel;

import lombok.Builder;

import java.util.Objects;

/**
 * 并行任务请求包装类
 *
 * @author cosmo-hhim-open Team
 */
@Builder
public class TaskRequestWrapper<T> {

    private T bizContent;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskRequestWrapper<?> that = (TaskRequestWrapper<?>) o;
        return Objects.equals(bizContent, that.bizContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bizContent);
    }

    @Override
    public String toString() {
        return "TaskRequestWrapper{" +
                "bizContent=" + bizContent +
                '}';
    }
}
