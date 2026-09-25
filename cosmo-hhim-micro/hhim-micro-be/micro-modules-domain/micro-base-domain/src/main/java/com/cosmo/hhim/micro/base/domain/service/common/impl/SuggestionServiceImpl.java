/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroSuggestion;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroSuggestionMapper;
import com.cosmo.hhim.micro.base.domain.service.common.ISuggestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户建议 Service 实现
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class SuggestionServiceImpl implements ISuggestionService {

    @Autowired
    private MicroSuggestionMapper suggestionMapper;

    @Override
    public int insert(MicroSuggestion record) {
        return suggestionMapper.insert(record);
    }
}
