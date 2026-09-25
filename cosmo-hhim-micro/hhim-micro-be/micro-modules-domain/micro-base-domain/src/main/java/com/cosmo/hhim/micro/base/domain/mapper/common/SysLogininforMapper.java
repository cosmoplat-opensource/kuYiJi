/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;


import com.cosmo.hhim.micro.base.domain.entity.common.SysLogininfor;

import java.util.List;

/**
 * 系统访问日志情况信息 数据层
 *
 * @author cosmo-hhim-open Team
 */
public interface SysLogininforMapper {
    /**
     * 新增系统登录日志
     *
     * @param logininfor 访问日志对象
     */
    public int insertLogininfor(SysLogininfor logininfor); 

    /**
     * 查询系统登录日志集合
     *
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    /**
     * 批量删除系统登录日志
     *
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    public int deleteLogininforByIds(Long[] infoIds);

    /**
     * 清空系统登录日志
     *
     * @return 结果
     */
    public int cleanLogininfor();

    /**
     * 获取最新登录日志
     *
     * @param username
     * @return
     */
    SysLogininfor getLastLoginInfo(String username);
}
