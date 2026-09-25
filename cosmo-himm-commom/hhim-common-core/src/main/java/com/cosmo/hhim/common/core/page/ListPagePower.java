/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.page;

import com.cosmo.hhim.common.core.web.page.PageDomain;
import com.github.pagehelper.Page;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @description 提供列表分页能力
 * @createTime 2022-05-26
 */
public abstract class ListPagePower {
    /**
     * 列表分页
     *
     * @param pageDomain
     * @param dataList
     * @return
     */
    public <T> Page<T> doPageList(PageDomain pageDomain, List<T> dataList) {
        Page<T> pages = new Page<>();
        pages.setTotal(dataList.size());
        if (!Optional.ofNullable(pageDomain.getPageNum()).isPresent() || !Optional.ofNullable(pageDomain.getPageSize()).isPresent()) {
            pages.addAll(dataList);
            return pages;
        }
        long skipSize = (pageDomain.getPageNum().longValue() - 1L) * pageDomain.getPageSize().longValue();
        pages.addAll(dataList.stream().skip(skipSize).limit(pageDomain.getPageSize()).collect(Collectors.toList()));
        return pages;
    }

    /**
     * List转Page
     * @param dataList
     * @param <T>
     * @return
     */
    public <T> Page<T> listToPage(List<T> dataList){
        Page<T> pages = new Page<>();
        pages.setTotal(dataList.size());
        pages.addAll(dataList);
        return pages;
    }
}
