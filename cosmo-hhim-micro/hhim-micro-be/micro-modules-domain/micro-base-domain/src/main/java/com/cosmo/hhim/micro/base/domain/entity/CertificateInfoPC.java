/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import lombok.Data;

/**
 * decouple-from-ops-platform：登录态上下文，存到 Redis 的结构。
 * tenantCode 从登录态直接读取，schema/datasource 在单库场景下为常量 im_micro/db0。
 *
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/5
 */
@Data
public class CertificateInfoPC extends MicroUser { 

    // 任务编码 
    private String taskCode;

    // 系统标识（micro_process：KU易记，micro_plan：工易派，micro_material：料易投） 
    private String sysCode;

    // 单库场景下固定为 im_micro 
    private String schema = "im_micro";

    // 单库场景下固定为 db0（与 application-dev.yml 中保留的数据源 bean 名一致） 
    private String datasource = "db0";
}
