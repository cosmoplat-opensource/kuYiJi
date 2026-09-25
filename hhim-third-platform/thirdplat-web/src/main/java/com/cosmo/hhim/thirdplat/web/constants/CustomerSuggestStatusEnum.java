/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.constants;



/**
 * 客户意见反馈状态
 * @author cosmo-hhim-open Team
 */

public enum CustomerSuggestStatusEnum {
    CREATE("创建", "10"),
    DEAL("处理", "20"),
    FINISH("完成", "30");
    private String name;
    private String code;
    CustomerSuggestStatusEnum(String name, String code) {
       this.name=name;
       this.code=code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

}
