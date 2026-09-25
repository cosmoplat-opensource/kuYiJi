/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.excel;

import java.util.List;

public class ExcelFunction {

    private String function;//公式

    private Object[] data;//数据集

    private String key;//唯一建

    private Object value;//得到的结果
    
    private List<String> errorCodes;//错误码

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Object[] getData() {
        return data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ExcelFunction(String function, Object[] data, String key) {
        this.function = function;
        this.data = data;
        this.key = key;
    }

    public ExcelFunction(String function, Object[] data) {
        this.function = function;
        this.data = data;
    }

    public ExcelFunction(String function) {
        this.function = function;
    }

    public ExcelFunction() {

    }

    public List<String> getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(List<String> errorCodes) {
        this.errorCodes = errorCodes;
    }
}
