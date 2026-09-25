/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.runner;

import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzUserDictService;
import com.cosmo.hhim.thirdplat.web.utils.TextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 加载用户自定义词典(分词使用)
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class InitUserDictRunner implements ApplicationRunner {
    @Autowired
    private IHyzzUserDictService userDictService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("query db and generate micro.dict file");
        //加载智能问答索引标签表中所有的标签,写入到dict文件
        try {
            userDictService.loadDict();
            //读取dict文件
            log.info("load micro.dict file to jieba lexicon");
            TextUtils.loadDict();
            log.info("load micro.dict success!!!!");
        } catch (Exception e) {
            log.warn("Failed to load micro.dict:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
    }
}
