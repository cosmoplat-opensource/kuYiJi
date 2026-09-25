/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.tech.impl;

import cn.hutool.core.bean.BeanUtil;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.IdUtils;
import com.cosmo.hhim.common.redis.distributedlock.utils.RedisLockHelper;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessChain;
import com.cosmo.hhim.micro.base.domain.entity.submit.FirstOrLastProcess;
import com.cosmo.hhim.micro.base.domain.entity.tech.*;
import com.cosmo.hhim.micro.base.domain.mapper.tech.MicroProcessChainMapper;
import com.cosmo.hhim.micro.base.domain.mapper.tech.MicroTechnologyMapper;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroProcessChainService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroTechnologyService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroStandardEntity;
import com.cosmo.hhim.micro.infrastructure.enums.ApplicationTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.HotKeyEnum;
import com.cosmo.hhim.micro.infrastructure.enums.IsFirstProcessEnum;
import com.cosmo.hhim.micro.infrastructure.enums.IsLastProcessEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.BomAndTechTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.TechPatternEnum;
import com.cosmo.hhim.micro.infrastructure.util.CodeGenerateUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 工艺链定义Service业务层处理
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroTechnologyServiceImpl implements IMicroTechnologyService {

    private static final String ROOT_PARENT_PROCESS = "0";
    private static final int TECH_CHAIN_MIN_COUNT = 2;
    @Autowired
    private MicroTechnologyMapper microTechnologyMapper;
    @Autowired
    private IMicroProcessChainService processChainService;
    @Autowired
    private MicroProcessChainMapper microProcessChainMapper;
    @Autowired
    private RedisLockHelper redisLockHelper;

    /**
     * 查询工艺链定义
     *
     * @param id 工艺链定义ID
     * @return 工艺链定义
     */
    @Override
    public MicroTechnology selectMicroTechnologyById(Long id) {
        return microTechnologyMapper.selectMicroTechnologyById(id);
    }

    /**
     * 查询工艺链定义列表
     *
     * @param microTechnology 工艺链定义
     * @return 工艺链定义
     */
    @Override
    public List<MicroTechnology> selectMicroTechnologyList(MicroTechnology microTechnology) {
        return microTechnologyMapper.selectMicroTechnologyList(microTechnology);
    }

    @Override
    public List<MicroProcessChainBindEntity> selectMicroTechChain(MicroTechnology microTechnology) {
        return this.selectMicroTechChainByTechType(microTechnology, BomAndTechTypeEnum.STANDARD.getCode());
    }

    @Override
    public List<MicroProcessChainBindEntity> selectMicroTechChainByTechType(MicroTechnology microTechnology, String techType) {
        List<Long> productIds = new ArrayList<>();
        List<String> productSeqs = new ArrayList<>();
        if (microTechnology.getProductId() != null) {
            productIds = Collections.singletonList(microTechnology.getProductId());
        }
        if (!StringUtils.isEmpty(microTechnology.getProductSeq())) {
            productSeqs = Collections.singletonList(microTechnology.getProductSeq());
        }
        return this.selectMicroTechChainByProductIdsOrSeqs(productIds, productSeqs, techType);
    }

    @Override
    public List<MicroProcessChainBindEntity> selectMicroTechChainByProductIdsOrSeqs(List<Long> productIds, List<String> productSeqs, String techType) {
        List<MicroProcessChainBindEntity> bindEntityList = microTechnologyMapper.selectMicroTechChainByProductIdsOrSeqs(productIds, productSeqs, techType);
        HashSet<Long> standardIdSet = new HashSet<>();
        HashSet<Long> draftIdSet = new HashSet<>();
        Set<Long> standardSet = bindEntityList.stream()
                .filter(t -> BomAndTechTypeEnum.STANDARD.getCode().equals(t.getTechType()))
                .map(MicroProcessChainBindEntity::getProductId)
                .collect(Collectors.toSet());
        for (MicroProcessChainBindEntity t : bindEntityList) {
            if (BomAndTechTypeEnum.STANDARD.getCode().equals(t.getTechType())) {
                standardIdSet.add(t.getTechId());
            } else if (BomAndTechTypeEnum.DRAFT.getCode().equals(t.getTechType())) {
                if (!standardSet.contains(t.getProductId())) {
                    draftIdSet.add(t.getTechId());
                }
            }
        }
        standardIdSet.addAll(draftIdSet);
        List<MicroProcessChainBindEntity> finalList = bindEntityList.stream().filter(b -> standardIdSet.contains(b.getTechId()))
                .sorted(Comparator.comparing(MicroProcessChainBindEntity::getSort,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
//        List<MicroProcessChainBindEntity> allList = new ArrayList<>();
//        try {
//            Map<Long, List<MicroProcessChainBindEntity>> collect = finalList.stream().collect(Collectors.groupingBy(MicroProcessChainBindEntity::getProductId));
//            for (Map.Entry<Long, List<MicroProcessChainBindEntity>> entry : collect.entrySet()) {
//                List<MicroProcessChainBindEntity> value = entry.getValue();
//                allList.addAll(sortChainByParallelProcess(value));
//            }
//        } catch (Exception e) {
//            log.error("并行的工艺链排序失败:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
//            allList = finalList;
//        } 
        return finalList;
    }

    /**
     * 对并行工序进行串行排序
     * 工序链的本质是倒置的多叉树,先拿到dfs结果,倒排下数组,然后将集合按照数组顺序排序
     *
     * @param toSortList 将要排序的数据集合
     */
    @Override
    public List<MicroProcessChainBindEntity> sortChainByParallelProcess(List<MicroProcessChainBindEntity> toSortList) {
        boolean existHead = false;
        boolean self2self = false;
        Set<Long> lastProcess = new HashSet<>();
        MicroProcessChainBindEntity rootNode = new MicroProcessChainBindEntity();
        HashMultimap<Long, MicroProcessChainBindEntity> preSortMap = HashMultimap.create();
        for (MicroProcessChainBindEntity t : toSortList) {
            if (t.getProcessId().equals(t.getParentProcessId())) {
                self2self = true;
            }
            if (t.getParentProcessId() == 0L) {
                existHead = true;
            }
            if (IsLastProcessEnum.YES.getCode().equals(t.getIsLastProcess())) {
                lastProcess.add(t.getProcessId());
                rootNode = t;
            }
            //在本次遍历里把后续要排序的数据准备好
            preSortMap.put(t.getProcessId(), t);
        }
        /*
        判断是否不存在头节点,是否自己指向自己,判断是否不存在尾节点或多尾序
        防止递归出现问题
         */
        if ((!existHead) || self2self || CollectionUtils.isEmpty(lastProcess) || lastProcess.size() > 1) {
            return toSortList;
        }
        ChainTreeNode root = new ChainTreeNode();
        Long rootProcessId = rootNode.getProcessId();
        root.setProcessId(rootProcessId);
        root.setDepth(0L);
        this.buildChainTree(toSortList, root, root.getProcessId(), root.getDepth());
        List<List<ChainTreeNode>> dfs = this.dfs(root);
        return sortHelper(preSortMap, dfs, rootProcessId);
    }

    @Override
    public List<String> selectAllProductByTech(MicroTechnology technology, List<Integer> patternList) {
        return microTechnologyMapper.selectAllProductByTech(technology, patternList);
    }

    /**
     * 排序辅助方法
     * 倒排节点
     *
     */
    private List<MicroProcessChainBindEntity> sortHelper(HashMultimap<Long, MicroProcessChainBindEntity> preSortMap, List<List<ChainTreeNode>> dfs, Long rootProcessId) {
        dfs.forEach(nodeList -> nodeList.removeIf(chainTreeNode -> chainTreeNode.getProcessId() == 0L));
        List<ChainTreeNode> newList = new ArrayList<>();
        for (List<ChainTreeNode> nodeList : dfs) {
            int size = nodeList.size();
            int i = 0;
            ChainTreeNode newNode;
            for (ChainTreeNode treeNode : nodeList) {
                newNode = new ChainTreeNode();
                newNode.setDepth((long) (size - i));
                newNode.setProcessId(treeNode.getProcessId());
                newList.add(newNode);
                i++;
            }
        }
        newList.sort(Comparator.comparing(ChainTreeNode::getDepth));
        List<MicroProcessChainBindEntity> result = new ArrayList<>();
        Set<MicroProcessChainBindEntity> rootList;
        List<MicroProcessChainBindEntity> rootNewList = new ArrayList<>();
        if (preSortMap.containsKey(rootProcessId)) {
            rootList = preSortMap.get(rootProcessId);
            rootNewList = BeanUtil.copyToList(rootList, MicroProcessChainBindEntity.class);
            preSortMap.removeAll(rootProcessId);
        }
        for (ChainTreeNode treeNode : newList) {
            Long processId = treeNode.getProcessId();
            if (preSortMap.containsKey(processId)) {
                result.addAll(preSortMap.get(processId));
            }
        }
        result.addAll(rootNewList);
        for (int i = 0; i < result.size(); i++) {
            MicroProcessChainBindEntity entity = result.get(i);
            entity.setSort(i);
        }
        return result;
    }

    /**
     * 深度遍历
     */
    public List<List<ChainTreeNode>> dfs(ChainTreeNode root) {
        List<List<ChainTreeNode>> res = new ArrayList<>();
        if (root == null) {
            return res;
        }
        dfsHelper(root, new ArrayList<>(), res);
        return res;
    }

    /**
     * 深度遍历辅助方法
     *
     */
    private void dfsHelper(ChainTreeNode node, List<ChainTreeNode> path, List<List<ChainTreeNode>> res) {
        ChainTreeNode treeNode = new ChainTreeNode();
        treeNode.setProcessId(node.getProcessId());
        treeNode.setDepth(node.getDepth());
        path.add(treeNode);
        if (node.getChildren().isEmpty()) {
            res.add(new ArrayList<>(path));
        } else {
            for (ChainTreeNode child : node.getChildren()) {
                dfsHelper(child, path, res);
            }
        }
        path.remove(path.size() - 1);
    }

    /**
     * 构建多叉树
     *
     */
    private void buildChainTree(List<MicroProcessChainBindEntity> toSortList, ChainTreeNode root, Long rootId, Long depth) {
        for (MicroProcessChainBindEntity t : toSortList) {
            if (t.getProcessId().equals(rootId)) {
                ChainTreeNode child = new ChainTreeNode();
                child.setProcessId(t.getParentProcessId());
                child.setDepth(depth + 1);
                root.getChildren().add(child);
                buildChainTree(toSortList, child, child.getProcessId(), child.getDepth());
            }
        }
    }

    @Data
    class ChainTreeNode {
        Long processId;
        Long depth;
        List<ChainTreeNode> children = new ArrayList<>();
    }

    /**
     * 工易派获取工艺链
     *
     */
    @Override
    public List<MicroSingleTechChainEntity> selectSingleChainByProduct(List<Long> productIds, List<String> productSeqs) {
        List<MicroProcessChainBindEntity> bindEntityList = this.selectMicroTechChainByProductIdsOrSeqs(productIds, productSeqs, null);
        HashMultimap<MicroSingleTechChainEntity, MicroSaveChainNodeEntity> chainMap = HashMultimap.create();
        bindEntityList.forEach(b -> chainMap.put(toSingleTech(b), toSingleChainNode(b)));
        List<MicroSingleTechChainEntity> finalList = new ArrayList<>();
        for (MicroSingleTechChainEntity chainEntity : chainMap.keySet()) {
            Set<MicroSaveChainNodeEntity> entitySet = chainMap.get(chainEntity);
            chainEntity.setProcessChainList(new ArrayList<>(entitySet).stream()
                    .sorted(Comparator.comparing(MicroSaveChainNodeEntity::getSort))
                    .collect(Collectors.toList()));
            finalList.add(chainEntity);
        }
        return finalList;
    }

    /**
     * 库易记模型转换工易派模型
     *
     */
    private MicroSingleTechChainEntity toSingleTech(MicroProcessChainBindEntity entity) {
        MicroSingleTechChainEntity tech = new MicroSingleTechChainEntity();
        tech.setTechId(entity.getTechId());
        tech.setProductId(entity.getProductId());
        tech.setProductSeq(entity.getProductSeq());
        tech.setProductName(entity.getProductName());
        tech.setTechType(entity.getTechType());
        tech.setTechName(entity.getTechName());
        return tech;
    }

    /**
     * 库易记模型转换工易派模型
     *
     */
    private MicroSaveChainNodeEntity toSingleChainNode(MicroProcessChainBindEntity entity) {
        MicroSaveChainNodeEntity singleChainNode = new MicroSaveChainNodeEntity();
        singleChainNode.setProcessId(entity.getProcessId());
        singleChainNode.setProcessSeq(entity.getProcessSeq());
        singleChainNode.setProcessName(entity.getProcessName());
        singleChainNode.setProcessCode(entity.getProcessCode());
        singleChainNode.setIsLastProcess(entity.getIsLastProcess());
        if (entity.getParentProcessSeq().equals(ROOT_PARENT_PROCESS)) {
            singleChainNode.setIsFirstProcess(IsFirstProcessEnum.YES.getCode());
        } else {
            singleChainNode.setIsFirstProcess(IsFirstProcessEnum.NO.getCode());
        }
        singleChainNode.setSort(entity.getSort());
        return singleChainNode;
    }

    /**
     * 组装工艺链查询实体
     *
     */
    private List<MicroProcessChainBindEntity> toChainQuery(MicroTechnology microTechnology) {
        List<MicroProcessChain> chainList = processChainService.selectMicroProcessChainListByTechId(microTechnology.getId());
        if (CollectionUtils.isEmpty(chainList)) {
            return Collections.emptyList();
        }
        return toProcessChainEntity(chainList);
    }

    private List<MicroProcessChainBindEntity> toProcessChainEntity(List<MicroProcessChain> chainList) {
        return BeanUtil.copyToList(chainList, MicroProcessChainBindEntity.class);
    }

    /**
     * 先将数据归档到历史表中
     * 再批量删除工艺链
     *
     * @param ids 需要删除的工艺链定义ID
     * @return 结果
     */
    @Override
    public int deleteMicroTechnologyByIds(Long[] ids) {
        microTechnologyMapper.saveTechnologyHistory(ids);
        return microTechnologyMapper.deleteMicroTechnologyByIds(ids);
    }

    /**
     * 先将数据归档到历史表中
     * 再删除工艺链
     *
     * @param id 工艺链定义ID
     * @return 结果
     */
    @Override
    public int deleteMicroTechnologyById(Long id) {
        microTechnologyMapper.saveTechnologyHistory(new Long[]{id});
        return microTechnologyMapper.deleteMicroTechnologyById(id);
    }

    /**
     * 根据某个工艺绑定工艺链
     *
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public MicroTechnologyBindResult bindTechAndChain(MicroTechnology technology, List<MicroProcessChain> chainList, MicroTechBindCondition condition) {
        String lockKey = "micro:update_process_chain:" + technology.getTenantCode();
        String lockUuid = IdUtils.fastSimpleUUID();
        boolean lock = redisLockHelper.lock(lockKey, lockUuid, 10, TimeUnit.SECONDS);
        if (!lock) {
            throw new CustomException("有其他用户在调整工艺路线，请核实后操作！");
        }
        Boolean standard = condition.getStandard();
        MicroTechnologyBindResult result;
        if (standard) {
            result = standardDeal(technology, chainList);
        } else {
            result = nonStandardDeal(technology, chainList, condition.getClone());
        }
        redisLockHelper.unlock(lockKey, lockUuid);
        return result;
    }

    /**
     * 标准工艺下确认工序的首尾序信息
     *
     */
    @Override
    public FirstOrLastProcess getFirstOrLastProcessFlagByCraftTech(String productSeq, String processSeq) {
        // 存储结果
        FirstOrLastProcess result = new FirstOrLastProcess();

        // 查询工艺主表获取工艺编码
        MicroTechnology queryParam = new MicroTechnology();
        queryParam.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        queryParam.setTechType(BomAndTechTypeEnum.STANDARD.getCode());
        queryParam.setProductSeq(productSeq);
        List<MicroTechnology> technologyList = this.selectMicroTechnologyList(queryParam);
        if (CollectionUtils.isEmpty(technologyList)) {
            throw new CustomException("该产品不存在标准工艺信息");
        }
        Long techId = technologyList.get(0).getId();

        // 根据工艺编码获取工序节点信息
        List<MicroProcessChain> processChainList = microProcessChainMapper.selectMicroProcessChainListByTechId(techId);
        if (CollectionUtils.isEmpty(processChainList)) {
            throw new CustomException("该产品不存在具体标准工艺信息");
        }
        List<MicroProcessChain> chainListByProcessSeq = processChainList.stream().filter(obj -> obj.getProcessSeq().equals(processSeq))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(chainListByProcessSeq)) {
            throw new CustomException("该产品标准工艺中不存在该工序");
        }

        chainListByProcessSeq.forEach(obj -> {
            // 如果尾序字段为null，则设置为1
            if (obj.getIsLastProcess() == null) {
                obj.setIsLastProcess(CommonConstants.NO);
            }
        });
        Optional<Long> isLastProcessOptional = chainListByProcessSeq.stream().map(obj -> Long.parseLong(obj.getIsLastProcess())).min(Long::compareTo);
        isLastProcessOptional.ifPresent(aLong -> result.setIsLastProcess(aLong.toString()));

        if (chainListByProcessSeq.stream().anyMatch(obj -> CommonConstants.ROOT_PROCESS_SEQ.equals(obj.getParentProcessSeq()))) {
            result.setIsFirstProcess(CommonConstants.YES);
        } else {
            result.setIsFirstProcess(CommonConstants.NO);
        }
        return result;
    }

    @Override
    public Map<String, Set<MicroSelectEntity>> recommendByProductOrProcess(String productSeq) {
        Map<String, Set<MicroSelectEntity>> result = new HashMap<>(8);
//        List<MicroSelectEntity> list;
//        if (operateProcessCode == null && preProcessCode == null) {
        List<MicroProcessChainRecommendEntity> recommendEntityList = microTechnologyMapper.recommendByProduct(productSeq);
        Map<String, List<MicroProcessChainRecommendEntity>> map = recommendEntityList.stream().collect(Collectors.groupingBy(MicroProcessChainRecommendEntity::getProductSeq));
        Set<MicroSelectEntity> operateProcessList = new HashSet<>();
        Set<MicroSelectEntity> preProcessList = new HashSet<>();
        MicroSelectEntity operateEntity;
        MicroSelectEntity preEntity;
        for (MicroProcessChainRecommendEntity recommend : recommendEntityList) {
            String operateSeq = recommend.getOperateProcessSeq();
            String operateName = recommend.getOperateProcessName();
            String operateCode = recommend.getOperateProcessCode();
            String preSeq = recommend.getPreProcessSeq();
            String preName = recommend.getPreProcessName();
            String preCode = recommend.getPreProcessCode();
            if (StringUtils.hasText(operateSeq)) {
                operateEntity = new MicroSelectEntity();
                operateEntity.setItemSeq(operateSeq);
                operateEntity.setItemCode(operateCode);
                operateEntity.setItemName(operateName);
                operateProcessList.add(operateEntity);
            }
            if (StringUtils.hasText(preSeq)) {
                preEntity = new MicroSelectEntity();
                preEntity.setItemSeq(preSeq);
                preEntity.setItemCode(preCode);
                preEntity.setItemName(preName);
                preProcessList.add(preEntity);
            }
        }
        result.put(HotKeyEnum.OPERATE_PROCESS.getDesc(), operateProcessList);
        result.put(HotKeyEnum.PRE_PROCESS.getDesc(), preProcessList);
//        } else if (operateProcessCode == null) {
//            list = microTechnologyMapper.recommendByProcess(productCode, null, preProcessCode);
//            result.put(HotKeyEnum.OPERATE_PROCESS.getDesc(), new HashSet<>(list));
//        } else if (preProcessCode == null) {
//            list = microTechnologyMapper.recommendByProcess(productCode, operateProcessCode, null);
//            result.put(HotKeyEnum.PRE_PROCESS.getDesc(), new HashSet<>(list));
//        } 
        return result;
    }

    /**
     * 某道工序追加到工艺链,放在尾序
     *
     */
    @Override
    public boolean appandTailTech(MicroSingleTechChainEntity chainEntity) {
        //暂时注释2023年03月17日16:51:56
//        String productSeq = chainEntity.getProductSeq();
//        if (productSeq == null) {
//            throw new CustomException("无法获取产品信息");
//        }
//        List<MicroSaveChainNodeEntity> processChainList = chainEntity.getProcessChainList();
//        if (CollectionUtils.isEmpty(processChainList)) {
//            throw new CustomException("无法获取工序信息");
//        }
//        // 根据产品查询工艺链信息
//        List<MicroSingleTechChainEntity> techList = microTechnologyMapper.selectSingleTechChainByProductIdsOrSeqs(null, Collections.singletonList(productSeq));
//        if (CollectionUtils.isEmpty(techList)) {
//            throw new CustomException("无法获取工艺信息");
//        }
//        List<MicroSingleTechChainEntity> draftList = techList.stream().filter(t -> "DRAFT".equals(t.getTechType())).collect(Collectors.toList());
//        if (CollectionUtils.isEmpty(draftList)) {
//            throw new CustomException("无法找到对应的工艺信息");
//        }
//        MicroSingleTechChainEntity draftTech = draftList.get(0);
//        // 追加到工艺链尾部
//        Long appendTechId = draftTech.getTechId();
//        List<MicroSaveChainNodeEntity> draftChain = draftTech.getProcessChainList();
//        MicroSaveChainNodeEntity tailProcess = draftChain.stream().filter(b -> IsLastProcessEnum.YES.getCode().equals(b.getIsLastProcess())).findFirst().orElse(null);
//        if (tailProcess == null) {
//            throw new CustomException("无法获取工艺链工序信息");
//        }
//        //将原来的尾序摘掉尾序标识
//        MicroProcessChain oldTail = new MicroProcessChain();
//        oldTail.setId(tailProcess.getProcessId());
//        oldTail.setIsLastProcess(IsLastProcessEnum.NO.getCode());
//        microProcessChainMapper.updateMicroProcessChain(oldTail);
//        List<MicroProcessChain> microProcessChains = sortChainList(draftChain);
//        //将新的工序链头节点接入原尾序节点,所有新工序链塞入原工艺ID
//        MicroProcessChain indexChain = microProcessChains.get(0);
//        indexChain.setParentProcessId(tailProcess.getProcessId());
//        indexChain.setParentProcessSeq(tailProcess.getProcessSeq());
//        microProcessChains.add(0, indexChain);
//        microProcessChains.forEach(c -> c.setTechId(appendTechId));
//        microProcessChainMapper.insertBatch(microProcessChains); 
        return false;
    }

    /**
     * 提供保存单条工艺链服务
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSingleTechAndChain(MicroSingleTechChainEntity singleTechChain) {
        List<MicroSaveChainNodeEntity> processChainList = singleTechChain.getProcessChainList();
        List<MicroProcessChain> chainList = sortChainList(processChainList);
        MicroTechnology technology = new MicroTechnology();
        technology.setTechType(singleTechChain.getTechType());
        technology.setProductSeq(singleTechChain.getProductSeq());
        technology.setProductId(singleTechChain.getProductId());
        technology.setProductName(singleTechChain.getProductName());
        List<MicroTechnology> technologyList = microTechnologyMapper.selectMicroTechnologyList(technology);
        Long[] techIds;
        if (technologyList.isEmpty()) {
            techIds = new Long[0];
        } else {
            techIds = technologyList.stream().map(MicroTechnology::getId).toArray(Long[]::new);
        }
        technology.setTechType(singleTechChain.getTechType());
        technology.setTechPattern(singleTechChain.getTechPattern());
        insertNewTechAndChain(Collections.singletonList(technology), chainList, techIds);
        return true;
    }

    /**
     * 保存工艺和工艺链
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTechAndChain(MicroSingleTechChainEntity param) {

        // 1.组装工艺基础信息
        MicroTechnology technology = new MicroTechnology();
        technology.setTechType(param.getTechType());
        technology.setProductSeq(param.getProductSeq());
        technology.setProductId(param.getProductId());
        technology.setProductName(param.getProductName());
        List<MicroTechnology> technologyList = microTechnologyMapper.selectMicroTechnologyList(technology);
        Long[] techIds;
        if (technologyList.isEmpty()) {
            techIds = new Long[0];
        } else {
            techIds = technologyList.stream().map(MicroTechnology::getId).toArray(Long[]::new);
        }
        technology.setTechType(param.getTechType());
        technology.setTechPattern(param.getTechPattern());

        // 2.组装工艺链信息
        List<MicroProcessChain> chainList = Lists.newArrayList();
        List<MicroSaveChainNodeEntity> processChainList = param.getProcessChainList();
        switch (TechPatternEnum.getEnum(technology.getTechPattern())) {
            case SERIAL: // 顺序 
                processChainList = processChainList.stream().sorted(Comparator.comparing(MicroSaveChainNodeEntity::getSort)).collect(Collectors.toList());
                chainList = sortChainList(processChainList);
                break;
            case UN_ORDER: // 乱序 
                chainList = processChainList.stream().map(e -> {
                    String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
                    Long userId = SecurityUtils.getUserId();
                    MicroProcessChain processChain = new MicroProcessChain();
                    processChain.setProcessId(e.getProcessId());
                    processChain.setProcessSeq(e.getProcessSeq());
                    processChain.setProcessCode(e.getProcessCode());
                    processChain.setProcessName(e.getProcessName());
                    processChain.setTenantCode(tenantCode);
                    processChain.setCreatedBy(String.valueOf(userId));
                    processChain.setLastUpdBy(String.valueOf(userId));

                    processChain.setParentProcessId(-1L);
                    processChain.setParentProcessSeq("-1");
                    if (IsFirstProcessEnum.YES.getCode().equals(e.getIsFirstProcess())) { // 首序
                        processChain.setParentProcessId(0L);
                        processChain.setParentProcessSeq(CommonConstants.ROOT_PROCESS_SEQ);
                    }
                    if (IsLastProcessEnum.YES.getCode().equals(e.getIsLastProcess())) { // 尾序
                        processChain.setIsLastProcess(IsLastProcessEnum.YES.getCode());
                    } else {
                        processChain.setIsLastProcess(IsLastProcessEnum.NO.getCode());
                    }
                    return processChain;
                }).collect(Collectors.toList());
                break;

            default:
                break;
        }

        // 3.新增工艺+工艺链
        insertNewTechAndChain(Collections.singletonList(technology), chainList, techIds);
    }

    private List<MicroProcessChain> sortChainList(List<MicroSaveChainNodeEntity> processChainList) {
        List<MicroProcessChain> chainList = new ArrayList<>();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        Long userId = SecurityUtils.getUserId();
        int i = 0;
        int size = processChainList.size();
        MicroProcessChain processChain;
        MicroSaveChainNodeEntity parentNode;
        MicroSaveChainNodeEntity childNode;

        while (i < size) {
            childNode = processChainList.get(i);
            processChain = new MicroProcessChain();
            if (i > 0) {
                parentNode = processChainList.get(i - 1);
                processChain.setParentProcessId(parentNode.getProcessId());
                processChain.setParentProcessSeq(parentNode.getProcessSeq());
            } else {
                processChain.setParentProcessId(0L);
                processChain.setParentProcessSeq(CommonConstants.ROOT_PROCESS_SEQ);
            }
            if (i == size - 1) {
                processChain.setIsLastProcess(IsLastProcessEnum.YES.getCode());
            } else {
                processChain.setIsLastProcess(IsLastProcessEnum.NO.getCode());
            }
            processChain.setProcessId(childNode.getProcessId());
            processChain.setProcessSeq(childNode.getProcessSeq());
            processChain.setProcessCode(childNode.getProcessCode());
            processChain.setTenantCode(tenantCode);
            processChain.setCreatedBy(String.valueOf(userId));
            processChain.setLastUpdBy(String.valueOf(userId));
            chainList.add(processChain);
            i++;
        }
        return chainList;
    }

    /**
     * 标准工艺处理流程
     *
     */
    private MicroTechnologyBindResult standardDeal(MicroTechnology technology, List<MicroProcessChain> chainList) {
        MicroTechnologyBindResult result = new MicroTechnologyBindResult();
        boolean equal = false;
        //标准工艺
        Long techId = technology.getId();
        List<MicroProcessChain> oldChainList = processChainService.selectMicroProcessChainListByTechId(techId);
        if (!CollectionUtils.isEmpty(oldChainList)) {
            equal = compareTwoList(oldChainList, chainList);
        }
        if (!equal) {
            /*
            编辑过
            创建新工艺绑定到该产品
            并找到相同标准工艺链的产品集合(coverList)
             */
            this.insertNewTechAndChain(Collections.singletonList(technology), chainList, Stream.of(techId).toArray(Long[]::new));
            result.setCoverProductList(this.sameProcessChainProduct(technology, oldChainList));
        }
        return result;
    }

    /**
     * 相同标准工艺链的产品集合(coverList)
     *
     */
    private List<MicroTechBindProductDomain> sameProcessChainProduct(MicroTechnology technology, List<MicroProcessChain> oldChainList) {
        try {
            List<MicroTechDiffDomain> techDiffDomains = microTechnologyMapper.selectTechAllChainForCompare();
            return getExtraProductList(technology.getProductId(), oldChainList, techDiffDomains);
        } catch (Exception ignored) {
            log.error("Failed to get the same process chain product:{}:{}, Error:{}", ignored.getClass().getName(), ignored.getLocalizedMessage(), ignored.getStackTrace()[0].toString(), ignored);
        }
        return Collections.emptyList();

    }

    /**
     * 非标准工艺处理流程
     *
     */
    private MicroTechnologyBindResult nonStandardDeal(MicroTechnology technology, List<MicroProcessChain> chainList, Boolean clone) {
        MicroTechnologyBindResult result = new MicroTechnologyBindResult();
        /*
        拷贝标准工艺后,无论编辑与否
        都需要新建工艺与产品关系,插入工艺链
         */
        Long techId = technology.getId();
        Long[] techIds;
        if (techId == null) {
            techIds = new Long[0];
        } else {
            techIds = Stream.of(techId).toArray(Long[]::new);
        }
        this.insertNewTechAndChain(Collections.singletonList(technology), chainList, techIds);
        //非标准工艺
        if (!clone && !CollectionUtils.isEmpty(chainList)) {
            result.setSimilarProductList(getNonStandardSimilarProduct(technology.getProductId(), chainList));
        }
        return result;
    }

    /**
     * 获取非标准工艺下相似的工艺链产品集合
     *
     */
    private List<MicroTechBindProductDomain> getNonStandardSimilarProduct(Long productId, List<MicroProcessChain> chainList) {
        List<MicroTechBindProductDomain> similarProductList = new ArrayList<>();
        try {
            // 所有非标标准工艺的产品下processId,parentProcessId
            List<MicroTechSimilarDomain> mixedList = microTechnologyMapper.selectNonStandardProductTechChain();
            if (CollectionUtils.isEmpty(mixedList)) {
                return Collections.emptyList();
            } else {
                mixedList = mixedList.stream().filter(m -> !productId.equals(m.getProductId())).collect(Collectors.toList());
            }
            MicroTechSimilarDomain domain;
            String parentProcessSeq;
            HashMultimap<MicroTechSimilarDomain, String> mixedMap = HashMultimap.create();
            List<String> parentProcessSeqList = new ArrayList<>();
            for (MicroTechSimilarDomain similarDomain : mixedList) {
                parentProcessSeq = similarDomain.getParentProcessSeq();
                if (parentProcessSeq.contains(",")) {
                    String[] parentProcessSeqs = parentProcessSeq.split(",");
                    parentProcessSeqList = Stream.of(parentProcessSeqs).collect(Collectors.toList());
                } else {
                    parentProcessSeqList.add(parentProcessSeq);
                }
                for (String seq : parentProcessSeqList) {
                    if ("0".equals(seq)) {
                        continue;
                    }
                    domain = new MicroTechSimilarDomain();
                    domain.setProductId(similarDomain.getProductId());
                    domain.setProductCode(similarDomain.getProductCode());
                    domain.setProductSeq(similarDomain.getProductSeq());
                    domain.setProductName(similarDomain.getProductName());
                    mixedMap.put(domain, similarDomain.getProcessSeq() + "&" + seq);
                }
                parentProcessSeqList.clear();
            }
            Set<String> oldProcessChain = chainList.stream().filter(c -> c.getParentProcessId() != 0L).map(o -> o.getProcessSeq() + "&" + o.getParentProcessSeq()).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(oldProcessChain)) {
                return similarProductList;
            }
            MicroTechBindProductDomain productDomain;
            Set<String> mixedProcessChain;
            for (MicroTechSimilarDomain similarDomain : mixedMap.keySet()) {
                mixedProcessChain = mixedMap.get(similarDomain);
                if (oldProcessChain.equals(mixedProcessChain)) {
                    productDomain = new MicroTechBindProductDomain();
                    BeanUtils.copyProperties(similarDomain, productDomain);
                    similarProductList.add(productDomain);
                }
            }
        } catch (Exception ignored) {
            log.error("Failed to get the similar process chain product:{}:{}, Error:{}", ignored.getClass().getName(), ignored.getLocalizedMessage(), ignored.getStackTrace()[0].toString(), ignored);

        }
        return similarProductList;
    }

    /**
     * 新建工艺与产品绑定关系,绑定工艺链
     *
     */
    private void insertNewTechAndChain(List<MicroTechnology> technologyList, List<MicroProcessChain> chainList, Long[] techIds) {
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        List<MicroProcessChain> addChainList = new ArrayList<>();
        List<MicroTechnology> addTechList = new ArrayList<>();
        if (techIds != null && techIds.length > 0) {
            deleteMicroTechnologyByIds(techIds);
            processChainService.deleteMicroProcessChainByTechIds(techIds);
        }
        if (!CollectionUtils.isEmpty(chainList)) {
            List<MicroProcessChain> cloneChainList;
            for (MicroTechnology technology : technologyList) {
                cloneChainList = BeanUtil.copyToList(chainList, MicroProcessChain.class);
                long nextId = CodeGenerateUtils.getInstance().genCode();
                technology.setId(nextId);
                String techCode = "TECH" + nextId;
                technology.setTechCode(techCode);
                technology.setTechName(getTechName(technology));
                technology.setTechDesc(techCode);
                technology.setTechVersion(techCode);
                technology.setRelationType("PRODUCT");
                technology.setTenantCode(tenantCode);
                technology.setCreatedBy(String.valueOf(userId));
                technology.setLastUpdBy(String.valueOf(userId));
                technology.setTechType(StringUtils.isEmpty(technology.getTechType()) ? BomAndTechTypeEnum.STANDARD.getCode() : technology.getTechType());
                addTechList.add(technology);
                cloneChainList = this.sortChainByPattern(cloneChainList, technology);
                for (int i = 0; i < cloneChainList.size(); i++) {
                    MicroProcessChain c = cloneChainList.get(i);
                    c.setTechId(nextId);
                    c.setTenantCode(tenantCode);
                    c.setCreatedBy(String.valueOf(userId));
                    c.setLastUpdBy(String.valueOf(userId));
                    // 顺序/推荐等工艺 sortChainByPattern 不生成 sort，此处按落库顺序补齐，
                    // 保证工艺链查询排序稳定（UN_ORDER 已设置 sort 的不覆盖）
                    if (c.getSort() == null) {
                        c.setSort(i);
                    }
                }
                addChainList.addAll(cloneChainList);
            }
            try {
                microTechnologyMapper.insertMicroTechnologyBatch(addTechList);
            } catch (DuplicateKeyException e) {
                log.error("插入工艺表主键冲突:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            processChainService.insertMicroProcessChainBatch(addChainList);
        }
    }

    private String getTechName(MicroTechnology technology) {
        String productName = StringUtils.isEmpty(technology.getProductName()) ? "" : technology.getProductName();
        BomAndTechTypeEnum typeEnum = BomAndTechTypeEnum.getEnum(technology.getTechType());
        String typeName = typeEnum == null ? "工艺" : typeEnum.getDesc() + "工艺";
        return productName + typeName;
    }

    private List<MicroProcessChain> sortChainByPattern(List<MicroProcessChain> toSortChainList, MicroTechnology bindTech) {
        Integer techPattern = bindTech.getTechPattern();
        TechPatternEnum patternEnum = TechPatternEnum.getEnum(techPattern);
        Assert.notNull(patternEnum, "无法获取工艺形式");
        switch (patternEnum) {
            case SERIAL:
                //如果是顺序(顺序 并序),排序后插入
            case RECOMMEND:
            case ISSUED_ORDER:
                //未确认的包含(推荐的  工易派保存的草稿)
            case INITIAL:
                //期初库存导入的
                List<MicroProcessChainBindEntity> entityList = this.sortChainByParallelProcess(BeanUtil.copyToList(toSortChainList, MicroProcessChainBindEntity.class));
                toSortChainList = BeanUtil.copyToList(entityList, MicroProcessChain.class);
                break;
            case UN_ORDER:
                //如果是无序,只确认前中后三个顺序
                Integer first = 0;
                Integer middle = 1;
                Integer last = 2;
                for (MicroProcessChain chain : toSortChainList) {
                    chain.setSort(middle);
                    if (IsLastProcessEnum.YES.getCode().equals(chain.getIsLastProcess())) {
                        chain.setSort(last);
                    }
                    if (CommonConstants.ROOT_PROCESS_SEQ.equals(chain.getParentProcessSeq())) {
                        chain.setSort(first);
                    }
                }
                break;
            default:
                break;
        }
        return toSortChainList;
    }

    /**
     * 获取除了当前修改的工艺链之外,还有哪些产品与修改前的工艺链相同
     *
     */
    private List<MicroTechBindProductDomain> getExtraProductList(Long productId, List<MicroProcessChain> oldChainList, List<MicroTechDiffDomain> techDiffDomains) {
        Set<String> oldChain = oldChainList.stream().map(o -> o.getProcessId() + "&" + o.getParentProcessId() + "&" + o.getProcessSeq() + "&" + o.getParentProcessSeq() + "&" + (StringUtils.isEmpty(o.getIsLastProcess()) ? "1" : o.getIsLastProcess())).collect(Collectors.toSet());
        List<MicroTechBindProductDomain> productList = new ArrayList<>();
        MicroTechBindProductDomain product;
        for (MicroTechDiffDomain diffDomain : techDiffDomains) {
            if (diffDomain.getProductId().equals(productId)) {
                continue;
            }
            HashSet<String> chainStrSet = new HashSet<>(diffDomain.getChainStrArray());
            if (oldChain.equals(chainStrSet)) {
                product = new MicroTechBindProductDomain();
                product.setProductId(diffDomain.getProductId());
                product.setProductSeq(diffDomain.getProductSeq());
                product.setProductCode(diffDomain.getProductCode());
                product.setProductName(diffDomain.getProductName());
                productList.add(product);
            }
        }
        return productList;
    }

    private boolean compareTwoList(List<MicroProcessChain> oldChainList, List<MicroProcessChain> chainList) {
        if (oldChainList.size() != chainList.size()) {
            return false;
        }
        return oldChainList.stream().map(o -> o.getProcessId() + o.getParentProcessId() + o.getProcessSeq() + o.getParentProcessSeq() + o.getIsLastProcess()).collect(Collectors.toSet()).equals(chainList.stream().map(o -> o.getProcessId() + o.getParentProcessId() + o.getProcessSeq() + o.getParentProcessSeq() + o.getIsLastProcess()).collect(Collectors.toSet()));
    }

    @Override
    public List<MicroStandardEntity> selectStandardPro(String key) {
        return microTechnologyMapper.selectStandardProByKey(key);
    }

    /**
     * 这里同时处理了标准工艺与非标准工艺产品的工艺链绑定
     * 先查询出要覆盖产品集合中的所有标准工艺ID,删除旧工艺及工艺链
     * 对每个产品重新赋值新工艺,绑定新工艺链
     *
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public int coverOrSimilar(List<MicroTechnology> technologyList, List<MicroProcessChain> chainList) {
        //如果是覆盖, 说明产品均已存在标准工艺, 删除工艺表及工艺链数据
        List<Long> ids = microTechnologyMapper.selectTechIdsByProductIdAndSeq(technologyList, BomAndTechTypeEnum.STANDARD.getCode());
        Long[] techIds = new Long[0];
        if (!CollectionUtils.isEmpty(ids)) {
            techIds = ids.toArray(new Long[0]);
        }
        //插入工艺表和工艺链
        this.insertNewTechAndChain(technologyList, chainList, techIds);
        return 1;
    }

    @Override
    public List<MicroTechnology> judgeStandardOrProduct(String productCode, String productSeq, Long productId) {
        return microTechnologyMapper.judgeStandardProduct(productCode, productSeq, productId);
    }

    /**
     * 选出一条工艺
     *
     */
    @Override
    public List<MicroProcessChainBindEntity> selectSingleMicroChainList(List<Long> productIds, List<String> productSeqs, String techType) {
        List<MicroProcessChainBindEntity> bindEntityList = this.selectMicroTechChainByProductIdsOrSeqs(productIds, productSeqs, techType);
        if (CollectionUtils.isEmpty(bindEntityList)) {
            return Collections.emptyList();
        }
        Map<Long, List<MicroProcessChainBindEntity>> techMap = bindEntityList.stream()
                .sorted(Comparator.comparing(MicroProcessChainBindEntity::getSort))
                .collect(Collectors.groupingBy(MicroProcessChainBindEntity::getTechId));

        // 只有一条工艺链
        if (techMap.size() >= TECH_CHAIN_MIN_COUNT) {
            // 有标准工艺则返回标准工艺链
            for (Map.Entry<Long, List<MicroProcessChainBindEntity>> entry : techMap.entrySet()) {
                if (entry.getValue().get(0).getTechType().equals(BomAndTechTypeEnum.STANDARD.getCode())) {
                    return entry.getValue();
                }
            }
        }
        // 否则返回最新
        return bindEntityList;
    }

    /**
     * 判断是否为标准产品
     *
     */
    @Override
    public Set<String> judgeStandardProductByList(List<Long> productIds, List<String> productSeqs) {
        return microTechnologyMapper.judgeStandardProductByList(productIds, productSeqs);
    }

    @Override
    public List<MicroProductProcessEntity> getPreProcessByStandardProductAndProcess(List<MicroProductProcessEntity> collect) {
        List<MicroProductProcessEntity> result = new ArrayList<>();
// 标准工序找到对应的前工序,并且找到是否首尾序
        Map<String, Set<String>> rawProductProcess = collect.stream().collect(Collectors.groupingBy(MicroProductProcessEntity::getProductSeq,
                Collectors.collectingAndThen(Collectors.toList(), value -> value.stream().map(MicroProductProcessEntity::getOperateProcessSeq).collect(Collectors.toSet()))));
        List<MicroProcessChainBindEntity> productProcessList = this.selectMicroTechChainByProductIdsOrSeqs(null,
                new ArrayList<>(rawProductProcess.keySet()),
                BomAndTechTypeEnum.STANDARD.getCode());
        if (!CollectionUtils.isEmpty(productProcessList)) {
            HashBasedTable<String, String, List<MicroProcessChainBindEntity>> chainTable = HashBasedTable.create();
            List<MicroProcessChainBindEntity> entityList;
            String productSeq;
            String processSeq;
            for (MicroProcessChainBindEntity chainBindEntity : productProcessList) {
                productSeq = chainBindEntity.getProductSeq();
                processSeq = chainBindEntity.getProcessSeq();
                if (chainTable.contains(productSeq, processSeq)) {
                    entityList = chainTable.get(productSeq, processSeq);
                } else {
                    entityList = new ArrayList<>();
                }
                entityList.add(chainBindEntity);
                chainTable.put(productSeq, processSeq, entityList);
            }
            for (Map.Entry<String, Map<String, List<MicroProcessChainBindEntity>>> chainEntry : chainTable.rowMap().entrySet()) {
                productSeq = chainEntry.getKey();
                Map<String, List<MicroProcessChainBindEntity>> chainValue = chainEntry.getValue();
                if (rawProductProcess.containsKey(productSeq)) {
                    Set<String> operateProcessSeqList = rawProductProcess.get(productSeq);
                    MicroProductProcessEntity processEntity;
                    for (String operateProcessSeq : operateProcessSeqList) {
                        if (chainValue.containsKey(operateProcessSeq)) {
                            List<MicroProcessChainBindEntity> operateProcessList = chainValue.get(operateProcessSeq);
                            MicroProcessChainBindEntity operateProcess = operateProcessList.get(0);
                            processEntity = new MicroProductProcessEntity();
                            processEntity.setPreProcessId(operateProcess.getParentProcessId());
                            processEntity.setProductSeq(operateProcess.getProductSeq());
                            processEntity.setOperateProcessSeq(operateProcess.getProcessSeq());
                            long count = operateProcessList.stream().filter(o -> CommonConstants.ROOT_PROCESS_SEQ.equals(o.getParentProcessSeq())).count();
                            // 如果存在多条,并且均为前工序,取一条即可
                            if (count == operateProcessList.size()) {
                                // 当前工序均为前工序,取一个即可
                                processEntity.setIsFirstProcess(true);
                            } else {
                                Set<String> preProcessSet = operateProcessList.stream()
                                        .map(MicroProcessChainBindEntity::getParentProcessSeq)
                                        .collect(Collectors.toSet());
                                List<MicroProcessChainBindEntity> preProcessList = chainValue.values().stream()
                                        .flatMap(Collection::stream)
                                        .filter(entity -> preProcessSet.contains(entity.getProcessSeq()))
                                        .collect(Collectors.toList());
                                this.assembleProcess(processEntity, preProcessList);
                            }
                            processEntity.setIsLastProcess(IsLastProcessEnum.YES.getCode().equals(operateProcess.getIsLastProcess()));
                            result.add(processEntity);
                        }

                    }
                }
            }
        }
        return result;
    }

    /**
     * 根据多个产品推荐现工序
     *
     */
    @Override
    public Map<String, List<MicroSelectEntity>> recommendOperateProcessByProductList(List<String> productList) {
        Map<String, List<MicroSelectEntity>> result = new HashMap<>(8);
        List<MicroProcessChainRecommendEntity> recommendEntityList = microTechnologyMapper.recommendOperateProcessByProduct(productList);
        Map<String, List<MicroProcessChainRecommendEntity>> map = recommendEntityList.stream().collect(Collectors.groupingBy(MicroProcessChainRecommendEntity::getProductSeq));
        List<MicroSelectEntity> operateProcessList;
        MicroSelectEntity operateEntity;
        for (Map.Entry<String, List<MicroProcessChainRecommendEntity>> entry : map.entrySet()) {
            String productSeq = entry.getKey();
            recommendEntityList = entry.getValue();
            operateProcessList = new ArrayList<>();
            for (MicroProcessChainRecommendEntity recommend : recommendEntityList) {
                String operateSeq = recommend.getOperateProcessSeq();
                String operateName = recommend.getOperateProcessName();
                String operateCode = recommend.getOperateProcessCode();
                if (StringUtils.hasText(operateSeq)) {
                    operateEntity = new MicroSelectEntity();
                    operateEntity.setItemSeq(operateSeq);
                    operateEntity.setItemCode(operateCode);
                    operateEntity.setItemName(operateName);
                    operateProcessList.add(operateEntity);
                }
            }
            result.put(productSeq, operateProcessList);
        }
        return result;
    }

    private void assembleProcess(MicroProductProcessEntity processEntity, List<MicroProcessChainBindEntity> preProcessList) {
        StringBuilder seq = new StringBuilder();
        StringBuilder code = new StringBuilder();
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < preProcessList.size(); i++) {
            MicroProcessChainBindEntity entity = preProcessList.get(i);
            seq.append(entity.getProcessSeq());
            code.append(entity.getProcessCode());
            name.append(entity.getProcessName());
            if (i < preProcessList.size() - 1) {
                seq.append(",");
                code.append(",");
                name.append(",");
            }
        }
        processEntity.setPreProcessSeq(seq.toString());
        processEntity.setPreProcessCode(code.toString());
        processEntity.setPreProcessName(name.toString());
    }

    /**
     * 校验产品是否有标准工艺，如果有标准工艺判断工序是否符合工艺路线
     *
     */
    @Override
    public boolean validSubmitRecordTechInfo(String productSeq, String processSeq, String preProcessSeq) {
        if (productSeq != null) {
            // 获取工艺信息
            List<String> productSeqList = new ArrayList<>();
            productSeqList.add(productSeq);
            List<MicroProcessChainBindEntity> bindEntityList = microTechnologyMapper.selectMicroTechChainByProductIdsOrSeqs(Collections.emptyList(), productSeqList, BomAndTechTypeEnum.STANDARD.getCode());
            if (!CollectionUtils.isEmpty(bindEntityList)) {
                // 有标准工艺，校验报工记录是否符合工艺路线
                return this.judgeSubmitRecordBelongToStandardTech(bindEntityList, processSeq, preProcessSeq);
            }
        }
        return true;
    }

    /**
     * 根据产品或产品+现工序取得标准/草稿工艺中的工序范围列表
     *
     */
    @Override
    public MicroProcessMixedResultEntity selectRangeProcessInTechByProductOrOperateProcess(String productSeq, String operateProcessSeq) {
        MicroProcessMixedResultEntity resultEntity = this.judgeProductStandardOrDraft(productSeq);
        boolean operateProcessEmpty = operateProcessEmpty(operateProcessSeq);
        List<MicroSelectEntity> extraList;
        //根据产品    推荐工序
        if (operateProcessEmpty) {
            if (resultEntity.isStandard()) {
                //存在标准工艺   只能在工艺链范围内选择
                resultEntity.setResultList(getSelectListFromProcessChainBind(productSeq, BomAndTechTypeEnum.STANDARD.getCode()));
                resultEntity.setFinalReturn(true);
                return resultEntity;
            } else if (resultEntity.isDraft()) {
                //草稿工艺      优先推荐草稿工序
                extraList = getSelectListFromProcessChainBind(productSeq, BomAndTechTypeEnum.DRAFT.getCode());
                resultEntity.setResultList(extraList);
            }
        } else {
            //根据产品+现工序    推荐前工序,标准顺序工艺只推前一个,其余推所有
            if (resultEntity.isStandard() || resultEntity.isDraft()) {
                //标准顺序工艺只推前一个
                MicroProductProcessEntity processEntity = new MicroProductProcessEntity();
                processEntity.setProductSeq(productSeq);
                processEntity.setOperateProcessSeq(operateProcessSeq);
                if (resultEntity.isSerial()) {
                    List<MicroProductProcessEntity> processList = this.getPreProcessByStandardProductAndProcess(Collections.singletonList(processEntity));
                    resultEntity.setResultList(getSelectListFromProcessEntity(processList));
                    resultEntity.setFinalReturn(true);
                    return resultEntity;
                } else {
                    //其余推所有
                    extraList = getSelectListFromProcessChainBind(productSeq, null);
                    extraList = extraList.stream().filter(t -> !operateProcessSeq.equals(t.getItemSeq())).collect(Collectors.toList());
                    resultEntity.setResultList(extraList);
                }
            }
        }
        return resultEntity;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int removeTechByProduct(List<Long> productIds) {
        List<MicroProcessChainBindEntity> processChainList = microTechnologyMapper.selectMicroTechChainByProductIdsOrSeqs(productIds, null, null);
        if (CollectionUtils.isEmpty(processChainList)) {
            return 1;
        }
        List<Long> techIds = processChainList.stream().map(MicroProcessChainBindEntity::getTechId).collect(Collectors.toList());
        for (MicroProcessChainBindEntity entity : processChainList) {
            techIds.add(entity.getTechId());
        }
        Long[] ids = techIds.toArray(new Long[0]);
        microTechnologyMapper.deleteMicroTechnologyByIds(ids);
        microProcessChainMapper.deleteByTechIds(ids);
        return 1;
    }

    /**
     * 转换MicroSelectEntity
     *
     */
    private List<MicroSelectEntity> getSelectListFromProcessEntity(List<MicroProductProcessEntity> processList) {
        List<MicroSelectEntity> selectList = new ArrayList<>();
        MicroSelectEntity select;
        for (MicroProductProcessEntity bind : processList) {
            select = new MicroSelectEntity();
            select.setItemSeq(bind.getPreProcessSeq());
            select.setItemId(bind.getPreProcessId());
            select.setItemCode(bind.getPreProcessCode());
            select.setItemName(bind.getPreProcessName());
            selectList.add(select);
        }
        return selectList;
    }

    /**
     * 转换MicroSelectEntity
     *
     */
    private List<MicroSelectEntity> getSelectListFromProcessChainBind(String productSeq, String techType) {
        List<MicroSelectEntity> selectList = new ArrayList<>();
        List<MicroProcessChainBindEntity> standardChain = this.selectMicroTechChainByProductIdsOrSeqs
                (null, Collections.singletonList(productSeq), techType);
        MicroSelectEntity select;
        for (MicroProcessChainBindEntity bind : standardChain) {
            select = new MicroSelectEntity();
            select.setItemSeq(bind.getProcessSeq());
            select.setItemId(bind.getProcessId());
            select.setItemCode(bind.getProcessCode());
            select.setItemName(bind.getProcessName());
            selectList.add(select);
        }
        return selectList;
    }

    /**
     * 判断该产品是否是标准工艺  草稿工艺 还是无工艺
     *
     */
    private MicroProcessMixedResultEntity judgeProductStandardOrDraft(String productSeq) {
        MicroProcessMixedResultEntity resultEntity = new MicroProcessMixedResultEntity();
        Boolean standard = false;
        Boolean draft = false;
        Boolean serial = false;
        List<MicroTechnology> judgeList = this.judgeStandardOrProduct(null, productSeq, null);
        //是否存在标准工艺/草稿工艺
        if (!CollectionUtils.isEmpty(judgeList)) {
            for (MicroTechnology technology : judgeList) {
                // 工艺链可能存在标准和草稿两种状态,优先级:标准>草稿
                if (BomAndTechTypeEnum.STANDARD.getCode().equals(technology.getTechType())) {
                    standard = true;
                }
                if (BomAndTechTypeEnum.DRAFT.getCode().equals(technology.getTechType())) {
                    draft = true;
                }
                if (TechPatternEnum.SERIAL.getCode().equals(technology.getTechPattern())) {
                    serial = true;
                }
            }
        }
        resultEntity.setStandard(standard);
        resultEntity.setDraft(draft);
        resultEntity.setSerial(serial);
        return resultEntity;
    }

    /**
     * 判断传入的现工序是否为空
     *
     */
    private boolean operateProcessEmpty(String operateProcessSeq) {
        return StringUtils.isEmpty(operateProcessSeq);
    }


    /**
     * 判断产品是否符合标准工艺
     *
     */
    public boolean judgeSubmitRecordBelongToStandardTech(List<MicroProcessChainBindEntity> microProcessChainBindEntityList,
                                                         String processSeq, String preProcessSeq) {
        // 编辑记工时更换产品可能报工记录中的当前工序是否可能为null
        if (processSeq == null) {
            return false;
        }
        // 获取标准工艺的类型
        Integer pattern = microProcessChainBindEntityList.get(0).getTechPattern();
        // 判断当前工序是否在工艺链中
        List<MicroProcessChainBindEntity> processChainList = microProcessChainBindEntityList.stream().filter(obj -> obj.getProcessSeq().equals(processSeq))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(processChainList)) {
            return false;
        }
        // 区分ku易记和工易派两个小应用，工易派的报工没有前工序
        String applicationSign = SecurityUtils.getApplicationSign();
        if (applicationSign.equals(ApplicationTypeEnum.GONG_YI_PAI.getCode())) {
            return true;
        } else if (applicationSign.equals(ApplicationTypeEnum.KU_YI_JI.getCode())) {
            // 顺序标准工艺
            if (pattern.equals(TechPatternEnum.SERIAL.getCode())) {
                // 获取前工序
                List<String> preProcessSeqList = processChainList.stream().map(MicroProcessChainBindEntity::getParentProcessSeq).distinct().collect(Collectors.toList());
                // 判定前工序是否符合工艺路线
                // preProcessSeq可能是null或者空字符串
                boolean conditionFlag = (StringUtils.isEmpty(preProcessSeq) && preProcessSeqList.contains("0"))
                        || (preProcessSeq != null && Arrays.stream(preProcessSeq.split(",")).allMatch(processSeqKey -> preProcessSeqList.contains(processSeqKey)));
                if (conditionFlag) {
                    return true;
                } else {
                    return false;
                }
            } else {
                // 乱序标准工艺
                Integer sortIndex = processChainList.get(0).getSort();
                // 排除尾序的工艺链
                List<String> chainListExcludeLastProcess = microProcessChainBindEntityList.subList(0, microProcessChainBindEntityList.size() - 1)
                        .stream().map(MicroProcessChainBindEntity::getProcessSeq).collect(Collectors.toList());
                // 首序，但是前序不为null
                if (sortIndex == 0) {
                    if (StringUtils.isEmpty(preProcessSeq)) {
                        return true;
                    }
                    return false;
                } else {
                    boolean flag = (preProcessSeq != null && chainListExcludeLastProcess.contains(preProcessSeq));
                    if (flag) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        // 默认如果有其他应用的话就返回true
        return true;
    }
}
