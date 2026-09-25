/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.parallel;

import java.util.Objects;

/**
 * 异步并行任务返回实体类
 * 业务所需的返回实体需要继承该类
 *
 * @author cosmo-hhim-open Team
 */
public class TaskRespEntity<T> {

    /**
     * 区分返回的唯一标记
     */
    private String mark;
    /**
     * 返回的data
     */
    private T data;

    public TaskRespEntity(String mark, T data) {
        this.mark = mark;
        this.data = data;
    }

    public String getMark() {
        return mark;
    }

    public T getData() {
        return data;
    }


    @Override
    public String toString() {
        return "TaskRespEntity{" +
                "mark='" + mark + '\'' +
                ", data=" + data +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskRespEntity<?> that = (TaskRespEntity<?>) o;
        return Objects.equals(mark, that.mark) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mark, data);
    }
}