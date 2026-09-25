/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.user;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.enums.RoleCodeEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色Controller
 *
 * @date 2022-10-12
 */
@RestController
@RequestMapping("/role")
public class MicroRoleController extends BaseController {

    /**
     * 查询角色列表
     */
    @GetMapping("/select")
    public AjaxResult select() {
        List<MicroSelectEntity> list = new ArrayList<>();
        MicroSelectEntity select;
        for (RoleCodeEnum codeEnum : RoleCodeEnum.values()) {
            if (!codeEnum.equals(RoleCodeEnum.MANAGER)) {
                select = new MicroSelectEntity();
                select.setItemSeq(codeEnum.getCode());
                select.setItemCode(codeEnum.getCode());
                select.setItemName(codeEnum.getDesc());
                list.add(select);
            }
        }
        return AjaxResult.success(list);
    }
}
