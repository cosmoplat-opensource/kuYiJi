/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import com.github.pagehelper.Page;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/21
 */
public class MicroPageUtils {

    /**
     * list转为Page
     *
     * @param sourceList PageHelper原始List
     * @param targetList 需要转为Page的List
     * @param <T>
     * @param <S>
     * @return
     */
    public static <T, S> Page<T> listToPage(List<S> sourceList, List<T> targetList) {
        Page<T> page = new Page<>();
        long total = 0L;
        if (CollectionUtils.isEmpty(targetList)) {
            page.setTotal(total);
            return page;
        }
        if (sourceList instanceof Page) {
            Page sourcePage = (Page) sourceList;
            total = sourcePage.getTotal();
        } else {
            total = targetList.size();
        }
        page.addAll(targetList);
        page.setTotal(total);
        return page;
    }
}
