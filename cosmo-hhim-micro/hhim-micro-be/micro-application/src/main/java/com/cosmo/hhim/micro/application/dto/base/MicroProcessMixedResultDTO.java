/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.base;

import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MicroProcessMixedResultDTO implements Serializable { 

    private boolean standard; 
    private List<MicroSelectEntity> selectList;
}
