/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.user;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.application.dto.base.MicroUserRecommendButtonDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroUserFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserEditEntity;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserQueryParam;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * 用户Controller
 *
 * @date 2022-10-11
 */
@Validated
@RestController
@RequestMapping("/user")
public class MicroUserController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）；
     * roleCode/roleName 为查询结果聚合输出字段（SQL group_concat 生成），非查询条件，禁止作为入参绑定
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class", "roleCode", "roleName");
    }

    @Autowired
    private IMicroUserService microUserService;
    @Autowired
    private IMicroUserFacadeService facadeService;


    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public TableDataInfo list(MicroUserQueryParam microUser) {
        startPage();
        List<MicroUser> list = microUserService.selectMicroUserList(microUser);
        return getDataTable(list);
    }

    /**
     * 查询所有用户列表(包括管理员)
     *
     * @param microUser
     * @return
     */
    @GetMapping("/all/list")
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public TableDataInfo allList(MicroUserQueryParam microUser) {
        startPage();
        List<MicroUser> list = microUserService.selectMicroAllUserList(microUser);
        return getDataTable(list);
    }

    /**
     * 获取用户详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(microUserService.selectMicroUserById(id));
    }

    /**
     * 修改用户
     */
    @PostMapping("/edit")
    @Log(title = "人员管理模块", businessType = BusinessType.UPDATE)
    public AjaxResult edit(@RequestBody MicroUserEditEntity microUser) {
        return toAjax(facadeService.editUser(microUser));
    }

    /**
     * 根据用户名查询用户完整信息
     *
     * @param username
     * @return
     */
    @GetMapping("/completeUserInfo")
    public AjaxResult getUserCompleteInfo(@RequestParam("username") String username) {
        return AjaxResult.success(facadeService.getUserCompleteInfo(username));
    }

    /**
     * 根据用户名查询用户业务概览信息
     *
     * @return
     */
    @GetMapping("/businessInfo")
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult getBusinessInfo() {
        return AjaxResult.success(microUserService.getBusinessInfo());
    }

    /**
     * 是否开始个性化
     *
     * @param option
     */
    @GetMapping("/enablePersonalized")
    public AjaxResult enablePersonalized(@RequestParam Integer option) {
        return AjaxResult.success(facadeService.enablePersonalized(option));
    }

    /**
     * 根据用户使用情况,推荐出常访问的页面
     *
     * @param pageType 页面类型
     */
    @GetMapping("/recommendPage")
    public AjaxResult recommendPage(@RequestParam String pageType) {
        return AjaxResult.success(null, facadeService.findUserPersonalizedRecommendPage(pageType));
    }

    /**
     * 根据用户使用情况,推荐出常访问的页面
     *
     * @param dataList 推荐按钮DTO列表
     */
    @PostMapping("/recommendButton")
    public AjaxResult recommendButton(@RequestBody List<MicroUserRecommendButtonDTO> dataList) {
        return AjaxResult.success(facadeService.findUserPersonalizedRecommendButton(dataList));
    }


    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    @Log(title = "人员管理模块", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userId}")
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult removeUser(@PathVariable("userId") @Min(value = 1, message = "用户ID不合法") Long userId) {
        // 删除操作关键参数校验：注解（@Min，扫描器可识别）+ 显式校验双层防护，防止非法/越界参数直接进入删除逻辑（关键参数篡改防护）
        if (userId == null || userId <= 0) {
            throw new CustomException("用户ID不合法");
        }
        return AjaxResult.success(microUserService.removeUser(userId));
    }

}
