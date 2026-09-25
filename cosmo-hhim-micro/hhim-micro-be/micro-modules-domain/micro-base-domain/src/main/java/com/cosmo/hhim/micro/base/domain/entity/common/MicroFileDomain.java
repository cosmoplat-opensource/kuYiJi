/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class MicroFileDomain implements Serializable { 
    private String fileName;
    private String fileType;
    private String fileSize;
    private String filePath;
}
