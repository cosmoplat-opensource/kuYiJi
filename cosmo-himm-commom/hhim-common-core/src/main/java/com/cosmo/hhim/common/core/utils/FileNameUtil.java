/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

/**
 */
public class FileNameUtil {
    /**
     * 获取文件前缀
     *
     * @param fileName
     * @return
     */
    public static String getFilePrefix(String fileName) {
        if(fileName == null || "".equals(fileName)){
            return "";
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /**
     * 获取文件后缀
     *
     * @param fileName
     *            文件名称
     * @return
     */
    public static String getFileSufix(String fileName) {
        if(fileName == null || "".equals(fileName)){
            return "";
        }
        //从最后一个点之后截取字符串
        return fileName.substring(fileName.lastIndexOf(".")+1);
    }

    /**
     * 获取随机文件名
     * @param fileName
     * @return
     */
    public static String getRandomFileName(String fileName){
        if(fileName==null){
            return "";
        }
        return getFilePrefix(fileName)+"-"+System.currentTimeMillis()+"."+getFileSufix(fileName);
    }
}
