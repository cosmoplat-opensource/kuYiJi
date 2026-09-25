/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.util;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainBindEntity;
import com.cosmo.hhim.micro.infrastructure.enums.IsLastProcessEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @description: 工艺链工具类
 * @date 2023/3/23 15:13
 */
@Slf4j
public class TechChainUtils {

    /**
     * 找到尾巴节点
     *
     * @param bindEntityList
     * @return
     */
    public static List<MicroProcessChainBindEntity> findChainLastNode(List<MicroProcessChainBindEntity> bindEntityList) {
        // 找到尾巴节点
        List<MicroProcessChainBindEntity> nodes = bindEntityList.stream().filter(obj -> IsLastProcessEnum.YES.getCode().equals(obj.getIsLastProcess()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(nodes)) {
            throw new CustomException("该工艺没有尾节点");
        }
        if (nodes.stream().map(MicroProcessChainBindEntity::getProcessSeq).distinct().count() > 1) {
            throw new CustomException("尾节点数量不能大于2");
        }
        nodes = mergeNextNodeList(nodes);
        // 其实只有一个
        return nodes;
    }

    /**
     * 合并相同当前工序的节点,并重置ParentProcessSeq字段
     *
     * @param nextNodeList
     * @return
     */
    public static List<MicroProcessChainBindEntity> mergeNextNodeList(List<MicroProcessChainBindEntity> nextNodeList) {
        // 存储结果
        List<MicroProcessChainBindEntity> res = new ArrayList<>();
        Map<String, List<MicroProcessChainBindEntity>> processMap = nextNodeList.stream().collect(Collectors.groupingBy(MicroProcessChainBindEntity::getProcessSeq));
        for (String key : processMap.keySet()) {
            List<MicroProcessChainBindEntity> tempList = processMap.get(key);
            // 并序的节点，合并parentProcessSeq
            if (tempList.size() > 1) {
                String newParentProcessSeq = tempList.stream().map(MicroProcessChainBindEntity::getParentProcessSeq).collect(Collectors.joining(","));

                MicroProcessChainBindEntity temp = tempList.get(0);
                temp.setParentProcessSeq(newParentProcessSeq);
                List<MicroProcessChainBindEntity> list = new ArrayList<>();
                list.add(temp);
                res.addAll(list);
            } else {
                res.addAll(tempList);
            }
        }
        return res;
    }
}
