/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.submit.impl;

import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistory;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistoryDto;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitHistoryMapper;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 报工记录历史服务实现类
 *
 * @author cosmo-hhim-open Team
 * @since 2022-10-26 13:56:34
 */
@Service
public class MicroWorkSubmitHistoryServiceImpl implements IMicroWorkSubmitHistoryService {
    @Resource
    private MicroWorkSubmitHistoryMapper microWorkSubmitHistoryMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public MicroWorkSubmitHistory selectMicroWorkSubmitHistoryById(Long id) {
        return this.microWorkSubmitHistoryMapper.selectMicroWorkSubmitHistoryById(id);
    }

    /**
     * 根据报工记录号查询报工历史详情
     *
     * @param submitNo
     * @return
     */
    @Override
    public List<MicroWorkSubmitHistoryDto> selectMicroWorkSubmitHistoryListBySubmitNo(String submitNo) {
        MicroWorkSubmitHistory param = new MicroWorkSubmitHistory();
        param.setSubmitNo(submitNo);
        List<MicroWorkSubmitHistoryDto> microWorkSubmitHistoryDtoList = microWorkSubmitHistoryMapper.selectMicroWorkSubmitHistoryExList(param);
        return microWorkSubmitHistoryDtoList;
    }
}
