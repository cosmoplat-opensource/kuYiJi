/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-03
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulateLoginParam {

    // 手机号 
    private String phoneNum;

    // 账号 
    private String username;

    // 密码 
    private String password;

    // 应用编码 
    private String appCode;

    public void checkParam() {
        if (StringUtils.hasText(phoneNum) && StringUtils.hasText(username)) {
            throw new CustomException("参数非法！手机号或账号不能同时使用！");
        }

        if (!StringUtils.hasText(phoneNum) && !StringUtils.hasText(username)) {
            throw new CustomException("参数非法！手机号或账号不允许都为空！");
        }

        if (StringUtils.hasText(username) && !StringUtils.hasText(password)) {
            throw new CustomException("参数非法！账号登录时，密码不允许为空！");
        }
    }

}
