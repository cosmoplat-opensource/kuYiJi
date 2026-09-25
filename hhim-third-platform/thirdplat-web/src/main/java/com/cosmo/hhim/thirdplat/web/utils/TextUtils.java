/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.system.ApplicationHome;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 分词工具类
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
public class TextUtils {
    private static final JiebaSegmenter segmenter; // NOPMD - PMD误判：非编译期常量，是实例对象
    static {
        segmenter = new JiebaSegmenter();
    }

    /**
     * 分词
     *
     * @param text 文本
     * @return 分词结果
     */
    public static List<String> wordSegmentation(String text) {
        return segmenter.sentenceProcess(text);
    }


    public static void loadDict() {
        String dictPath = getDictPath();
        Path path = Paths.get(dictPath);
        WordDictionary.getInstance().loadUserDict(path);
    }

    /**
     * 获取当前词库地址
     *
     * @return
     */
    public static String getDictPath() {
        ApplicationHome home = new ApplicationHome(TextUtils.class);
        File jarFile = home.getSource();
        String jarDir = jarFile.getParent();
        return jarDir + File.separator + "micro.dict";
    }
}
