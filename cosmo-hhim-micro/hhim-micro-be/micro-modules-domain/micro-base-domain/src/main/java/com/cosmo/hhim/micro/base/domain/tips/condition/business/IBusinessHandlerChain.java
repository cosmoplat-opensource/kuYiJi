/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
public interface IBusinessHandlerChain {

    /**
     * 执行下一个业务处理器
     *
     * @param param
     * @return
     */
    BusinessHandlerResult executeNextHandler(BusinessHandlerParam param);

}
