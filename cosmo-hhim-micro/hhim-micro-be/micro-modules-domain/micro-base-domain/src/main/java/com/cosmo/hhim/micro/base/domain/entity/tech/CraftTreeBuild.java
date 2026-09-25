/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import com.cosmo.hhim.common.core.utils.StringUtils;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description: 工艺树构建
 * @date 2023/2/22 13:31
 */
@Data
public class CraftTreeBuild {

    /**
     * 存储一棵树里面出现过的节点
     */
    private List<ProcessNode> singleChainNodes = new ArrayList<>();

    /**
     * 顶级节点的深度
     */
    private static final int ROOT_NODE_DEPTH = 0;

    /**
     * 按照层级存储节点信息
     */
    private Map<Integer, List<ProcessNode>> deepMap = new HashMap<>(16);

    private List<ProcessNode> nodeList;

    public CraftTreeBuild(List<ProcessNode> nodeList) {
        this.nodeList = nodeList;
    }

    /**
     * 根据顶级节点构建所有工艺树
     *
     * @return
     */
    public List<ProcessNode> buildCraftTree() {
        // 保存一个顶级节点所构建出来的树
        List<ProcessNode> treeNodes = new ArrayList<>();
        // 计算树的深度
        int depth = ROOT_NODE_DEPTH;

        for (ProcessNode rootNode : getRootNode()) {
            rootNode.setDepth(depth);
            rootNode = buildChildCraftTree(rootNode, depth);
            treeNodes.add(rootNode);
        }

        return treeNodes;
    }

    /**
     * 获取根节点
     *
     * @return
     */
    public List<ProcessNode> getRootNode() {
        List<ProcessNode> rootNodeList = new ArrayList<>();
        // 如果工序节点的前工序为null，则为根节点
        for (ProcessNode processNode : nodeList) {
            if (processNode.getPreProcessSeq() == null || "0".equals(processNode.getPreProcessSeq())) {
                rootNodeList.add(processNode);
            }
        }
        return rootNodeList;
    }

    /**
     * 利用递归的方式获取一个顶级节点下面的工艺树
     *
     * @param rootNode
     * @return
     */
    public ProcessNode buildChildCraftTree(ProcessNode rootNode, int depth) {
        List<ProcessNode> childCraftTree = new ArrayList<>();

        for (ProcessNode processNode : nodeList) {
            if (StringUtils.isNotEmpty(processNode.getPreProcessSeq())) {
                if (processNode.getPreProcessSeq().equals(rootNode.getOperateProcessSeq())) {
                    // 用于记录已经出现的树的节点
                    singleChainNodes.add(rootNode);
                    // 如果一个节点出现超过两次就把它确认为是并序，深度不再增加
                    if (singleChainNodes.stream()
                            .filter(obj -> obj.getOperateProcessSeq().equals(rootNode.getOperateProcessSeq()))
                            .count() < 2) {
                        depth += 1;
                    }
                    processNode.setDepth(depth);
                    // 递归构建
                    childCraftTree.add(buildChildCraftTree(processNode, depth));
                }
            }
        }
        rootNode.setChildrenProcess(childCraftTree);
        return rootNode;
    }

    /**
     * 返回各深度的树节点
     *
     * @param tree
     */
    public Map<Integer, List<ProcessNode>> deepOrderTraversal(List<ProcessNode> tree) {
        if (CollectionUtils.isEmpty(tree)) {
            return deepMap;
        }

        for (ProcessNode processNode : tree) {
            // 不再携带子树的节点信息
            ProcessNode singleNodeTemp = new ProcessNode();
            singleNodeTemp.setDepth(processNode.getDepth());
            singleNodeTemp.setPreProcessSeq(processNode.getPreProcessSeq());
            singleNodeTemp.setOperateProcessSeq(processNode.getOperateProcessSeq());

            // 向map中存储每一层对应的树节点信息
            List<ProcessNode> temp = new ArrayList<>();
            temp.add(processNode);
            if (!CollectionUtils.isEmpty(deepMap.get(processNode.getDepth()))) {
                temp.addAll(deepMap.get(processNode.getDepth()));
            }

            deepMap.put(processNode.getDepth(), temp);
            if (!CollectionUtils.isEmpty(processNode.getChildrenProcess())) {
                deepOrderTraversal(processNode.getChildrenProcess());
            }
        }
        return deepMap;
    }
}
