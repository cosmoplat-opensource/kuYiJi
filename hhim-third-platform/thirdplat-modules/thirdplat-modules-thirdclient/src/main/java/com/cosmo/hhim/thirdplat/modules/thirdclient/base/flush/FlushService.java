/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.base.flush;

import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceLog;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdResponse;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdStatusEnum;

public interface FlushService {
    void saveLog(ThirdStatusEnum statusEnum, HyzzThirdInterfaceLog interfaceLog, ThirdResponse response);
}
