/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 用户信息
 *
 * @author cosmo-hhim-open Team
 */
public class LoginUser implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 用户名id
     */
    private Long userid;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 角色列表
     */
    private Set<String> roles;

    /**
     * 用户信息
     */
    private SysUser sysUser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * datasource
     */
    private String dataSource;

    /**
     * schema
     */
    private String schema;

    /**
     * schema
     */
    private String customer;


    /**
     * 顶级部门
     */
    private String topSysDeptName;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;


    /**
     * 应用
     */
    private List<String> appsList;

    /**
     * 价格权限信息
     */
    private List<String> pricePermissions;

    //token删除标记
    private String loginType;

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public Long getUserid()
    {
        return userid;
    }

    public void setUserid(Long userid)
    {
        this.userid = userid;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public Long getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }

    public Long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Long expireTime)
    {
        this.expireTime = expireTime;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
    }

    public Set<String> getRoles()
    {
        return roles;
    }

    public void setRoles(Set<String> roles)
    {
        this.roles = roles;
    }

    public SysUser getSysUser()
    {
        return sysUser;
    }

    public void setSysUser(SysUser sysUser)
    {
        this.sysUser = sysUser;
    }

    public String getTopSysDeptName() {
        return topSysDeptName;
    }

    public void setTopSysDeptName(String topSysDeptName) {
        this.topSysDeptName = topSysDeptName;
    }

    public List<String> getAppsList() {
        return appsList;
    }

    public void setAppsList(List<String> appsList) {
        this.appsList = appsList;
    }

    public String getLoginLocation() {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation) {
        this.loginLocation = loginLocation;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public List<String> getPricePermissions() {
        return pricePermissions;
    }

    public void setPricePermissions(List<String> pricePermissions) {
        this.pricePermissions = pricePermissions;
    }
}
