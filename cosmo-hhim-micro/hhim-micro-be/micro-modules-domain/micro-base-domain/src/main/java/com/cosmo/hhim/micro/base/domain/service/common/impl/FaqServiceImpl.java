/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroFaq;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroFaqMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IFaqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * FAQ Service 实现
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class FaqServiceImpl implements IFaqService {

    @Autowired
    private MicroFaqMapper faqMapper;

    @Override
    public List<MicroFaq> listByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return Collections.emptyList();
        }
        return faqMapper.selectByCategory(category);
    }

    @Override
    public List<MicroFaq> list(MicroFaq query) {
        return faqMapper.selectList(query);
    }

    @Override
    public int insert(MicroFaq record) {
        return faqMapper.insert(record);
    }

    @Override
    public int updateById(MicroFaq record) {
        return faqMapper.updateById(record);
    }
}
