/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.web.controller;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.cosmo.hhim.common.core.constant.HttpStatus;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.utils.sql.SqlUtil;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.PageDomain;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.core.web.page.TableSupport;

/**
 * web层通用数据处理
 *
 * @author cosmo-hhim-open Team
 */
public class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(BaseController.class);

    private int typeFeign = 0;//feign请求类型
    private int typeAll = 1;//设置所有分页包含feign和pageHelper

    public BaseController() {
    }


    public BaseController(Integer pageNum,Integer pageSize,int tp) {
        if (typeAll == tp){
            this.startPage(pageNum,pageSize);
        } else if (typeFeign == tp){
            ThreadContext.put(Constants.PAGE_NUM_FEIGN,pageNum);
            ThreadContext.put(Constants.PAGE_SIZE_FEIGN,pageSize);
        }
    }



    /**
     * 将前台传递过来的日期格式的字符串，自动转化为Date类型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * 设置请求分页数据
     */
    protected void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if ( !(StringUtils.isNotNull(pageNum) && StringUtils.isNotNull(pageSize))
                && CheckObjectUtils.isNotEmpty(ThreadContext.get(Constants.PAGE_NUM_FEIGN))
                && CheckObjectUtils.isNotEmpty(ThreadContext.get(Constants.PAGE_SIZE_FEIGN))) {
            pageNum = Integer.valueOf(ThreadContext.get(Constants.PAGE_NUM_FEIGN).toString());
            pageSize = Integer.valueOf(ThreadContext.get(Constants.PAGE_SIZE_FEIGN).toString());
        }
        this.startPage(pageNum,pageSize);
    }

    /**
     * 设置请求分页数据
     */
    protected void startPage(Integer pageNum,Integer pageSize) {
        if (StringUtils.isNotNull(pageNum) && StringUtils.isNotNull(pageSize) ){
            PageDomain pageDomain = TableSupport.buildPageRequest();
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.startPage(pageNum, pageSize, orderBy);
            if (CheckObjectUtils.isNotEmpty(ThreadContext.get("pageNumFeign")) && CheckObjectUtils.isNotEmpty(ThreadContext.get("pageSizeFeign"))) {
                ThreadContext.remove("pageNumFeign");
                ThreadContext.remove("pageSizeFeign");
            }
        }
    }

    /**
     * 设置请求分页数据
     */
    public void startPage(Integer pageNum,Integer pageSize,int tp) {
        if (typeAll == tp){
            if (CheckObjectUtils.isEmpty(pageNum)){
                this.startPage();
                PageDomain pageDomain = TableSupport.buildPageRequest();
                pageNum = pageDomain.getPageNum();
                pageSize = pageDomain.getPageSize();
                ThreadContext.put(Constants.PAGE_NUM_FEIGN,pageNum);
                ThreadContext.put(Constants.PAGE_SIZE_FEIGN,pageSize);
                return;
            }
            this.startPage(pageNum,pageSize);
        } else if (typeFeign == tp){
            if (CheckObjectUtils.isEmpty(pageNum)){
                PageDomain pageDomain = TableSupport.buildPageRequest();
                pageNum = pageDomain.getPageNum();
                pageSize = pageDomain.getPageSize();
            }
            ThreadContext.put(Constants.PAGE_NUM_FEIGN,pageNum);
            ThreadContext.put(Constants.PAGE_SIZE_FEIGN,pageSize);
        }
    }

    /**
     * 响应请求分页数据
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected TableDataInfo getDataTable(List<?> list) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setRows(list);
        rspData.setMsg("查询成功");
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

    protected TableDataInfo getDataTable(List<?> list, Map<String, Object> data) {
        TableDataInfo dataTable = getDataTable(list);
        dataTable.setData(data);
        return dataTable;
    }

    protected TableDataInfo getDataTable(List<?> list, Object data) {
        TableDataInfo dataTable = getDataTable(list);
        dataTable.setData(data);
        return dataTable;
    }


    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    protected AjaxResult toAjax(int rows, String msg) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error(msg);
    }

    protected AjaxResult toAjax(int rows,Object data,String msg)
    {
        return rows > 0 ? AjaxResult.success(data) : AjaxResult.error(msg);
    }

    /**
     * 设置请求排序数据
     */
    protected void startOrderBy() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        if (StringUtils.isNotEmpty(pageDomain.getOrderBy())) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.orderBy(orderBy);
        }
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果
     */
    protected AjaxResult toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * 返回成功
     */
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * 返回成功消息
     */
    public AjaxResult success(String message) {
        return AjaxResult.success(message);
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error(String message) {
        return AjaxResult.error(message);
    }

    /**
     * 页面跳转
     */
    public String redirect(String url) {
        return StringUtils.format("redirect:{}", url);
    }


}
