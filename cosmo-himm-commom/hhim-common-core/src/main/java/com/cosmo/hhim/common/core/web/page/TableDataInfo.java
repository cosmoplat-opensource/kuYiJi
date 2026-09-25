/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.web.page;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表格分页数据对象
 *
 * @author cosmo-hhim-open Team
 */
public class TableDataInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 列表数据 */
    private List<?> rows;

    /** 消息状态码 */
    private int code;

    /** 消息内容 */
    private String msg;

    private Map<String,Object> data;

    /**
     * 表格数据对象
     */
    public TableDataInfo()
    {
    }

    /**
     * 分页
     *
     * @param list 列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<?> list, long total)
    {
        this.rows = list;
        this.total = total;
    }
    /**
     * 分页
     *
     * @param list 列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<?> list, long total,int code,String msg)
    {
        this.rows = list;
        this.total = total;
        this.code = code;
        this.msg = msg;
    }
    /**
     * 分页
     *
     * @param list 列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<?> list, long total,int code,String msg,Map<String,Object> data)
    {
        this.rows = list;
        this.total = total;
        this.code = code;
        this.msg = msg;
        this.setData(data);
    }
    /**
     * 分页
     *
     * @param list 列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<?> list, long total,int code,String msg,Object data)
    {
        this.rows = list;
        this.total = total;
        this.code = code;
        this.msg = msg;
        this.setData(data);
    }

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public List<?> getRows()
    {
        return rows;
    }

    public void setRows(List<?> rows)
    {
        this.rows = rows;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void setData(Object data) {
        if (data != null) {
            this.data = JSONObject.parseObject(JSONObject.toJSONString(data), Map.class);
        } else {
            this.data = new HashMap<>();
        }
    }
}