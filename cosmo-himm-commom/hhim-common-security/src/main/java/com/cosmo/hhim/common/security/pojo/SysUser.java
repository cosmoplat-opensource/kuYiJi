/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.pojo;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.annotation.Excel.ColumnType;
import com.cosmo.hhim.common.core.annotation.Excel.Type;
import com.cosmo.hhim.common.core.annotation.Excels;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.common.security.vo.CimDataAuthorityVO;
import com.cosmo.hhim.common.security.vo.ItpmDataAuthorityVo;
import com.cosmo.hhim.common.security.vo.SrmDataAuthorityVo;
import com.cosmo.hhim.common.security.vo.SysDataScopeVo;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * 用户对象 sys_user
 * 
 * @author cosmo-hhim-open Team
 */
public class SysUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * app "我的" 修改用户头像校验使用
     */
    public interface UpdateUserHeadPicture{}

    /** 用户ID */
    private Long userId;

    /** 部门ID */
    @Excel(name = "部门编号", type = Type.IMPORT)
    private Long deptId;

    /** 用户账号 */
    @Excel(name = "登录名称")
    @NotBlank(message = "用户账号不能是空",groups = {UpdateUserHeadPicture.class})
    private String userName;

    /** 用户昵称 */
    @Excel(name = "用户名称")
    private String nickName;

    /** 用户邮箱 */
    @Excel(name = "用户邮箱")
    private String email;

    /** 手机号码 */
    @Excel(name = "手机号码")
    private String phonenumber;

    /** 用户性别 */
    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    /** 用户头像 */
    @NotBlank(message = "用户头像地址不能是空",groups = {UpdateUserHeadPicture.class})
    private String avatar;

    /** 密码 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private transient String password;

    /** 盐加密 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private transient String salt;

    /** 帐号状态（0正常 1停用） */
    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 最后登录IP */
    @Excel(name = "最后登录IP", type = Type.EXPORT)
    private String loginIp;

    /** 最后登录时间 */
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date loginDate;

    private String dataCode;

    private String dataType;

    public String getDataCode() {
        return dataCode;
    }

    public void setDataCode(String dataCode) {
        this.dataCode = dataCode;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /** 部门对象 */
    @Excels({
            @Excel(name = "部门名称", targetAttr = "deptName", type = Type.EXPORT),
            @Excel(name = "部门负责人", targetAttr = "leader", type = Type.EXPORT)
    })
    private SysDept dept;

    /** 角色对象 */
    private List<SysRole> roles;

    /** 角色组 */
    private Long[] roleIds;

    /** 岗位组 */
    private Long[] postIds;

    /** 账号有效期 */
    private Date validDate;

    /** 账号类型（1-企业管理员；2普通账号） */
    private String mainAccountFlag;

    /**
     * 修改密码时间
     */
    private Date changePasswordTime;

    /** 企业编码 */
    private String cmpCode;

    /** CIM数据权限列表 */
    private CimDataAuthorityVO authorityVO;

    private String userType;

    private List<SysDataAuthority> dataAuthorityList;

    /** wms数据权限 */
    private List<SysDataScopeVo> dataScopes;

    /** srm数据权限 */
    private List<SrmDataAuthorityVo> srmDataAuthorityVoList;

    /**
     * itpm数据权限
     */
    private List<ItpmDataAuthorityVo> deviceDataAuthorityVoList;

    /** oms数据权限 */
    private List<SrmDataAuthorityVo> omsDataAuthorityVoList;

    /**
     * srm数据权限全选标志 Y全选/N非全选
     */
    private String srmWholeFlag;

    /**
     * oms数据权限全选标志 Y全选/N非全选
     */
    private String customerWholeFlag;

    private String roleName;
    /**
     * 是否显示水印 Y/N
     */
    private String waterMark;
    //用户是否认证 1已认证 0未认证
    private int isAuthentication;
    //uucId
    private Long uucUserId;
    //uuc 验证码
    private String uucVerifyCode;
    //uuc token
    private String uucAccessToken;

    public String getUucAccessToken() {
        return uucAccessToken;
    }

    public void setUucAccessToken(String uucAccessToken) {
        this.uucAccessToken = uucAccessToken;
    }

    public String getUucVerifyCode() {
        return uucVerifyCode;
    }

    public void setUucVerifyCode(String uucVerifyCode) {
        this.uucVerifyCode = uucVerifyCode;
    }

    public int getIsAuthentication() {
        return isAuthentication;
    }

    public void setIsAuthentication(int isAuthentication) {
        this.isAuthentication = isAuthentication;
    }

    public Long getUucUserId() {
        return uucUserId;
    }

    public void setUucUserId(Long uucUserId) {
        this.uucUserId = uucUserId;
    }

    public SysUser()
    {

    }

    public SysUser(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return isAdmin(this.userId, CheckObjectUtils.isEmpty(this.mainAccountFlag) ? "2" : this.mainAccountFlag);
    }

    public static boolean isAdmin(Long userId, String mainAccountFlag) {
        return userId != null && (1L == userId || "1".equals(mainAccountFlag));
    }

    public static boolean isAdmin(Long userId) {
        return userId != null && (1L == userId);
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    @NotBlank(message = "用户账号不能为空")
    @Size(min = 0, max = 30, message = "用户账号长度不能超过30个字符")
    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    @JsonProperty
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getSalt()
    {
        return salt;
    }

    public void setSalt(String salt)
    {
        this.salt = salt;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getLoginIp()
    {
        return loginIp;
    }

    public void setLoginIp(String loginIp)
    {
        this.loginIp = loginIp;
    }

    public Date getLoginDate()
    {
        return loginDate;
    }

    public void setLoginDate(Date loginDate)
    {
        this.loginDate = loginDate;
    }

    public SysDept getDept()
    {
        return dept;
    }

    public void setDept(SysDept dept)
    {
        this.dept = dept;
    }

    public List<SysRole> getRoles()
    {
        return roles;
    }

    public void setRoles(List<SysRole> roles)
    {
        this.roles = roles;
    }

    public Long[] getRoleIds()
    {
        return roleIds;
    }

    public void setRoleIds(Long[] roleIds)
    {
        this.roleIds = roleIds;
    }

    public Long[] getPostIds()
    {
        return postIds;
    }

    public void setPostIds(Long[] postIds)
    {
        this.postIds = postIds;
    }

    public Date getValidDate() {
        return validDate;
    }

    public void setValidDate(Date validDate) {
        this.validDate = validDate;
    }

    public String getMainAccountFlag() {
        return mainAccountFlag;
    }

    public void setMainAccountFlag(String mainAccountFlag) {
        this.mainAccountFlag = mainAccountFlag;
    }

    public String getCmpCode() {
        return cmpCode;
    }

    public void setCmpCode(String cmpCode) {
        this.cmpCode = cmpCode;
    }

    public CimDataAuthorityVO getAuthorityVO() {
        return authorityVO;
    }

    public void setAuthorityVO(CimDataAuthorityVO authorityVO) {
        this.authorityVO = authorityVO;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public List<SysDataScopeVo> getDataScopes() {
        return dataScopes;
    }

    public void setDataScopes(List<SysDataScopeVo> dataScopes) {
        this.dataScopes = dataScopes;
    }

    public List<SysDataAuthority> getDataAuthorityList() {
        return dataAuthorityList;
    }

    public void setDataAuthorityList(List<SysDataAuthority> dataAuthorityList) {
        this.dataAuthorityList = dataAuthorityList;
    }

    public List<SrmDataAuthorityVo> getSrmDataAuthorityVoList() {
        return srmDataAuthorityVoList;
    }

    public void setSrmDataAuthorityVoList(List<SrmDataAuthorityVo> srmDataAuthorityVoList) {
        this.srmDataAuthorityVoList = srmDataAuthorityVoList;
    }

    public List<ItpmDataAuthorityVo> getDeviceDataAuthorityVoList() {
        return deviceDataAuthorityVoList;
    }

    public void setDeviceDataAuthorityVoList(List<ItpmDataAuthorityVo> deviceDataAuthorityVoList) {
        this.deviceDataAuthorityVoList = deviceDataAuthorityVoList;
    }

    public String getSrmWholeFlag() {
        return srmWholeFlag;
    }

    public void setSrmWholeFlag(String srmWholeFlag) {
        this.srmWholeFlag = srmWholeFlag;
    }

    public Date getChangePasswordTime() {
        return changePasswordTime;
    }

    public void setChangePasswordTime(Date changePasswordTime) {
        this.changePasswordTime = changePasswordTime;
    }


    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getWaterMark() {
        return waterMark;
    }

    public void setWaterMark(String waterMark) {
        this.waterMark = waterMark;
    }

    public List<SrmDataAuthorityVo> getOmsDataAuthorityVoList() {
        return omsDataAuthorityVoList;
    }

    public void setOmsDataAuthorityVoList(List<SrmDataAuthorityVo> omsDataAuthorityVoList) {
        this.omsDataAuthorityVoList = omsDataAuthorityVoList;
    }

    public String getCustomerWholeFlag() {
        return customerWholeFlag;
    }

    public void setCustomerWholeFlag(String customerWholeFlag) {
        this.customerWholeFlag = customerWholeFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("userId", getUserId())
                .append("deptId", getDeptId())
                .append("userName", getUserName())
                .append("nickName", getNickName())
                .append("email", getEmail())
                .append("phonenumber", getPhonenumber())
                .append("sex", getSex())
                .append("avatar", getAvatar())
                .append("password", getPassword())
                .append("salt", getSalt())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("loginIp", getLoginIp())
                .append("loginDate", getLoginDate())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("dept", getDept())
                .append("validDate",getValidDate())
                .append("mainAccountFlag", getMainAccountFlag())
                .append("cmpCode",getCmpCode())
                .append("authorityVO",getAuthorityVO())
                .append("dataScopes",getDataScopes())
                .append("srmDataAuthorityVoList",getSrmDataAuthorityVoList())
                .append("srmWholeFlag",getSrmWholeFlag())
                .toString();
    }
}
