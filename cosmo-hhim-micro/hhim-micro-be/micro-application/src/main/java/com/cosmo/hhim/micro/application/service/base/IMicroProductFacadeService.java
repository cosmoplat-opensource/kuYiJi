/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.micro.infrastructure.entity.MicroProductSelectEntity;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
public interface IMicroProductFacadeService {
    List<MicroProductSelectEntity> selectMicroProductByName(String key, boolean mixed); 

    int removeProductByIds(Long[] ids); 
}
