/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.manager;

import com.cosmo.hhim.thirdplat.api.unipush.domain.in.DeviceUserBindInfo;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.DeviceUserBind;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.manager.IDeviceUserBindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.Valid;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-30
 */
@RestController
@RequestMapping("/manager/bind")
public class DeviceUserBindController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IDeviceUserBindService iDeviceUserBindService;

    /**
     * 重置设备绑定的用户账号
     */
    @PutMapping("/reset")
    public APIResponse<Boolean> resetBind(@RequestBody @Valid DeviceUserBindInfo deviceUserBindInfo) {
        return APIResponse.success(iDeviceUserBindService.resetBind(deviceUserBindInfo));
    }


    /**
     * 根据用户名查询绑定设备id
     */
    @GetMapping("/findByUserName")
    public APIResponse<List<DeviceUserBind>> findByUserName(@RequestParam("userName") List<String> userName) {
        return APIResponse.success(iDeviceUserBindService.findByUserName(userName));
    }

}
