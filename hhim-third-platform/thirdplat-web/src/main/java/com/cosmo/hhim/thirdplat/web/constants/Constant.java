/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.constants;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-29
 */
public class Constant {
    public static final String UNIPUSH_INTENT = "intent:#Intent;action=android.intent.action.oppopush;launchFlags=0x14000000;component=%s/io.dcloud.PandoraEntry;S.UP-OL-SU=true;S.title=%s;S.content=%s;S.payload=%s;end";

    public final static int ERROR_REQUEST_CODE = 400;
    public final static int ERROR_SERVER_CODE = 500;
    public final static int SUCCESS_CODE = 200;
}
