/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.intercept;

import com.cosmo.hhim.micro.base.domain.tips.intercept.anno.EnableContentTip;
import com.cosmo.hhim.micro.base.domain.tips.intercept.conf.ProxyContentTipConfiguration;
import org.springframework.context.annotation.AdviceMode;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
public class ContentTipConfigurationSelector extends AbstractAdviceContentTipSelector<EnableContentTip> {

    @Override
    protected String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return new String[]{ProxyContentTipConfiguration.class.getName()};
            case ASPECTJ:
                return null;
            default:
                return null;
        }
    }
}
