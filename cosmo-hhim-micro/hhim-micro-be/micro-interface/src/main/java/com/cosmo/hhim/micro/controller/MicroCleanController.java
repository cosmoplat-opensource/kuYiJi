/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.utils.utils.DBControlUtil;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.application.service.tech.IMicroTechFacadeService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroCleanService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @date 2023/1/13 16:45
 */
@RestController
@RequestMapping("/clean")
public class MicroCleanController {

    @Autowired
    IMicroCleanService microCleanService;
    @Autowired
    IMicroTechFacadeService techFacadeService;

    @GetMapping("/duplicatedProduct")
    public AjaxResult cleanDuplicatedProduct() {
        // decouple-from-ops-platform：单库场景，datasource 固定 db0
        DBControlUtil.setDbAndSchema("db0", CommonConstants.MICRO_SCHEMA, "708181");
        return AjaxResult.success(microCleanService.cleanDuplicatedProduct());
    }

    @PostMapping("/chain")
    public AjaxResult sortChain(@RequestBody List<String> customers) {
        for (String customer : customers) {
            DBControlUtil.setDbAndSchema("db0", CommonConstants.MICRO_SCHEMA, customer);
            microCleanService.initSortChain(customers);
        }
        return AjaxResult.success();
    }
}
