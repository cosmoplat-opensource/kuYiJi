/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.domin;

import javax.validation.constraints.Pattern;


/**
 *获取下载地址（批量）
 */
public class SysFilePreSign {
    /** 文件名：仅允许常规文件名字符（字母数字 . _ - / @），禁止 HTML 特殊字符与空白 */
    @Pattern(regexp = "^[a-zA-Z0-9._/@-]{1,255}$", message = "文件名包含非法字符")
    private String fileName;
    private String path;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
