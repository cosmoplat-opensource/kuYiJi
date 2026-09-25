/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatmp.impl;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.CreateMenuParam;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.modules.wechatmp.api.WxMenuApi;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in.CreateMenuInfoInDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.CreateMenuInfoOutDto;
import com.cosmo.hhim.thirdplat.web.service.wechatmp.IMpMenuService;
import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.cosmo.hhim.thirdplat.web.utils.ResponseParserUtil.wxResponseExceptionHandler;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Slf4j
@Service
public class MpMenuServiceImpl implements IMpMenuService {

    @Autowired
    private WxMenuApi wxMenuApi;

    /**
     * 创建自定义菜单
     * @param param
     * @return
     */
    @Override
    public APIResponse<Boolean> createMenu(CreateMenuParam param) {

        // 组装请求参数
        CreateMenuInfoInDto inDto = new CreateMenuInfoInDto();
        BeanUtils.copyProperties(param, inDto);

        // 发起请求
        ForestResponse<CreateMenuInfoOutDto> response = wxMenuApi.createMenu(inDto);
        APIResponse errorResponse = wxResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        return APIResponse.success(true);
    }
}
