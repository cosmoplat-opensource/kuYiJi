/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzInverseIndexEntity;
import com.cosmo.hhim.thirdplat.web.utils.TextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * 加载用户字典
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class IHyzzUserDictService {
    @Autowired
    private IHyzzFaqPairRecordService recordService;
    /**
     * 空格符
     */
    public static final String BLANK_SYMBOL = " ";

    /**
     * 加载用户字典
     */
    public void loadDict() {
        List<HyzzInverseIndexEntity> words = recordService.findAllIndex();
        String dictPath = TextUtils.getDictPath();
        // 创建文件对象
        File file = new File(dictPath);
        try (FileWriter writer = new FileWriter(file, true)) {
            for (HyzzInverseIndexEntity word : words) {
                String index = word.getIndex();
                Long weight = word.getWeight();
                writer.write(index + BLANK_SYMBOL + weight + BLANK_SYMBOL + "n" + "\r\n");
            }
            writer.flush();
        } catch (Exception e) {
            log.error("Failed to write to target file:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
    }
}
