/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.base.flush;

import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdInterfaceEntity;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdStatusEnum;
import org.springframework.beans.factory.InitializingBean;

import java.util.Date;

public abstract class ThirdFlushAbstract implements InitializingBean {
    public abstract String flushDB(ThirdStatusEnum statusEnum, ThirdInterfaceEntity interfaceEntity, Date requestDate);
}