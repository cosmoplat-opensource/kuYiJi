/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.ai;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * AI 问数 · 租户字典检索 Mapper（实体查证用，只读）
 *
 * @author cosmo-hhim-open Team
 */
public interface MicroAiDictMapper {

    /** 产品检索：名称模糊 或 编码精确（返回 product_seq/product_name/product_code 等） */
    List<Map<String, Object>> selectProductByName(@Param("name") String name);

    /** 工序检索 */
    List<Map<String, Object>> selectProcessByName(@Param("name") String name);

    /** 员工检索：昵称模糊 或 账号精确（返回 user_id/nick_name/user_name） */
    List<Map<String, Object>> selectEmployeeByName(@Param("name") String name);

    /** 实体清单（ENTITY_LIST）：type ∈ product/process/employee，Top 10 */
    List<Map<String, Object>> selectEntityList(@Param("tenantCode") String tenantCode,
                                               @Param("type") String type);
}
