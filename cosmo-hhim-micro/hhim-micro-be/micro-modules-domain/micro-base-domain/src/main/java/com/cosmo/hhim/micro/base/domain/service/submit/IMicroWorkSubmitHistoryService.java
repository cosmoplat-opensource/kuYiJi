/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.submit;

import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistory;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistoryDto;

import java.util.List;

/**
 * 报工记录历史服务接口
 *
 * @author cosmo-hhim-open Team
 * @since 2022-10-26 13:56:32
 */
public interface IMicroWorkSubmitHistoryService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    MicroWorkSubmitHistory selectMicroWorkSubmitHistoryById(Long id);

    /**
     * 根据报工记录号查询报工历史记录列表
     *
     * @param submitNo
     * @return
     */
    List<MicroWorkSubmitHistoryDto> selectMicroWorkSubmitHistoryListBySubmitNo(String submitNo);
}
