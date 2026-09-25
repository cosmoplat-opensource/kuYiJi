/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.excel;

import cn.afterturn.easypoi.handler.inter.IExcelVerifyHandler;
import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * 导入参数
 */
@Data
@Builder
public class AsyncImportParam {

    private InputStream inputStream;

    /**
     * 导入的实体类
     */
    private Class<?> pojoClass;
    /**
     * 验证拦截器
     */
    private IExcelVerifyHandler verifyHandler;

    /**
     * modelName
     */
    private String modelName;
    /**
     * 表头行数（占用行数）
     */
    private int headerNum=1;
    /**
     * 标题行数（占用行数）
     */
    private int titleNum=1;
    /**
     * 第几个sheet页
     */
    private int sheetNum;

    /**
     * 是否上传文件服务器
     */
    private boolean uploadFlag = true;
    /**
     * 模板地址
     */
    private String filePath;

    private Integer[] sheetNums= new Integer[]{0};

    /**
     * 导出模板的map key
     */
    private String key;

    private int importPageSize;

    /**
     * 导入限制条数
     */
    private int importRowNum=20000;


    private Map<String, Object> map;

    private List<ExportParam> list;



}
