/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroSuggestion;

/**
 * 用户建议 Service
 *
 * @author cosmo-hhim-open Team
 */
public interface ISuggestionService {

    int insert(MicroSuggestion record); 
}
