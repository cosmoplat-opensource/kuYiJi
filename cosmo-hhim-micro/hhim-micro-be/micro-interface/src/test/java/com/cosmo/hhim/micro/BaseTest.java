/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/10
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LittleGiantsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class BaseTest {

    @Before
    public void before() {

        String deviceType = "wechatMiniApp";
        String db = "db1";
        String schema = "`im_micro`";
        String customerCode = "391176";
        String authorization = "Bearer a6687a2e-71e8-4b51-8471-842f6e8d1bd1";
        String userName = "xiaohuihui";
        Long userId = 185L;

        Map<String, String> map = new HashMap<>(16);
        map.put(Constants.TARGET_DS, db);
        map.put(Constants.TARGET_SCHEMA, schema);
        map.put(CacheConstants.AUTHORIZATION_HEADER, authorization);
        map.put(Constants.TARGET_CUSTOMER, customerCode);
        map.put(CacheConstants.DETAILS_USERNAME, userName);
        map.put(CacheConstants.DETAILS_USER_ID, userId.toString());
        map.put(CacheConstants.DETAILS_TYPE, deviceType);
        ThreadContext.putAll(map);
    }

    @After
    public void testPassword() {
        ThreadContext.clear();
    }

}
