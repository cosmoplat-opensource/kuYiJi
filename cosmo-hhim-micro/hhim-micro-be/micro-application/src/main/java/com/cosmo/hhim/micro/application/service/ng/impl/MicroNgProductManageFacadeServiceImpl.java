/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ng.impl;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.IdUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.complete.MicroSettlementAssembler;
import com.cosmo.hhim.micro.application.assembler.ng.MicroQualityControlRecordAssembler;
import com.cosmo.hhim.micro.application.assembler.ng.MicroRepairRecordAssembler;
import com.cosmo.hhim.micro.application.dto.ng.*;
import com.cosmo.hhim.micro.application.service.ng.IMicroNgProductManageFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.entity.ng.*;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistory;
import com.cosmo.hhim.micro.base.domain.entity.submit.SubmitRecordQueryParam;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroEmailService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitService;
import com.cosmo.hhim.micro.complete.domain.service.IMicroSettlementReportService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.constant.MailConstants;
import com.cosmo.hhim.micro.infrastructure.enums.IsCompleteEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.UndoCheckRecordTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.ng.CheckStatusEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import com.cosmo.hhim.micro.ng.domain.entity.*;
import com.cosmo.hhim.micro.ng.domain.mapper.MicroQualityControlRecordMapper;
import com.cosmo.hhim.micro.ng.domain.mapper.MicroRepairRecordMapper;
import com.cosmo.hhim.micro.ng.domain.service.IMicroQualityControlRecordService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.aop.framework.AopContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroNgProductManageFacadeServiceImpl implements IMicroNgProductManageFacadeService {

    @Autowired
    private IMicroWorkSubmitService microWorkSubmitService;
    @Autowired
    private IMicroQualityControlRecordService microQualityControlRecordService;
    @Autowired
    private MicroSupportUtil supportUtil;
    @Autowired
    private IMicroProcessStorageService microProcessStorageService;
    @Autowired
    private IMicroSettlementReportService microSettlementReportService;
    @Autowired
    private IMicroEmailService microEmailService;
    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;
    @Autowired
    private MicroWorkSubmitHistoryMapper microWorkSubmitHistoryMapper;
    @Autowired
    private MicroQualityControlRecordMapper microQualityControlRecordMapper;
    @Autowired
    private MicroRepairRecordMapper microRepairRecordMapper;

    /**
     * 查询报工记录送检之后的列表
     *
     */
    @Override
    public List<QualityControlOfSubmitRecordInfo> selectQualityControlOfSubmitRecordList(SubmitRecordQueryParam submitRecordQueryParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(submitRecordQueryParam));
        submitRecordQueryParam.setSubmitType(supportUtil.getSubmitType());
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectSubmitRecordByUser(submitRecordQueryParam);
        // 如果查询结果为空则直接返回
        if (CollectionUtils.isEmpty(microWorkSubmitList)) {
            return Collections.emptyList();
        }

        // 根据报工记录信息查询质检相关信息
        Map<Long, List<MicroQualityControlRecord>> map = new HashMap<>(16);
        // 如果要查询已质检列表，则表明报工记录均已经质检过
        if (submitRecordQueryParam.getCheckStatus().equals(CheckStatusEnum.FINISHED_INSPECTION.getCode())) {
            List<Long> submitIdList = microWorkSubmitList.stream().map(MicroWorkSubmit::getId).collect(Collectors.toList());
            List<MicroQualityControlRecord> microQualityControlRecordList = microQualityControlRecordMapper.selectMicroQualityControlRecordBySubmitIdList(submitIdList);
            if (!CollectionUtils.isEmpty(microQualityControlRecordList)) {
                // 找到最新的报工记录
                map = microQualityControlRecordList.stream().collect(Collectors.groupingBy(MicroQualityControlRecord::getSubmitId));
            }
        }
        // 返回组合实体对象
        return MicroQualityControlRecordAssembler.covertDomain2QualityControlOfSubmitRecordInfo(microWorkSubmitList, map);
    }

    /**
     * 质检操作
     *
     * @param qualityControlOfSubmitRecordDto 质检操作前端入参
     * @return sql更新行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int qualityControlOfRecord(QualityControlOfSubmitRecordDto qualityControlOfSubmitRecordDto) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(qualityControlOfSubmitRecordDto));
        this.validQualityControlOfSubmitRecordDtoParam(qualityControlOfSubmitRecordDto);
        // 查询报工记录信息
        MicroWorkSubmit microWorkSubmit = microWorkSubmitService.selectMicroWorkSubmitExById(qualityControlOfSubmitRecordDto.getSubmitId());
        if (microWorkSubmit == null) {
            throw new CustomException("id为" + qualityControlOfSubmitRecordDto.getSubmitId().toString() + "的报工记录不存在");
        }
        if (microWorkSubmit.getIsComplete().equals(IsCompleteEnum.YES.getCode())) {
            throw new CustomException("id为:" + microWorkSubmit.getId().toString() + "记工已完工出库");
        }
        if (microWorkSubmit.getCheckStatus().equals(CheckStatusEnum.FINISHED_INSPECTION.getCode())) {
            throw new CustomException("id为:" + microWorkSubmit.getId().toString() + "记录已质检完成");
        }

        // 如果该报工记录已经审核了且审核数量与质检数量不一致，需要撤销原报工记录, 质检判异
        boolean isApproved = microWorkSubmit.getSubmitStatus().equals(SubmitStatusEnum.APPROVED.getCode());
        boolean passNumChanged = microWorkSubmit.getCheckPassNum().compareTo(qualityControlOfSubmitRecordDto.getCheckPassNum()) != 0;
        boolean ngNumChanged = microWorkSubmit.getCheckNgNum().compareTo(qualityControlOfSubmitRecordDto.getCheckNgNum()) != 0;
        if (isApproved && (passNumChanged || ngNumChanged)) { 
            // 撤销操作，更新报工表
            MicroWorkSubmit submit = microWorkSubmitService.undoCheckedRecord(microWorkSubmit.getId(), UndoCheckRecordTypeEnum.QC_UNDO.getCode());
            // 更新库存信息, 生成库存记录
            microProcessStorageService.changeStorage(submit, CommonConstants.STORAGE_CHANGE_TYPE_QC_ELIMINATION);
            // 撤销计件结算相关逻辑
            microSettlementReportService.undoSettlementReport(Collections.singletonList(submit.getId()));
            microWorkSubmit.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());
        }

        // 生成质检记录
        Date currentDate = DateUtils.getNowDate();
        String qualityControlRecordNo = IdUtils.fastSimpleUUID();
        List<MicroQualityControlRecord> microQualityControlRecordList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(qualityControlOfSubmitRecordDto.getNgProductDetailInfos())) {
            for (int i = 0; i < qualityControlOfSubmitRecordDto.getNgProductDetailInfos().size(); i++) {
                NgProductDetailInfo ngProductDetailInfo = qualityControlOfSubmitRecordDto.getNgProductDetailInfos().get(i);
                MicroQualityControlRecord temp = MicroQualityControlRecordAssembler.covertMicroWorkSubmitDomain2MicroQualityControlRecordDomain(microWorkSubmit, currentDate, qualityControlRecordNo, ngProductDetailInfo);
                if (i == 0) {
                    temp.setPassNum(qualityControlOfSubmitRecordDto.getCheckPassNum());
                } else {
                    temp.setPassNum(BigDecimal.ZERO);
                }
                temp.setRemark(qualityControlOfSubmitRecordDto.getRemark());
                microQualityControlRecordList.add(temp);
            }
            // 没有设置不良品类型
        } else {
            MicroQualityControlRecord temp = MicroQualityControlRecordAssembler.covertMicroWorkSubmitDomain2MicroQualityControlRecordDomain(microWorkSubmit, currentDate, qualityControlRecordNo, null);
            temp.setPassNum(qualityControlOfSubmitRecordDto.getCheckPassNum());
            temp.setNgNum(qualityControlOfSubmitRecordDto.getCheckNgNum());
            temp.setRemark(qualityControlOfSubmitRecordDto.getRemark());
            microQualityControlRecordList.add(temp);
        }

        // 批量插入质检记录信息
        if (!CollectionUtils.isEmpty(microQualityControlRecordList)) {
            microQualityControlRecordService.batchInsertMicroQualityControlRecord(microQualityControlRecordList);
        }

        // 新增报工记录历史
        MicroWorkSubmitHistory microWorkSubmitHistory = new MicroWorkSubmitHistory();
        BeanUtils.copyProperties(microWorkSubmit, microWorkSubmitHistory);
        microWorkSubmitHistory.setPassNum(qualityControlOfSubmitRecordDto.getCheckPassNum());
        microWorkSubmitHistory.setNgNum(qualityControlOfSubmitRecordDto.getCheckNgNum());
        microWorkSubmitHistory.setOperateNode(CommonConstants.SUBMIT_HISTORY_QUALITY_CONTROL);
        microWorkSubmitHistory.setCreatedDate(currentDate);
        microWorkSubmitHistory.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
        microWorkSubmitHistoryMapper.insertMicroWorkSubmitHistory(microWorkSubmitHistory);

        // 更新报工记录信息
        MicroWorkSubmit updateParam = new MicroWorkSubmit();
        updateParam.setId(microWorkSubmit.getId());
        updateParam.setCheckStatus(CheckStatusEnum.FINISHED_INSPECTION.getCode());
        updateParam.setCheckPassNum(qualityControlOfSubmitRecordDto.getCheckPassNum());
        updateParam.setCheckNgNum(qualityControlOfSubmitRecordDto.getCheckNgNum());
        updateParam.setLastUpdBy(SecurityUtils.getUserId().toString());
        updateParam.setLastUpdDate(currentDate);
        updateParam.setQcDate(currentDate);
        updateParam.setQcUser(SecurityUtils.getUserId().toString());

        return microWorkSubmitMapper.updateSubmitRecordCheckStatus(updateParam);
    }

    /**
     * 批量质检
     *
     * @param  ids  报工记录id
     */
    @Override
    public Boolean batchQualityControlOfRecord(Long[] ids) {
        log.info("请求的参数为:{}", Arrays.toString(ids));
        if (ArrayUtil.isEmpty(ids)) {
            throw new CustomException("未选择质检的记录");
        }
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectMicroWorkSubmitByIds(ids);
        // 构造质检操作的参数
        List<QualityControlOfSubmitRecordDto> qualityControlOfSubmitRecordDtoList = new ArrayList<>();
        for (MicroWorkSubmit microWorkSubmit : microWorkSubmitList) {
            QualityControlOfSubmitRecordDto temp = new QualityControlOfSubmitRecordDto();
            temp.setSubmitId(microWorkSubmit.getId());
            // 批量质检的数量就是报工的数量
            temp.setCheckPassNum(microWorkSubmit.getPassNum());
            temp.setCheckNgNum(microWorkSubmit.getNgNum());
            qualityControlOfSubmitRecordDtoList.add(temp);
        }
        // 循环调用
        for (QualityControlOfSubmitRecordDto qualityControlOfSubmitRecordDto : qualityControlOfSubmitRecordDtoList) {
            ((MicroNgProductManageFacadeServiceImpl) AopContext.currentProxy()).qualityControlOfRecord(qualityControlOfSubmitRecordDto);
        }
        return true;
    }

    /**
     * 根据报工记录id获取详细的质检信息
     *
     * @param id 报工记录id
     * @return 报工记录的质检明细信息
     */
    @Override
    public DetailQualityControlInfo getDetailQcInfo(Long id) {
        MicroWorkSubmit microWorkSubmit = microWorkSubmitService.selectMicroWorkSubmitExById(id);

        // 存储不良品类型信息
        List<NgProductDetailInfo> ngProductDetailInfoList = null;
        String qcNickName = null;
        Date qcDate = null;
        String remark = null;
        // 质检记录中针对某个报工记录可能生成多条，其中第一条会存在实际的质检数量信息
        BigDecimal checkPassNum = null;

        if (microWorkSubmit.getCheckStatus().equals(CheckStatusEnum.FINISHED_INSPECTION.getCode())) {
            MicroQualityControlRecord queryParam = new MicroQualityControlRecord();
            queryParam.setSubmitId(id);
            queryParam.setActiveFlag("1");
            List<MicroQualityControlRecord> microQualityControlRecordList = microQualityControlRecordService.selectMicroQualityControlRecordList(queryParam);
            // 选取最新的质检记录信息
            Map<String, List<MicroQualityControlRecord>> map = microQualityControlRecordList.stream().collect(Collectors.groupingBy(MicroQualityControlRecord::getQualityControlRecordNo));
            if (map.keySet().size() > 1) {
                String qualityControlRecordNoTemp = microQualityControlRecordList.stream().sorted(Comparator.comparing(MicroQualityControlRecord::getCreatedDate).reversed())
                        .collect(Collectors.toList()).get(0).getQualityControlRecordNo();
                microQualityControlRecordList = map.get(qualityControlRecordNoTemp);
            }
            checkPassNum = microQualityControlRecordList.stream().map(MicroQualityControlRecord::getPassNum).reduce(BigDecimal.ZERO, BigDecimal::add);
            ngProductDetailInfoList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(microQualityControlRecordList)) {
                for (MicroQualityControlRecord microQualityControlRecord : microQualityControlRecordList) {
                    NgProductDetailInfo temp = new NgProductDetailInfo();
                    temp.setNgType(microQualityControlRecord.getNgType());
                    temp.setNgNum(microQualityControlRecord.getNgNum());
                    ngProductDetailInfoList.add(temp);
                }
                MicroQualityControlRecord microQualityControlRecord = microQualityControlRecordList.get(0);
                qcNickName = getNickName(microQualityControlRecord.getCreatedBy().toString());
                remark = microQualityControlRecord.getRemark();
                qcDate = microQualityControlRecord.getCreatedDate();
            }
        }

        // 组装返回对象
        return MicroQualityControlRecordAssembler.covertDomain2DetailQualityControlInfo(microWorkSubmit, qcNickName, remark, qcDate, checkPassNum, ngProductDetailInfoList);
    }

    private String getNickName(String userId) {
        Object userCache = supportUtil.getUserCache(userId);
        String nickName = "";
        if (userCache != null) {
            nickName = ((MicroUser) userCache).getNickName();
        }
        return nickName;
    }

    /**
     * 获取不良品类型 (根据已有质检记录)
     *
     */
    @Override
    public List<String> selectNgTypeList(NgTypeQueryParam ngTypeQueryParam) {
        return microQualityControlRecordService.selectNgTypeList(ngTypeQueryParam);
    }

    /**
     * 获取不良品清单 - 产品（已审核）
     *
     */
    @Override
    public List<NgProductByProductFromAlreadyCheck> selectNgProductListFromAlreadyCheck(String productNameOrCode) {
        return microWorkSubmitService.selectNgProductListFromAlreadyCheck(productNameOrCode);
    }

    /**
     * 获取不良品清单到 产品 + 工序 + 人 维度
     *
     */
    @Override
    public List<NgProductAndProcessAndUserByProduct> selectNgProductAndProcessByUserFromAlreadyCheck(String productSeq, String processSeq) {
        return microWorkSubmitMapper.selectNgProductAndProcessAndUserByProductFromAlreadyCheck(productSeq, processSeq);
    }

    /**
     * 获取不良品清单 - 员工（已审核）
     *
     */
    @Override
    public List<NgProductByUserFromAlreadyCheck> selectNgProductListByUserFromAlreadyCheck(String submitNickName) {
        return microWorkSubmitService.selectNgProductListByUserFromAlreadyCheck(submitNickName);
    }

    /**
     * 返修复核时获取的质检记录信息
     *
     */
    @Override
    public DetailQualityControlInfoWhenRepair selectQualityControlInfoWhenRepair(NgQueryParam ngQueryParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(ngQueryParam));
        this.validNgQueryParam(ngQueryParam);

        NgQueryParam queryParam = new NgQueryParam();
        queryParam.setProductSeq(ngQueryParam.getProductSeq());
        queryParam.setOperateProcessSeq(ngQueryParam.getOperateProcessSeq());
        queryParam.setSubmitUser(ngQueryParam.getSubmitUser());
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectMicroWorkSubmitListByNgManage(queryParam);
        if (CollectionUtils.isEmpty(microWorkSubmitList)) {
            throw new CustomException("报工记录信息为空");
        }
        // 查询质检记录信息
        List<Long> submitIdList = microWorkSubmitList.stream().map(MicroWorkSubmit::getId).collect(Collectors.toList());
        List<QualityControlInfoWhenRepair> qualityControlInfoWhenRepairList = microQualityControlRecordService.selectDetailQualityControlInfo(submitIdList);

        // 返回结果
        DetailQualityControlInfoWhenRepair result = new DetailQualityControlInfoWhenRepair();
        MicroWorkSubmit microWorkSubmitTemp = microWorkSubmitList.get(0);
        result.setSubmitNickName(microWorkSubmitTemp.getSubmitNickName());
        result.setProductName(microWorkSubmitTemp.getProductName());
        result.setProductCode(microWorkSubmitTemp.getProductCode());
        result.setProductUnit(microWorkSubmitTemp.getUnit());
        result.setProcessName(microWorkSubmitTemp.getOperateProcessName());
        // 不良品清单中的不良数量
        BigDecimal repairNumTemp = microWorkSubmitList.stream().map(MicroWorkSubmit::getRepairNum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal checkNgNumTemp = microWorkSubmitList.stream().map(MicroWorkSubmit::getCheckNgNum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal abandonedNumTemp = microWorkSubmitList.stream().map(MicroWorkSubmit::getAbandonedNum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setExceptionNum(checkNgNumTemp.subtract(repairNumTemp).subtract(abandonedNumTemp));
        result.setQualityControlInfoWhenRepairList(qualityControlInfoWhenRepairList);
        result.setIds(StringUtils.join(submitIdList, ","));

        return result;
    }

    /**
     * 返修复核接口
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int repairAndThenQualityControl(RepairAndThenQualityControlParam repairAndThenQualityControlParam) { 
        log.info("请求的参数为:{}", JSONObject.toJSONString(repairAndThenQualityControlParam));
        this.validRepairAndThenQualityControlParam(repairAndThenQualityControlParam);

        // 1. 查询报工记录信息
        NgQueryParam queryParam = new NgQueryParam();
        queryParam.setIds(repairAndThenQualityControlParam.getIds());
        // 已经按照报工时间升序排列
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectMicroWorkSubmitListByNgManage(queryParam);
        if (CollectionUtils.isEmpty(microWorkSubmitList)) {
            throw new CustomException("不存在id为" + queryParam.getIds() + "的报工记录信息");
        }

        // 存储要插入的返修记录
        List<MicroRepairRecord> microRepairRecordList = new ArrayList<>();
        // 返修单号
        String repairNo = IdUtils.fastSimpleUUID();
        String remark = repairAndThenQualityControlParam.getRemark();
        Long repairUser = repairAndThenQualityControlParam.getRepairUser();

        // 2. 分配数量(按照报工时间升序分配)
        // 分配返修完成数量
        BigDecimal repairNumTemp = repairAndThenQualityControlParam.getRepairNum();
        if (repairNumTemp.signum() > 0) {
            for (MicroWorkSubmit obj : microWorkSubmitList) {
                // 剩余的不良品数
                BigDecimal otherNum = obj.getCheckNgNum().subtract(obj.getAbandonedNum().add(obj.getRepairNum()));
                if (otherNum.signum() > 0) {
                    // 质检记录新增判断
                    MicroRepairRecord microRepairRecordTemp;
                    List<MicroRepairRecord> repairRecords = microRepairRecordList.stream().filter(repair -> repair.getSubmitId().equals(obj.getId())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(repairRecords)) {
                        microRepairRecordTemp = MicroRepairRecordAssembler.microWorkSubmitDomain2MicroRepairRecordDomain(obj, repairNo, repairUser, remark);
                        microRepairRecordList.add(microRepairRecordTemp);
                    } else {
                        microRepairRecordTemp = repairRecords.get(0);
                    }

                    // 实际扣减操作
                    if (repairNumTemp.subtract(otherNum).signum() > 0) {
                        obj.setRepairNum(obj.getRepairNum().add(otherNum));
                        repairNumTemp = repairNumTemp.subtract(otherNum);
                        microRepairRecordTemp.setRepairNum(otherNum);
                    } else {
                        obj.setRepairNum(obj.getRepairNum().add(repairNumTemp));
                        microRepairRecordTemp.setRepairNum(repairNumTemp);
                        break;
                    }
                }
            }
        }

        // 分配让步接收数量
        BigDecimal concessionNumTemp = repairAndThenQualityControlParam.getConcessionNum();
        if (concessionNumTemp.signum() > 0) {
            for (MicroWorkSubmit obj : microWorkSubmitList) {
                // 剩余的不良品数
                BigDecimal otherNum = obj.getCheckNgNum().subtract(obj.getAbandonedNum().add(obj.getRepairNum()));
                if (otherNum.signum() > 0) {
                    // 质检记录新增判断
                    MicroRepairRecord microRepairRecordTemp;
                    List<MicroRepairRecord> repairRecords = microRepairRecordList.stream().filter(repair -> repair.getSubmitId().equals(obj.getId())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(repairRecords)) {
                        microRepairRecordTemp = MicroRepairRecordAssembler.microWorkSubmitDomain2MicroRepairRecordDomain(obj, repairNo, repairUser, remark);
                        microRepairRecordList.add(microRepairRecordTemp);
                    } else {
                        microRepairRecordTemp = repairRecords.get(0);
                    }

                    // 实际扣减操作
                    if (concessionNumTemp.subtract(otherNum).signum() > 0) {
                        obj.setRepairNum(obj.getRepairNum().add(otherNum));
                        concessionNumTemp = concessionNumTemp.subtract(otherNum);
                        microRepairRecordTemp.setConcessionNum(otherNum);
                    } else {
                        obj.setRepairNum(obj.getRepairNum().add(concessionNumTemp));
                        microRepairRecordTemp.setConcessionNum(concessionNumTemp);
                        break;
                    }
                }
            }
        }

        // 分配报废数量
        BigDecimal abandonedNumTemp = repairAndThenQualityControlParam.getAbandonedNum();
        if (abandonedNumTemp.signum() > 0) {
            for (MicroWorkSubmit obj : microWorkSubmitList) {
                // 剩余的不良品数
                BigDecimal otherNum = obj.getCheckNgNum().subtract(obj.getAbandonedNum().add(obj.getRepairNum()));
                if (otherNum.signum() > 0) {
                    // 质检记录新增判断
                    MicroRepairRecord microRepairRecordTemp;
                    List<MicroRepairRecord> repairRecords = microRepairRecordList.stream().filter(repair -> repair.getSubmitId().equals(obj.getId())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(repairRecords)) {
                        microRepairRecordTemp = MicroRepairRecordAssembler.microWorkSubmitDomain2MicroRepairRecordDomain(obj, repairNo, repairUser, remark);
                        microRepairRecordList.add(microRepairRecordTemp);
                    } else {
                        microRepairRecordTemp = repairRecords.get(0);
                    }

                    if (abandonedNumTemp.subtract(otherNum).signum() > 0) {
                        obj.setAbandonedNum(obj.getAbandonedNum().add(otherNum));
                        abandonedNumTemp = abandonedNumTemp.subtract(otherNum);
                        microRepairRecordTemp.setAbandonedNum(otherNum);
                        microRepairRecordTemp.setAbandonedType(repairAndThenQualityControlParam.getAbandonedType());
                    } else {
                        obj.setAbandonedNum(obj.getAbandonedNum().add(abandonedNumTemp));
                        microRepairRecordTemp.setAbandonedNum(abandonedNumTemp);
                        microRepairRecordTemp.setAbandonedType(repairAndThenQualityControlParam.getAbandonedType());
                        break;
                    }
                }
            }
        }

        // 构建各报工记录新增的返修数量
        List<MicroWorkSubmit> submitRecordListOfChangeStorage = MicroRepairRecordAssembler.
                microWorkSubmitAndMicroRepairRecordDomain2MicroWorkSubmitDomainForChangeStorage(microWorkSubmitList, microRepairRecordList);
        for (MicroWorkSubmit microWorkSubmit : submitRecordListOfChangeStorage) {
            // 有了返修数量才会去更新库存，报废的数量不会影响库存
            if (microWorkSubmit.getCheckPassNum().signum() > 0) {
                // 更新库存信息
                microProcessStorageService.changeStorage(microWorkSubmit, CommonConstants.STORAGE_CHANGE_TYPE_REPAIR);
            }
        }

        // 更新报工记录表
        List<MicroWorkSubmit> microWorkSubmitsForUpdate = MicroRepairRecordAssembler.
                microWorkSubmitAndMicroRepairRecordDomain2MicroWorkSubmitDomainForUpdate(microWorkSubmitList, microRepairRecordList);
        microWorkSubmitMapper.updateSubmitRecordRepairNumBatch(microWorkSubmitsForUpdate);

        // 生成报工记录变动记录表
        List<MicroWorkSubmitHistory> workSubmitHistoryList = MicroRepairRecordAssembler
                .repairRecordAndMicroWorkSubmitConvert2MicroWorkSubmitHistoryForRepair(microWorkSubmitList, microRepairRecordList);
        microWorkSubmitHistoryMapper.insertMicroWorkSubmitHistoryBatch(workSubmitHistoryList);

        // 生成计件结算
        List<MicroWorkSubmit> microWorkSubmitsForUpdateByFilter = microWorkSubmitsForUpdate.stream().filter(obj -> obj.getCheckPassNum().signum() > 0).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(microWorkSubmitsForUpdateByFilter)) {
            microSettlementReportService.generateReportAfterCheck(MicroSettlementAssembler.submitToSettlement(microWorkSubmitsForUpdateByFilter));
        }

        // 插入返修记录
        return microRepairRecordMapper.insertMicroRepairRecordBatch(microRepairRecordList);
    }

    /**
     * 返修复核列表
     *
     * @param queryKey (产品编码、名称、工序)
     */
    @Override
    public List<RepairRecordInfo> selectMicroRepairRecordListByQueryKey(String queryKey) {
        return microRepairRecordMapper.selectMicroRepairRecordListByQueryKey(queryKey);
    }

    /**
     * 根据返修单号获取返修详情
     *
     */
    @Override
    public DetailRepairAndThenQualityControlInfo getDetailRepairAndThenQualityControlInfo(String repairNo) {

        // 根据返修单号汇总返修记录信息
        MicroRepairRecord queryParam = new MicroRepairRecord();
        queryParam.setRepairNo(repairNo);
        List<MicroRepairRecord> microRepairRecordList = microRepairRecordMapper.selectMicroRepairRecordList(queryParam);
        if (CollectionUtils.isEmpty(microRepairRecordList)) {
            throw new CustomException("未查询到返修记录信息");
        }
        BigDecimal concessionNum = microRepairRecordList.stream().map(MicroRepairRecord::getConcessionNum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal repairNum = microRepairRecordList.stream().map(MicroRepairRecord::getRepairNum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal abandonedNum = microRepairRecordList.stream().map(MicroRepairRecord::getAbandonedNum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<MicroRepairRecord> recordList = microRepairRecordList.stream().filter(obj -> obj.getAbandonedType() != null).collect(Collectors.toList());
        // 主要是获取产品、工序、报工人姓名
        MicroWorkSubmit microWorkSubmit = microWorkSubmitMapper.selectMicroWorkSubmitExById(microRepairRecordList.get(0).getSubmitId());

        // 质检记录信息
        List<Long> submitIdList = microRepairRecordList.stream().map(MicroRepairRecord::getSubmitId).distinct().collect(Collectors.toList());
        List<QualityControlInfoWhenRepair> qualityControlInfoWhenRepairList = microQualityControlRecordService.selectDetailQualityControlInfo(submitIdList);

        // 组装信息
        DetailRepairAndThenQualityControlInfo result = new DetailRepairAndThenQualityControlInfo();
        result.setSubmitNickName(getNickName(microWorkSubmit.getSubmitUser()));
        result.setProductName(microWorkSubmit.getProductName());
        result.setProductCode(microWorkSubmit.getProductCode());
        result.setProcessName(microWorkSubmit.getOperateProcessName());
        result.setRepairNum(repairNum);
        result.setConcessionNum(concessionNum);
        result.setAbandonedNum(abandonedNum);
        result.setAbandonedType(recordList.isEmpty() ? null : recordList.get(0).getAbandonedType());
        result.setRepairNickName(getNickName(microRepairRecordList.get(0).getRepairUser().toString()));
        result.setRemark(microRepairRecordList.get(0).getRemark());
        result.setQualityControlInfoWhenRepairList(qualityControlInfoWhenRepairList);

        return result;
    }

    /**
     * 质检记录历史
     *
     */
    @Override
    public List<QualityControlHistoryInfo> selectQualityControlHistoryInfoList(QualityControlHistoryQueryParam qualityControlHistoryQueryParam) {
        return microQualityControlRecordService.selectQualityControlHistoryInfoList(qualityControlHistoryQueryParam);
    }

    /**
     *  不良品类型统计分析
     *
     */
    @Override
    public List<NgTypeStatisticInfo> obtainedNgTypeStatisticAnalysis(NgTypeStatisticQueryParam ngTypeStatisticQueryParam) {
        return microQualityControlRecordMapper.selectDifferentNgType(ngTypeStatisticQueryParam);
    }

    /**
     * 工作台不良品模块外部展示数量
     *
     */
    @Override
    public NgNumFromProductAndUser obtainedNgProductTotalNumAndUser() {
        Long submitType = supportUtil.getSubmitType();
        return microWorkSubmitMapper.obtainedNgProductTotalNumAndUser(submitType);
    }

    /**
     * 导出不良品清单
     *
     */
    @Override
    public String exportNgProductStatisticsReport(String receivedBy) {
        log.info("邮箱地址为:{}", receivedBy);
        // 存储结果
        List<NgProductMailExportResult> results = new ArrayList<>();
        List<NgInfoByProductAndProcessAndUser> ngInfoByProductAndProcessAndUserList = microWorkSubmitMapper.selectNgProductNumByProductAndProcessAndUser();
        if (!ngInfoByProductAndProcessAndUserList.isEmpty()) {
            // 查询相应的返修记录信息
            List<RepairNumInfoByProductAndProcessAndUser> repairNumInfoByProductAndProcessAndUsers = microRepairRecordMapper.selectNumInfoByProductAndProcessAndUser();
            if (!CollectionUtils.isEmpty(repairNumInfoByProductAndProcessAndUsers)) {
                Map<String, List<RepairNumInfoByProductAndProcessAndUser>> map = repairNumInfoByProductAndProcessAndUsers
                        .stream()
                        .collect(Collectors.groupingBy(obj -> obj.getProductSeq() + "-" + obj.getProcessSeq() + "-" + obj.getSubmitUser()));
                // 组装信息
                for (NgInfoByProductAndProcessAndUser ngInfoByProductAndProcessAndUser : ngInfoByProductAndProcessAndUserList) {
                    List<RepairNumInfoByProductAndProcessAndUser> repairNumInfoList = map.get(ngInfoByProductAndProcessAndUser.getProductSeq() + "-"
                            + ngInfoByProductAndProcessAndUser.getProcessSeq() + "-"
                            + ngInfoByProductAndProcessAndUser.getSubmitUser());
                    NgProductMailExportResult temp  = MicroRepairRecordAssembler.covertNgProductMailExportResult(ngInfoByProductAndProcessAndUser, repairNumInfoList);
                    results.add(temp);
                }
            }
        }

        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.NG_PRODUCT_MANAGE_EXCEL_NAME);

        return microEmailService.exportAndSendEmail(MailConstants.NG_PRODUCT_MANAGE_MAIL_SUBJECT,
                MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID,
                receivedBy,
                results,
                NgProductMailExportResult.class,
                placeholderMap);
    }

    /**
     * 校验质检操作的前端入参
     * @param qualityControlOfSubmitRecordDto 质检操作的前端入参
     */
    public void validQualityControlOfSubmitRecordDtoParam(QualityControlOfSubmitRecordDto qualityControlOfSubmitRecordDto) {
        if (CheckObjectUtils.isEmpty(qualityControlOfSubmitRecordDto)) {
            throw new CustomException("请求的参数不能为空");
        }
        if (CheckObjectUtils.isEmpty(qualityControlOfSubmitRecordDto.getSubmitId())) {
            throw new CustomException("报工记录的id不能为空");
        }
    }

    /**
     * 校验返修复核页面返回信息接口的前端入参
     *
     */
    public void validNgQueryParam(NgQueryParam ngQueryParam) {
        if (CheckObjectUtils.isEmpty(ngQueryParam)) {
            throw new CustomException("请求的参数不能为空");
        }
        if (CheckObjectUtils.isAnyEmpty(ngQueryParam.getProductSeq(),
                ngQueryParam.getOperateProcessSeq(),
                ngQueryParam.getSubmitUser())) {
            throw new CustomException("产品序列码、工序序列码以及报工人参数不能为空");
        }
    }

    /**
     * 校验返修复核前端入参
     *
     */
    public void validRepairAndThenQualityControlParam(RepairAndThenQualityControlParam repairAndThenQualityControlParam) {
        if (CheckObjectUtils.isEmpty(repairAndThenQualityControlParam)) {
            throw new CustomException("请求的参数不能为空");
        }
        // 返修复核的数量不能为0
        if (repairAndThenQualityControlParam.getRepairNum().add(repairAndThenQualityControlParam.getConcessionNum())
                .add(repairAndThenQualityControlParam.getAbandonedNum()).signum() == 0) {
            throw new CustomException("质检复核的数量不能为0");
        }
        if (StringUtils.isEmpty(repairAndThenQualityControlParam.getIds())) {
            throw new CustomException("报工记录ids不能为空");
        }
    }
}
