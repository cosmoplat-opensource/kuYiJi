/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.domain;

import com.cosmo.hhim.common.core.constant.Constants;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import lombok.Data;

/**
 * 操作消息提醒
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class WmsFeignResult<T> implements Serializable {

    private static final String SUCCESSMSG = "操作成功";

    private Integer code;
    private String msg;
    private T data;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 列表数据
     */
    private List<T> rows;

    public WmsFeignResult() {
        super();
    }

    public WmsFeignResult(T t) {
        super();
        this.data = t;
        this.code = Constants.SUCCESS;
        this.msg = SUCCESSMSG;
    }


    public WmsFeignResult(List<T> t, int total) {
        super();
        this.rows = t;
        this.total = total;
        this.code = Constants.SUCCESS;
        this.msg = SUCCESSMSG;
    }

    /**
     * 创建成功的WmsFeignResult对象.
     *
     * @param data 返回的数据
     * @return 包含成功标识的WmsFeignResult对象
     */
    public static <T> WmsFeignResult<T> success(T data) {
        WmsFeignResult result = new WmsFeignResult<>(data);
        result.setCode(Constants.SUCCESS);
        result.setMsg(SUCCESSMSG);
        return result;
    }

    /**
     * 创建成功的分页WmsFeignResult对象.
     *
     * @param list 返回的数据
     * @return 包含成功标识的WmsFeignResult对象
     */
    public <T> WmsFeignResult<T> success(List<T> list, int total) {
        WmsFeignResult result = new WmsFeignResult<>(list, total);
        result.setCode(Constants.SUCCESS);
        result.setTotal(total);
        result.setRows(rows);
        result.setMsg(SUCCESSMSG);
        return result;
    }

    /**
     * 创建失败的WmsFeignResult对象.
     *
     * @param errorCode 错误码
     * @param msg       错误提示信息
     * @return 包含失败标识的WmsFeignResult对象
     */
    public static <T> WmsFeignResult<T> fail(int errorCode, String msg) {
        WmsFeignResult result = new WmsFeignResult<>();
        result.setCode(Constants.FAIL);
        result.setCode(errorCode);
        result.setMsg(msg);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WmsFeignResult<?> that = (WmsFeignResult<?>) o;
        return Objects.equals(code, that.code)
                && Objects.equals(msg, that.msg)
                && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {

        return Objects.hash(code, msg, data);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WmsFeignResult{");
        sb.append(", code=").append(code);
        sb.append(", msg='").append(msg).append('\'');
        sb.append(", data=").append(data);
        sb.append(", total=").append(total);
        sb.append(", rows=").append(rows);
        sb.append('}');
        return sb.toString();
    }
}
