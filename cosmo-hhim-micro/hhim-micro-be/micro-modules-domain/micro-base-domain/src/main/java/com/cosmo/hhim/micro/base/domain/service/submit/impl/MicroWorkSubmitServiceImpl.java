/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.submit.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.IdUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.analysis.*;
import com.cosmo.hhim.micro.base.domain.entity.check.*;
import com.cosmo.hhim.micro.base.domain.entity.common.FirstSubmitOrCheckInfoResult;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.entity.ng.NgProductAndProcessByProductFromAlreadyCheck;
import com.cosmo.hhim.micro.base.domain.entity.ng.NgProductAndProcessByUserFromAlreadyCheck;
import com.cosmo.hhim.micro.base.domain.entity.ng.NgProductByProductFromAlreadyCheck;
import com.cosmo.hhim.micro.base.domain.entity.ng.NgProductByUserFromAlreadyCheck;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageDto;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageHistory;
import com.cosmo.hhim.micro.base.domain.entity.storage.StorageForProductAndProcess;
import com.cosmo.hhim.micro.base.domain.entity.submit.*;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainBindEntity;
import com.cosmo.hhim.micro.base.domain.entity.warn.SubmitRecordException;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessCommonService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.entity.MicroCapacityUpLimitEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.enums.*;
import com.cosmo.hhim.micro.infrastructure.enums.ng.CheckStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.TechPatternEnum;
import com.cosmo.hhim.micro.infrastructure.util.CompareFieldUtils;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 报工记录Service业务层处理
 */
@Slf4j
@Service
public class MicroWorkSubmitServiceImpl implements IMicroWorkSubmitService {

    private static final long DAYS_LOOKBACK = 7;
    private static final BigDecimal PASS_RATE_THRESHOLD = new BigDecimal("0.95");
    private static final BigDecimal CAPACITY_OVER_THRESHOLD = new BigDecimal("1.1");
    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;
    @Autowired
    private IMicroProductService microProductService;
    @Autowired
    private IMicroProcessCommonService microProcessCommonService;
    @Autowired
    private IMicroProcessStorageService processStorageService;
    @Autowired
    private MicroProcessStorageMapper microProcessStorageMapper;
    @Autowired
    private MicroWorkSubmitHistoryMapper microWorkSubmitHistoryMapper;
    @Autowired
    private MicroSupportUtil supportUtil;
    @Autowired
    private IMicroUserService microUserService;

    private static final String PRE = "0";
    private static final String CURRENT = "1";

    /**
     * 查询报工记录
     *
     * @param id 报工记录ID
     * @return 报工记录
     */
    @Override
    public MicroWorkSubmit selectMicroWorkSubmitById(Long id) {
        return microWorkSubmitMapper.selectMicroWorkSubmitById(id);
    }

    /**
     * 查询报工记录 (带有扩展信息)
     *
     */
    @Override
    public MicroWorkSubmit selectMicroWorkSubmitExById(Long id) {
        MicroWorkSubmit microWorkSubmit = microWorkSubmitMapper.selectMicroWorkSubmitExById(id);
        if (CheckObjectUtils.isEmpty(microWorkSubmit)) {
            return microWorkSubmit;
        }
        // 添加审产人名称
        if (StringUtils.hasText(microWorkSubmit.getCheckUser())) {

            microWorkSubmit.setCheckNickName(getNickName(microWorkSubmit.getCheckUser()));
        }
        // 添加报产人名称
        if (StringUtils.hasText(microWorkSubmit.getSubmitUser())) {
            String submitNickName = getNickName(microWorkSubmit.getSubmitUser());
            microWorkSubmit.setSubmitNickName(submitNickName);
        }
        return microWorkSubmit;
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
     * 查询报工记录列表
     *
     * @param microWorkSubmit 报工记录
     * @return 报工记录
     */
    @Override
    public List<MicroWorkSubmit> selectMicroWorkSubmitList(MicroWorkSubmit microWorkSubmit) {
        return microWorkSubmitMapper.selectMicroWorkSubmitList(microWorkSubmit);
    }

    /**
     * 查询报工记录列表（带有扩展信息）
     *
     */
    @Override
    public List<MicroWorkSubmitDto> selectMicroWorkSubmitExList(MicroWorkSubmit microWorkSubmit) {
        return microWorkSubmitMapper.selectMicroWorkSubmitExList(microWorkSubmit);
    }

    /**
     * 根据ids获取多条带有扩展信息的报工记录
     *
     */
    @Override
    public List<MicroWorkSubmitDto> selectMicroWorkSubmitExListByIds(Long[] ids, String submitNickName, Long submitStatus) {
        log.info("请求的参数为:ids:{}submitNickName{}submitStatus{}", Arrays.toString(ids), submitNickName, submitStatus);
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectMicroWorkSubmitExByIds(ids, submitNickName, submitStatus);
        if (CheckObjectUtils.isEmpty(microWorkSubmitDtoList)) {
            return microWorkSubmitDtoList;
        }
        // 只需要获取审产人的姓名
        microWorkSubmitDtoList.forEach(obj -> {
            if (StringUtils.hasText(obj.getCheckUser())) {
                String checkNickName = getNickName(obj.getCheckUser());
                obj.setCheckNickName(checkNickName);
            }
        });
        // 获取产品 + 工序的良品率和产能
        Map<String, Map<String, Object>> warningMetrics = supportUtil.getWarningMetrics();
        // 产品 + 工序 + 报工人 + 报工日期的报工数量（审核和未审核）
        Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition = microWorkSubmitMapper.obtainedTotalSubmitNumByCondition();

        // 判断异常标签
        microWorkSubmitDtoList = this.judgeSubmitRecordsException(microWorkSubmitDtoList, warningMetrics, allSubmitTotalNumByCondition);
        return microWorkSubmitDtoList;
    }

    /**
     * 新增报工记录
     *
     * @param microWorkSubmit 报工记录
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMicroWorkSubmit(MicroWorkSubmit microWorkSubmit) {
        // 后端也对报工数量进行限制
        this.validSubmitNumber(microWorkSubmit);
        int i;
        try {
            //生成报工号
            String uuid = IdUtils.fastSimpleUUID();
            String tenantCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
            String userId = String.valueOf(SecurityUtils.getUserId());
            String applicationSign = SecurityUtils.getApplicationSign();
            // 报工数据
            microWorkSubmit.setSubmitNo(uuid);
            microWorkSubmit.setTenantCode(tenantCode);
            microWorkSubmit.setCreatedBy(userId);
            microWorkSubmit.setCreatedDate(DateUtils.getNowDate());
            microWorkSubmit.setSubmitUser(userId);
            microWorkSubmit.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());
            microWorkSubmit.setDataStatus(DataStatusEnum.UN_CHANGED.getCode());
            // 判断是工易派还是工易记, 有不同的设置
            if (applicationSign.equals(ApplicationTypeEnum.GONG_YI_PAI.getCode())) {
                // 设置补录标示
                microWorkSubmit.setExpiredRecordFlag(ExpiredRecordFlagEnum.NO.getCode());
                // 报工方式
                microWorkSubmit.setSubmitType(SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
                // 设置一个报工时间,工易派没有报工时间字段
                microWorkSubmit.setSubmitDay(DateUtils.getNowDate());
            } else {
                // 报工方式
                microWorkSubmit.setSubmitType(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode());
                addHotData(microWorkSubmit);
            }
            i = microWorkSubmitMapper.insertMicroWorkSubmit(microWorkSubmit);

            // 报工历史记录: 工易派需要重新设置产品名称、编码，当前工序名称和编码
            MicroWorkSubmitHistory microWorkSubmitHistory = new MicroWorkSubmitHistory();
            BeanUtils.copyProperties(microWorkSubmit, microWorkSubmitHistory);
            // 历史变动操作节点
            microWorkSubmitHistory.setOperateNode(CommonConstants.SUBMIT_HISTORY_RECORDS_ADD);
            microWorkSubmitHistoryMapper.insertMicroWorkSubmitHistory(microWorkSubmitHistory);
        } catch (Exception e) {
            log.error("报工失败:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 0;
        }
        return i;
    }

    /**
     * 校验报工数量
     *
     */
    public void validSubmitNumber(MicroWorkSubmit microWorkSubmit) {
        if (microWorkSubmit.getPassNum() == null && microWorkSubmit.getNgNum() == null) {
            throw new CustomException("报工数量不能为空");
        }

        if (microWorkSubmit.getPassNum() != null && microWorkSubmit.getNgNum() == null) {
            if (microWorkSubmit.getPassNum().signum() == 0) {
                throw new CustomException("报工数量不能为0");
            }
            microWorkSubmit.setNgNum(BigDecimal.ZERO);
        }

        if (microWorkSubmit.getPassNum() == null && microWorkSubmit.getNgNum() != null) {
            if (microWorkSubmit.getNgNum().signum() == 0) {
                throw new CustomException("报工数量不能为0");
            }
            microWorkSubmit.setPassNum(BigDecimal.ZERO);
        }

        if (microWorkSubmit.getPassNum() != null && microWorkSubmit.getNgNum() != null) {
            if (microWorkSubmit.getPassNum().add(microWorkSubmit.getNgNum()).signum() == 0) {
                throw new CustomException("报工数量不能为0");
            }
        }
    }

    /**
     * 添加推荐数据(目前使用预研团队的推荐数据,此方法暂时无用)
     *
     */
    private void addHotData(MicroWorkSubmit microWorkSubmit) {
        try {
            MicroSelectEntity productEntity = new MicroSelectEntity();
            productEntity.setItemSeq(microWorkSubmit.getProductSeq());
            productEntity.setItemCode(microWorkSubmit.getProductCode());
            productEntity.setItemName(microWorkSubmit.getProductName());
            MicroSelectEntity operateProcessEntity = new MicroSelectEntity();
            operateProcessEntity.setItemSeq(microWorkSubmit.getOperateProcessSeq());
            operateProcessEntity.setItemCode(microWorkSubmit.getOperateProcessCode());
            operateProcessEntity.setItemName(microWorkSubmit.getOperateProcessName());
            MicroSelectEntity preProcessEntity = new MicroSelectEntity();
            preProcessEntity.setItemSeq(microWorkSubmit.getPreProcessSeq());
            preProcessEntity.setItemCode(microWorkSubmit.getPreProcessCode());
            preProcessEntity.setItemName(microWorkSubmit.getPreProcessName());
            supportUtil.addHotData(productEntity, operateProcessEntity, preProcessEntity);
        } catch (TaskRejectedException e) {
            log.error("add_hot_data was rejected!--->:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
    }

    /**
     * 修改报工记录
     *
     * @param microWorkSubmit 报工记录
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroWorkSubmit(MicroWorkSubmit microWorkSubmit) {
        // 根据id查询数据库获取报工人、报工时间、审核状态、报工单号等字段
        // 作用: 1. 判断该报工记录是否审核  2. 整合数据（变动历史记录）
        MicroWorkSubmit microWorkSubmitExById = microWorkSubmitMapper.selectMicroWorkSubmitExById(microWorkSubmit.getId());
        if (CheckObjectUtils.isEmpty(microWorkSubmitExById)) {
            throw new CustomException("未查询到id为" + microWorkSubmit.getId().toString() + "的报工记录");
        }
        if (microWorkSubmitExById.getSubmitStatus().equals(SubmitStatusEnum.APPROVED.getCode())) {
            throw new CustomException("该报工记录已被审核！");
        }
        // 报工数量校验（编辑路径原来漏了这道闸：新增走 validSubmitNumber，编辑只判显式的 0/0，
        // 否则"编辑一条报工把良品/不良都改成0"能存进去，后续所有比率统计都会踩除零）
        if (microWorkSubmit.getPassNum() != null && microWorkSubmit.getNgNum() != null
                && microWorkSubmit.getPassNum().add(microWorkSubmit.getNgNum()).signum() == 0) {
            throw new CustomException("报工数量不能为0");
        }

        /*
        *  (1) ku易记在编辑的时候可以改变产品和工序等信息，所以需要判断一下是否生成新的产品和工序，判断标准：是否有相应的seq
        *  (2) 工易派在编辑的时候只允许改变数量、备注等信息，不涉及到改变产品和工序信息
        **/
        if (SecurityUtils.getApplicationSign().equals(ApplicationTypeEnum.KU_YI_JI.getCode())) {
            String productSeq = createNewProductForSubmit(microWorkSubmit, null);
            // 内部包含了前工序和当前工序的序列码
            MicroWorkSubmit newProcessSeq = createNewProcessForSubmit(microWorkSubmit, null);
            // 记录编辑 + 实体转换
            microWorkSubmit.setProductSeq(productSeq);
            microWorkSubmit.setOperateProcessSeq(newProcessSeq.getOperateProcessSeq());
            // 前端初始化时这些字段传递的可能是空字符串
            if (StringUtils.isEmpty(newProcessSeq.getPreProcessSeq())) {
                microWorkSubmit.setPreProcessSeq(null);
            } else {
                microWorkSubmit.setPreProcessSeq(newProcessSeq.getPreProcessSeq());
            }
        }

        microWorkSubmit.setLastUpdDate(DateUtils.getNowDate());
        microWorkSubmit.setLastUpdBy(String.valueOf(SecurityUtils.getUserId()));
        // 编辑时报工记录有两种状态: (1) 未审核状态   (2) 驳回状态 ----> 编辑完成: 只有一种待审核状态
        microWorkSubmit.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());

        // 报工人编辑 —> 报工历史记录
        MicroWorkSubmitHistory microWorkSubmitHistory = new MicroWorkSubmitHistory();
        BeanUtils.copyProperties(microWorkSubmit, microWorkSubmitHistory);

        microWorkSubmitHistory.setOperateNode(CommonConstants.SUBMIT_HISTORY_RECORDS_EDIT);
        microWorkSubmitHistory.setSubmitNo(microWorkSubmitExById.getSubmitNo());
        microWorkSubmitHistory.setSubmitDay(microWorkSubmitExById.getSubmitDay());
        microWorkSubmitHistory.setSubmitUser(microWorkSubmitExById.getSubmitUser());
        microWorkSubmitHistory.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());
        microWorkSubmitHistory.setDataStatus(microWorkSubmitExById.getDataStatus());
        // 复制对象之后要重新设置一下时间（是报工记录的创建时间）
        microWorkSubmitHistory.setCreatedDate(DateUtils.getNowDate());
        microWorkSubmitHistory.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
        microWorkSubmitHistoryMapper.insertMicroWorkSubmitHistory(microWorkSubmitHistory);

        return microWorkSubmitMapper.updateMicroWorkSubmit(microWorkSubmit);
    }

    /**
     * 批量删除报工记录
     *
     * @param ids 需要删除的报工记录ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMicroWorkSubmitByIds(Long[] ids) {
        log.info("批量删除报工记录的ids:{}", JSONArray.toJSON(ids));
        // 获取报工号
        List<String> submitNoList = new ArrayList<>();
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectMicroWorkSubmitExByIds(ids, null, null);
        if (CollectionUtils.isEmpty(microWorkSubmitDtoList)) {
            throw new CustomException("未查询到报工记录信息");
        }
        microWorkSubmitDtoList.forEach(obj -> {
            if (SubmitStatusEnum.APPROVED.getCode().equals(obj.getSubmitStatus())) {
                throw new CustomException("已审核的报工记录不可以删除");
            }
            if (CheckStatusEnum.FINISHED_INSPECTION.getCode().equals(obj.getCheckStatus())) {
                throw new CustomException("质检完成的报工记录不可以删除");
            }
            submitNoList.add(obj.getSubmitNo());
        });
        String[] submitNos = submitNoList.toArray(new String[submitNoList.size()]);
        log.info("报工记录号为:{}", JSONArray.toJSON(submitNos));
        // 删除报工变动历史记录
        microWorkSubmitHistoryMapper.deleteMicroWorkSubmitHistoryBySubmitNos(submitNos);
        // 删除报工历史记录
        return microWorkSubmitMapper.deleteMicroWorkSubmitByIds(ids);
    }

    /**
     * 删除报工记录信息
     *
     * @param id 报工记录ID
     * @return 结果
     */
    @Override
    public int deleteMicroWorkSubmitById(Long id) {
        log.info("要删除的报工记录id为:{}", JSONObject.toJSONString(id));

        MicroWorkSubmit microWorkSubmit = microWorkSubmitMapper.selectMicroWorkSubmitById(id);
        if (microWorkSubmit == null) {
            throw new CustomException("未查询到报工记录信息");
        }
        if (microWorkSubmit.getSubmitStatus().equals(SubmitStatusEnum.APPROVED.getCode())) {
            throw new CustomException("已审核的报工记录不可以删除");
        }
        // 换取报工记录号 删除报工历史记录
        String submitNo = microWorkSubmit.getSubmitNo();
        microWorkSubmitHistoryMapper.deleteMicroWorkSubmitHistoryBySubmitNo(submitNo);

        return microWorkSubmitMapper.deleteMicroWorkSubmitById(id);
    }

    /**
     * 获取多天总的报工数量
     *
     */
    @Override
    public List<MicroWorkSubmitProductCount> getProductCountForSubmitByDay(List<String> dates) {
        String userId = String.valueOf(SecurityUtils.getUserId());
        Long submitType;
        if (SecurityUtils.getApplicationSign().equals(ApplicationTypeEnum.GONG_YI_PAI.getCode())) {
            submitType = SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode();
        } else {
            submitType = SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode();
        }
        // 查询总的记工数量
        return microWorkSubmitMapper.getProductCountForSubmitByDay(dates, userId, submitType);
    }


    /**
     * 获取该月里每天的报工数量
     *
     */
    @Override
    public Map<String, BigDecimal> getProductCountForSubmitByMonth(String monthOfYear) {
        // 入参格式校验：仅允许 yyyy-MM，防止非法字符串进入日期解析（异常与注入防护）
        if (StringUtils.isEmpty(monthOfYear) || !monthOfYear.matches("^\\d{4}-\\d{2}$")) {
            throw new CustomException("请输入正确的年月时间信息（格式：yyyy-MM）");
        }
        log.info("时间参数为：{}", monthOfYear);

        // 存储结果
        Map<String, BigDecimal> res = new HashMap<>(16);
        // 获取当前年月日
        LocalDate currentDate = LocalDate.now();
        // 前端传入时间转化
        String year = monthOfYear.substring(0, 4);
        String month = monthOfYear.substring(5, 7);
        // 本月的第一天
        LocalDate startDateOfMonth = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1);
        // 本月的最后一天
        LocalDate endDateOfMonth = startDateOfMonth.with(TemporalAdjusters.lastDayOfMonth());

        // 查询当月指定报工人的报工数量
        List<MicroWorkSubmitProductCount> microWorkSubmitProductCounts = microWorkSubmitMapper.getProductCountForSubmitByMonth(startDateOfMonth, endDateOfMonth, String.valueOf(SecurityUtils.getUserId()));

        // 未查询到该月的报工数据
        if (CheckObjectUtils.isEmpty(microWorkSubmitProductCounts)) {
            return res;
        }
        // 拆分为每天的总报工数量 <年月日， 报工数量实体>
        Map<String, MicroWorkSubmitProductCount> collect = microWorkSubmitProductCounts.stream().collect(Collectors.toMap(MicroWorkSubmitProductCount::getSubmitDay, v -> v));

        // 处理时间
        for (int i = 0; i <= endDateOfMonth.getDayOfMonth() - 1; i++) {
            int day = i + 1;
            String dayStr;
            if (day < 10) {
                dayStr = "0" + day;
            } else {
                dayStr = String.valueOf(day);
            }
            String dateStr = year + "-" + month + "-" + dayStr;
            LocalDate currentDayOfMonth = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), day);
            MicroWorkSubmitProductCount microWorkSubmitProductCount = collect.get(dateStr);
            if (currentDayOfMonth.isAfter(currentDate)) {
                break;
            } else {
                if (CheckObjectUtils.isEmpty(microWorkSubmitProductCount)) {
                    res.put(dateStr, BigDecimal.ZERO);
                } else {
                    res.put(dateStr, microWorkSubmitProductCount.getTotalCounts());
                }
            }
        }
        return res;
    }

    /**
     * 审产首页 - 产品维度 待办
     *
     */
    @Override
    @Deprecated
    public List<MicroWorkSubmitDto> selectSubmitRecordGroupByProduct(MicroWorkSubmitDto microWorkSubmitDto) {

        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectSubmitRecordGroupByProduct(microWorkSubmitDto);
        if (CollectionUtils.isEmpty(microWorkSubmitDtoList)) {
            return Collections.emptyList();
        }
        // 产品维度待审核列表才展示异常的标签
        if (microWorkSubmitDto.getSubmitStatus().equals(SubmitStatusEnum.UN_APPROVE.getCode())) {
            // 获取产品 + 工序的良品率和产能
            Map<String, Map<String, Object>> warningMetrics = supportUtil.getWarningMetrics();
            // 产品 + 工序 + 报工人 + 报工日期的报工数量（审核和未审核）
            Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition = microWorkSubmitMapper.obtainedTotalSubmitNumByCondition();

            // obj为最外侧 产品 + 工序 + 报工时间的对象
            microWorkSubmitDtoList.forEach(obj -> {
                // String -> Long[]
                String[] idsOfString = obj.getIds().split(",");
                Long[] ids = Arrays.stream(idsOfString).map(s -> Long.valueOf(s)).toArray(Long[]::new);

                // 最外侧对象对应的里面实际报工对象
                List<MicroWorkSubmitDto> microWorkSubmitDtoTempList = microWorkSubmitMapper.selectMicroWorkSubmitExByIds(ids, null, null);
                microWorkSubmitDtoTempList = this.judgeSubmitRecordsException(microWorkSubmitDtoTempList, warningMetrics, allSubmitTotalNumByCondition);

                // 设置异常条数
                int count = (int) microWorkSubmitDtoTempList.stream().filter(record -> record.getOverProductiveCapacityFlag().equals(OverProductiveCapacityFlagEnum.OVER.getCode())
                        || record.getNegativeStockFlag().equals(NegativeStockFlagEnum.NEGATIVE_STOCK.getCode())
                        || record.getLowPassRateFlag().equals(LowPassRateFlagEnum.LOW.getCode())).count();
                obj.setExceptionNums(count);

                List<String> lowPassRateFlags = microWorkSubmitDtoTempList.stream().map(MicroWorkSubmitDto::getLowPassRateFlag).collect(Collectors.toList());
                List<String> negativeStockFlags = microWorkSubmitDtoTempList.stream().map(MicroWorkSubmitDto::getNegativeStockFlag).collect(Collectors.toList());
                List<String> overProductiveCapacityFlags = microWorkSubmitDtoTempList.stream().map(MicroWorkSubmitDto::getOverProductiveCapacityFlag).collect(Collectors.toList());

                if (lowPassRateFlags.contains(LowPassRateFlagEnum.LOW.getCode())) {
                    obj.setLowPassRateFlag(LowPassRateFlagEnum.LOW.getCode());
                } else {
                    obj.setLowPassRateFlag(LowPassRateFlagEnum.NORMAL.getCode());
                }

                if (negativeStockFlags.contains(NegativeStockFlagEnum.NEGATIVE_STOCK.getCode())) {
                    obj.setNegativeStockFlag(NegativeStockFlagEnum.NEGATIVE_STOCK.getCode());
                } else {
                    obj.setNegativeStockFlag(NegativeStockFlagEnum.POSITIVE_STOCK.getCode());
                }

                if (overProductiveCapacityFlags.contains(OverProductiveCapacityFlagEnum.OVER.getCode())) {
                    obj.setOverProductiveCapacityFlag(OverProductiveCapacityFlagEnum.OVER.getCode());
                } else {
                    obj.setOverProductiveCapacityFlag(OverProductiveCapacityFlagEnum.NOT_OVER.getCode());
                }
            });
        }

        return microWorkSubmitDtoList;
    }

    /**
     * 新的产品维度审核展示列表
     *
     */
    @Override
    public List<SubmitRecordGroupByProduct> selectSubmitRecordGroupByProductList(CheckByProductParam checkByProductParam) {
        List<SubmitRecordGroupByProduct> submitRecordGroupByProducts = microWorkSubmitMapper.selectSubmitRecordGroupByProductList(checkByProductParam);
        if (CollectionUtils.isEmpty(submitRecordGroupByProducts)) {
            return Collections.emptyList();
        }
        // 产品维度待审核列表才展示异常的标签
        if (SubmitStatusEnum.UN_APPROVE.getCode().equals(checkByProductParam.getSubmitStatus())) {
            // 获取产品 + 工序的良品率和产能
            Map<String, Map<String, Object>> warningMetrics = supportUtil.getWarningMetrics();
            // 产品 + 工序 + 报工人 + 报工日期的报工数量（审核和未审核）
            Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition = microWorkSubmitMapper.obtainedTotalSubmitNumByCondition();

            for (SubmitRecordGroupByProduct submitRecordGroupByProduct : submitRecordGroupByProducts) {
                // String -> Long[]
                String[] idsOfString = submitRecordGroupByProduct.getIds().split(",");
                Long[] ids = Arrays.stream(idsOfString).map(Long::valueOf).toArray(Long[]::new);

                List<MicroWorkSubmitDto> microWorkSubmitDtoTempList = microWorkSubmitMapper.selectMicroWorkSubmitExByIds(ids, null, null);
                microWorkSubmitDtoTempList = this.judgeSubmitRecordsException(microWorkSubmitDtoTempList, warningMetrics, allSubmitTotalNumByCondition);

                List<String> lowPassRateFlags = microWorkSubmitDtoTempList.stream().map(MicroWorkSubmitDto::getLowPassRateFlag).collect(Collectors.toList());
                List<String> negativeStockFlags = microWorkSubmitDtoTempList.stream().map(MicroWorkSubmitDto::getNegativeStockFlag).collect(Collectors.toList());
                List<String> overProductiveCapacityFlags = microWorkSubmitDtoTempList.stream().map(MicroWorkSubmitDto::getOverProductiveCapacityFlag).collect(Collectors.toList());
                if (lowPassRateFlags.contains(LowPassRateFlagEnum.LOW.getCode())
                        || negativeStockFlags.contains(NegativeStockFlagEnum.NEGATIVE_STOCK.getCode())
                        || overProductiveCapacityFlags.contains(OverProductiveCapacityFlagEnum.OVER.getCode())) {
                    submitRecordGroupByProduct.setWarnFlag(WarnFlagEnum.WARN.getCode());
                } else {
                    submitRecordGroupByProduct.setWarnFlag(WarnFlagEnum.NO_WARN.getCode());
                }
            }
        }
        return submitRecordGroupByProducts;
    }

    /**
     * 产品维度下 详细的报工记录信息展示
     *
     */
    @Override
    public DetailCheckSubmitRecordByProductRes showDetailSubmitRecordByProduct(String productSeq, List<MicroWorkSubmitDto> microWorkSubmitDtoList, List<MicroProcessChainBindEntity> bindEntityList) {

        // 存储返回值
        DetailCheckSubmitRecordByProductRes result = new DetailCheckSubmitRecordByProductRes();
        // 存储详细的到工序的报工记录结果
        List<DetailCheckSubmitRecordByProduct> detailRecordsByTech = new ArrayList<>();
        // 获取所有产品+工序的库存信息
        Map<String, StorageForProductAndProcess> allStorageMap = microProcessStorageMapper.selectStorageByProductAndProcess();
        // 获取产品 + 工序的良品率和产能
        Map<String, Map<String, Object>> warningMetrics = supportUtil.getWarningMetrics();
        // 产品 + 工序 + 报工人 + 报工日期的报工数量（审核和未审核）
        Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition = microWorkSubmitMapper.obtainedTotalSubmitNumByCondition();
        // 获取产品单位
        String productUnit = microWorkSubmitDtoList.get(0).getUnit();

        // 记录要审核的报工记录数量
        int needCheckRecordNum = microWorkSubmitDtoList.size();
        int exceptionRecordNum = 0;

        // 针对标准工艺中顺序的情况，过滤不符合的标准工艺
        List<Long> ids = this.filterSubmitRecordByStandardTech(microWorkSubmitDtoList, bindEntityList);
        log.info("不合符工艺的报工记录id为:{}", ids.toString());
        result.setUnStandardTechSubmitRecordIds(ids);
        if (!CollectionUtils.isEmpty(ids)) {
            // 过滤出符合标准工艺的报工记录
            microWorkSubmitDtoList = microWorkSubmitDtoList.stream().filter(obj -> !ids.contains(obj.getId())).collect(Collectors.toList());
            needCheckRecordNum = microWorkSubmitDtoList.size();
        }

        if (!CollectionUtils.isEmpty(microWorkSubmitDtoList)) {
            Integer techPattern = null;
            if (!CollectionUtils.isEmpty(bindEntityList)) {
                techPattern = bindEntityList.get(0).getTechPattern();
            }
            // 存储详细的到工序的报工记录结果，根据报工记录构建
            detailRecordsByTech = this.createDetailSubmitRecordDisplayInfo(productSeq, productUnit, microWorkSubmitDtoList,
                    allStorageMap, warningMetrics, allSubmitTotalNumByCondition, techPattern);

            // 构建展示顺序
            if (techPattern != null) {
                result.setStandard(true);
                if (techPattern.equals(TechPatternEnum.SERIAL.getCode())) {
                    detailRecordsByTech = this.detailSubmitRecordInfoByProductFromSequentialStandardTech(detailRecordsByTech, bindEntityList);
                } else {
                    detailRecordsByTech = this.detailSubmitRecordInfoByProductFromNonSequentialStandardTech(detailRecordsByTech, bindEntityList);
                }
            } else {
                result.setStandard(false);
                // 找出一个合适的顺序
                detailRecordsByTech = this.findValidChainForNotStandardCraft(detailRecordsByTech);
            }

            for (DetailCheckSubmitRecordByProduct recordsByTech : detailRecordsByTech) {
                if (!CollectionUtils.isEmpty(recordsByTech.getUserDetailSubmitRecordInfos())) {
                    for (UserDetailSubmitRecordInfo recordInfoByUser : recordsByTech.getUserDetailSubmitRecordInfos()) {
                        exceptionRecordNum = exceptionRecordNum + recordInfoByUser.getExceptionRecordNum();
                    }
                }
            }
        }

        result.setExceptionRecordNum(exceptionRecordNum);
        result.setNeedCheckRecordNum(needCheckRecordNum);
        result.setDetailCheckSubmitRecordByProductList(detailRecordsByTech);

        return result;
    }

    /**
     * 查找工艺链中的尾巴节点，如果是并序的则合并前工序等信息
     *
     */
    @Deprecated
    private List<MicroProcessChainBindEntity> findChainLastNode(List<MicroProcessChainBindEntity> bindEntityList) {
        // 找到尾巴节点
        List<MicroProcessChainBindEntity> nodes = bindEntityList.stream().filter(obj -> IsLastProcessEnum.YES.getCode().equals(obj.getIsLastProcess()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(nodes)) {
            throw new CustomException("该工艺没有尾节点");
        }
        if (nodes.stream().map(MicroProcessChainBindEntity::getProcessSeq).distinct().count() > 1) {
            throw new CustomException("尾节点数量不能大于2");
        }
        nodes = this.mergeNextNodeList(nodes);
        // 其实只有一个
        return nodes;
    }

    /**
     * 审产首页 - 产品维度 待办 (数量)
     *
     */
    @Override
    @Deprecated
    public int obtainedTodoNumsByProduct(MicroWorkSubmitDto microWorkSubmitDto) {
        return microWorkSubmitMapper.obtainedTodoNumsByProduct(microWorkSubmitDto);
    }

    /**
     * 审产首页 - 员工维度
     *
     */
    @Override
    public List<MicroWorkSubmitDto> selectSubmitRecordByUser(SubmitRecordQueryParam submitRecordQueryParam) {
        submitRecordQueryParam.setSubmitType(supportUtil.getSubmitType());
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectSubmitRecordByUser(submitRecordQueryParam);
        if (CollectionUtils.isEmpty(microWorkSubmitList)) {
            return Collections.emptyList();
        }

        // 有审产人的报工记录则获取审产人的中文名
        microWorkSubmitList.forEach(obj -> {
            if (!org.springframework.util.StringUtils.isEmpty(obj.getCheckUser())) {
                String checkNickName = getNickName(obj.getCheckUser());
                obj.setCheckNickName(checkNickName);
            }
        });
        // 返回对象为Dto
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = BeanUtil.copyToList(microWorkSubmitList, MicroWorkSubmitDto.class);

        if (submitRecordQueryParam.getSubmitStatus().equals(SubmitStatusEnum.UN_APPROVE.getCode())) {
            // 获取产品 + 工序的良品率和产能
            Map<String, Map<String, Object>> warningMetrics = supportUtil.getWarningMetrics();
            // 产品 + 工序 + 报工人 + 报工日期的报工数量（审核和未审核）
            Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition = microWorkSubmitMapper.obtainedTotalSubmitNumByCondition();

            for (MicroWorkSubmitDto microWorkSubmitDto : microWorkSubmitDtoList) {
                // 异常判断
                this.judgeSingleSubmitRecordException(microWorkSubmitDto,warningMetrics,allSubmitTotalNumByCondition);
            }
        }

        return MicroPageUtils.listToPage(microWorkSubmitList, microWorkSubmitDtoList);
    }

    /**
     * 审产首页 - 员工维度 待办 (数量)
     *
     */
    @Override
    @Deprecated
    public int obtainedTodoNumsByUser(MicroWorkSubmitDto microWorkSubmitDto) {
        return microWorkSubmitMapper.obtainedTodoNumsByUser(microWorkSubmitDto);
    }

    /**
     * 审产数量归一（所有审产入口统一收口）
     *
     * <p>规则（"按报工数审产"是既有默认语义：见下面各入口原来的 {@code checkXxxNum == null → 复制报工数}）：
     * <ul>
     *   <li>审产数量为 <b>null 或 0/0</b>（视为"没填"，历史上前端弹窗默认 0 也走这里）→ <b>按报工数回填</b>；</li>
     *   <li>回填依据（报工数）也为 0 → 抛错，提示填写数量或驳回（不允许产生"0 审产"的空记录）；</li>
     *   <li>审产数量为负 → 抛错。</li>
     * </ul>
     *
     * <p>背景：原来只在 null 时回填，前端传 0（不是 null）就写了 0/0 的已审记录 →
     * 良品率 = 0/(0+0) 无意义 → 趋势图/良品率/排行 多处 / by zero 500（生产事故已发生一次）。
     */
    private void normalizeCheckNums(MicroWorkSubmit obj) {
        BigDecimal pass = obj.getPassNum();
        BigDecimal ng = obj.getNgNum();
        // 只带 id 进来的入参（批量审产）需要回查报工数作为回填依据
        if ((pass == null || ng == null) && obj.getId() != null) {
            MicroWorkSubmit db = microWorkSubmitMapper.selectMicroWorkSubmitById(obj.getId());
            if (db != null) {
                if (pass == null) {
                    pass = db.getPassNum();
                }
                if (ng == null) {
                    ng = db.getNgNum();
                }
            }
        }
        BigDecimal checkPass = obj.getCheckPassNum();
        BigDecimal checkNg = obj.getCheckNgNum();
        if ((checkPass != null && checkPass.signum() < 0) || (checkNg != null && checkNg.signum() < 0)) {
            throw new CustomException("审产后的良品/不良数量不能为负数");
        }
        boolean blank = (checkPass == null || checkPass.signum() == 0)
                && (checkNg == null || checkNg.signum() == 0);
        if (!blank) {
            // 有值：把缺的一侧补 0（避免下游 NPE）
            if (checkPass == null) {
                obj.setCheckPassNum(BigDecimal.ZERO);
            }
            if (checkNg == null) {
                obj.setCheckNgNum(BigDecimal.ZERO);
            }
            return;
        }
        BigDecimal passNum = pass == null ? BigDecimal.ZERO : pass;
        BigDecimal ngNum = ng == null ? BigDecimal.ZERO : ng;
        if (passNum.add(ngNum).signum() > 0) {
            obj.setCheckPassNum(passNum);
            obj.setCheckNgNum(ngNum);
            log.warn("[审产] 报工记录{} 审产数量为空/0，已按报工数回填 良品{} 不良{}",
                    obj.getId(), passNum, ngNum);
            return;
        }
        throw new CustomException("审产数量不能为 0：该记工的报工数也为 0，请填写良品/不良数量，或驳回该记工");
    }

    /**
     * 批量审核报工记录
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MicroWorkSubmit> checkSubmitRecordBatch(List<MicroWorkSubmit> microWorkSubmitList) {
        // 存储新增的报工历史记录
        List<MicroWorkSubmitHistory> insertListForSubmitHistory = new ArrayList<>();
        // 获取审核人的昵称
        String userId = String.valueOf(SecurityUtils.getUserId());

        // 2. 更新报工记录（状态, 审核人, 审核时间， 审核之后的良品和不良品数量）
        microWorkSubmitList.forEach(obj -> {
            obj.setSubmitStatus(SubmitStatusEnum.APPROVED.getCode());
            obj.setCheckUser(userId);
            obj.setCheckDate(DateUtils.getNowDate());
            // 审产数量归一：null/0 → 按报工数回填；报工数也为 0 → 拒绝（见 normalizeCheckNums）
            this.normalizeCheckNums(obj);
            obj.setLastUpdBy(userId);
            obj.setLastUpdDate(DateUtils.getNowDate());

            // 报工历史记录 (批量审核只会改变报工记录的审核状态，不会发生记录变更)
            MicroWorkSubmitHistory microWorkSubmitHistoryTemp = new MicroWorkSubmitHistory();
            BeanUtils.copyProperties(obj, microWorkSubmitHistoryTemp);
            microWorkSubmitHistoryTemp.setCreatedDate(DateUtils.getNowDate());
            microWorkSubmitHistoryTemp.setCreatedBy(userId);
            microWorkSubmitHistoryTemp.setSubmitStatus(SubmitStatusEnum.APPROVED.getCode());
            // 报工历史记录添加审核的操作节点
            microWorkSubmitHistoryTemp.setOperateNode(CommonConstants.SUBMIT_HISTORY_RECORDS_CHECK);
            // 报工记录历史以审核以后数量为准
            microWorkSubmitHistoryTemp.setPassNum(obj.getCheckPassNum());
            microWorkSubmitHistoryTemp.setNgNum(obj.getCheckNgNum());
            insertListForSubmitHistory.add(microWorkSubmitHistoryTemp);
        });

        // 3. 批量更新相关记录 (报工、库存变动历史记录、报工历史记录)
        this.batchInsertOrUpdate(microWorkSubmitList, insertListForSubmitHistory);

        return microWorkSubmitList;
    }

    /**
     * 报工明细 - 审产接口 (需传递 id 和 isLastProcess 字段)
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MicroWorkSubmit> checkSubmitRecordBatchByProduct(List<MicroWorkSubmit> microWorkSubmitList) {
        // 打印入参
        log.info("请求参数为:{}", JSONObject.toJSONString(microWorkSubmitList));
        // 校验参数
        if (CheckObjectUtils.isEmpty(microWorkSubmitList)) {
            throw new CustomException("参数不能为空");
        }
        microWorkSubmitList.forEach(param -> {
            if (CheckObjectUtils.isEmpty(param.getId())) {
                throw new CustomException("报工记录id不能为空");
            }
            if (CheckObjectUtils.isEmpty(param.getIsLastProcess())) {
                throw new CustomException("报工记录的最后一道工序标志不能为空");
            }
            if (CheckObjectUtils.isEmpty(param.getIsFirstProcess())) {
                throw new CustomException("报工记录的首序标识不能为空");
            }
        });

        // 获取用户的userId
        String userId = String.valueOf(SecurityUtils.getUserId());
        // 存储要插入或者更新的对象
        List<MicroWorkSubmitHistory> insertForHistorySubmitRecords = new ArrayList<>();

        // 根据入参获取ids -> 查询数据库
        Map<Long, List<MicroWorkSubmit>> collect = microWorkSubmitList.stream().collect(Collectors.groupingBy(MicroWorkSubmit::getId));
        Long[] ids = collect.keySet().toArray(new Long[0]);
        List<MicroWorkSubmit> submitRecords = microWorkSubmitMapper.selectMicroWorkSubmitByIds(ids);
        if (CheckObjectUtils.isEmpty(submitRecords)) {
            throw new CustomException("未查询到相关报工记录信息");
        }
        submitRecords.forEach(obj -> {
            // 判断报工记录是否被审核过
            if (obj.getSubmitStatus().equals(SubmitStatusEnum.APPROVED.getCode())) {
                throw new CustomException("报工记录:" + obj.getId().toString() + "已被审核");
            }
        });


        // 业务操作
        submitRecords.forEach(obj -> {
            // 1. 更新报工记录
            // 判断报工记录是否发生变更,不变更的话有默认值
            MicroWorkSubmit paramTemp = collect.get(obj.getId()).get(0);
            // 针对报工记录不是首序，但是被人为勾选了首序这种的情况，将前工序的信息都置空
            // 相反，报工记录是首序，但是被人为勾选了我不是首序，没有办法给它在添加前工序的信息，不做处理
            if (IsFirstProcessEnum.NO.getCode().equals(obj.getIsFirstProcess()) && IsFirstProcessEnum.YES.getCode().equals(paramTemp.getIsFirstProcess())) {
                obj.setPreProcessSeq(null);
                obj.setPreProcessCode(null);
                obj.setPreProcessName(null);
                obj.setPreProcessGroup(null);
            }
            if (!obj.getIsLastProcess().equals(paramTemp.getIsLastProcess()) || !obj.getIsFirstProcess().equals(paramTemp.getIsFirstProcess())) {
                // 为了更新变动历史记录
                obj.setDataStatus(DataStatusEnum.CHANGED.getCode());
                obj.setIsLastProcess(paramTemp.getIsLastProcess());
                obj.setIsFirstProcess(paramTemp.getIsFirstProcess());
                log.info("报工记录:{}发生了记录变更(是否是最后一道工序)", obj.getId());
            }
            obj.setSubmitStatus(SubmitStatusEnum.APPROVED.getCode());
            // 审产数量归一：null/0 → 按报工数回填；报工数也为 0 → 拒绝（见 normalizeCheckNums）
            this.normalizeCheckNums(obj);
            obj.setLastUpdBy(userId);
            obj.setLastUpdDate(DateUtils.getNowDate());
            // 审核人存放账号信息
            obj.setCheckUser(userId);
            obj.setCheckDate(DateUtils.getNowDate());
            obj.setSubmitStatus(SubmitStatusEnum.APPROVED.getCode());

            // 2. 生成报工历史记录
            MicroWorkSubmitHistory submitHistoryTemp = new MicroWorkSubmitHistory();
            BeanUtils.copyProperties(obj, submitHistoryTemp);
            // 报工历史记录添加审核的操作节点
            submitHistoryTemp.setOperateNode(CommonConstants.SUBMIT_HISTORY_RECORDS_CHECK);
            submitHistoryTemp.setCreatedBy(userId);
            submitHistoryTemp.setCreatedDate(DateUtils.getNowDate());
            // 报工记录历史以审核以后数量为准
            submitHistoryTemp.setPassNum(obj.getCheckPassNum());
            submitHistoryTemp.setNgNum(obj.getCheckNgNum());
            insertForHistorySubmitRecords.add(submitHistoryTemp);
        });
        // 3. 批量更新相关记录 (报工、库存变动历史记录、报工历史记录)
        this.batchInsertOrUpdate(submitRecords, insertForHistorySubmitRecords);

        return submitRecords;
    }

    /**
     * 编辑并审核 (单条报工记录)
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MicroWorkSubmit editAndCheck(MicroWorkSubmit microWorkSubmit) {
        // 1. 先从报工表获取一下报工记录信息（最新的） 作用： 1.为了判断单据是否已经被审核 2.为了判断单据是否变更
        MicroWorkSubmit microWorkSubmitExById = microWorkSubmitMapper.selectMicroWorkSubmitById(microWorkSubmit.getId());
        if (CheckObjectUtils.isEmpty(microWorkSubmitExById)) {
            throw new CustomException("未查询到id为" + microWorkSubmit.getId().toString() + "的报工记录");
        }
        if (microWorkSubmitExById.getSubmitStatus().equals(SubmitStatusEnum.APPROVED.getCode())) {
            throw new CustomException("该报工记录已被审核");
        }

        // 2. 判断报工记录信息是否发生了变化, ku易记和工易派不同
        if (SecurityUtils.getApplicationSign().equals(ApplicationTypeEnum.GONG_YI_PAI.getCode())) {
            // 工易派 只有数量、图片和备注会发生变化
            if (this.validSubmitRecordChange(microWorkSubmit, microWorkSubmitExById)) {
                microWorkSubmit.setDataStatus(DataStatusEnum.CHANGED.getCode());
            } else {
                microWorkSubmit.setDataStatus(DataStatusEnum.UN_CHANGED.getCode());
            }
        } else {
            // 判断是否有新的产品或者工序生成
            List<MicroProduct> insertModifyMicroProducts = new ArrayList<>();
            List<MicroProcessCommon> insertModifyMicroProcesses = new ArrayList<>();
            String productSeq = this.createNewProductForSubmit(microWorkSubmit, insertModifyMicroProducts);
            MicroWorkSubmit processSeq = this.createNewProcessForSubmit(microWorkSubmit, insertModifyMicroProcesses);

            microWorkSubmit.setProductSeq(productSeq);
            if (StringUtils.isEmpty(processSeq.getPreProcessSeq())) {
                microWorkSubmit.setPreProcessSeq(null);
            } else {
                microWorkSubmit.setPreProcessSeq(processSeq.getPreProcessSeq());
            }
            microWorkSubmit.setOperateProcessSeq(processSeq.getOperateProcessSeq());
            // 如果有新的产品或者工序，则要更新相应的编码、名称等信息
            if (CheckObjectUtils.isNotEmpty(insertModifyMicroProducts)) {
                microWorkSubmit.setProductName(insertModifyMicroProducts.get(0).getProductName());
                microWorkSubmit.setProductCode(insertModifyMicroProducts.get(0).getProductCode());
            }
            if (CheckObjectUtils.isNotEmpty(insertModifyMicroProcesses)) {
                Map<String, MicroProcessCommon> collect = insertModifyMicroProcesses.stream().collect(Collectors.toMap(MicroProcessCommon::getProcessSeq, v -> v));
                if (CheckObjectUtils.isNotEmpty(collect.get(microWorkSubmit.getPreProcessSeq()))) {
                    microWorkSubmit.setPreProcessName(collect.get(microWorkSubmit.getPreProcessSeq()).getProcessName());
                    microWorkSubmit.setPreProcessCode(collect.get(microWorkSubmit.getPreProcessSeq()).getProcessCode());
                }
                if (CheckObjectUtils.isNotEmpty(collect.get(microWorkSubmit.getOperateProcessSeq()))) {
                    microWorkSubmit.setOperateProcessName(collect.get(microWorkSubmit.getOperateProcessSeq()).getProcessName());
                    microWorkSubmit.setOperateProcessCode(collect.get(microWorkSubmit.getOperateProcessSeq()).getProcessCode());
                }
            }
            // Ku易记判断单据是否发生了变更 (产品、前工序、后工序、审核后良品数量、审核后不良品数量、备注)
            microWorkSubmitExById.setCheckPassNum(microWorkSubmitExById.getPassNum());
            microWorkSubmitExById.setCheckNgNum(microWorkSubmitExById.getNgNum());
            log.info("原始记录的信息为:{}", JSONObject.toJSONString(microWorkSubmitExById));
            if (CompareFieldUtils.changeOrNot(microWorkSubmit, microWorkSubmitExById)) {
                microWorkSubmit.setDataStatus(DataStatusEnum.CHANGED.getCode());
                log.info("报工记录发生变化");
            } else {
                microWorkSubmit.setDataStatus(DataStatusEnum.UN_CHANGED.getCode());
                log.info("报工记录未发生变化");
            }
        }
        microWorkSubmit.setCheckUser(String.valueOf(SecurityUtils.getUserId()));
        microWorkSubmit.setCheckDate(DateUtils.getNowDate());
        // 审产数量归一：null/0 → 按报工数回填；报工数也为 0 → 拒绝（与两个批量审产入口同一口径）
        this.normalizeCheckNums(microWorkSubmit);
        microWorkSubmit.setSubmitStatus(SubmitStatusEnum.APPROVED.getCode());
        microWorkSubmit.setLastUpdBy(String.valueOf(SecurityUtils.getUserId()));
        microWorkSubmit.setLastUpdDate(DateUtils.getNowDate());
        microWorkSubmit.setTenantCode((String) ThreadContext.get(Constants.TARGET_CUSTOMER));
        // 更新报工记录信息
        microWorkSubmitMapper.updateMicroWorkSubmit(microWorkSubmit);

        // 3. 新增报工记录历史
        MicroWorkSubmitHistory microWorkSubmitHistory = new MicroWorkSubmitHistory();
        BeanUtils.copyProperties(microWorkSubmit, microWorkSubmitHistory);
        microWorkSubmitHistory.setOperateNode(CommonConstants.SUBMIT_HISTORY_RECORDS_CHECK);
        microWorkSubmitHistory.setPassNum(microWorkSubmit.getCheckPassNum());
        microWorkSubmitHistory.setNgNum(microWorkSubmit.getCheckNgNum());
        microWorkSubmitHistory.setSubmitNo(microWorkSubmitExById.getSubmitNo());
        microWorkSubmitHistory.setSubmitUser(microWorkSubmitExById.getSubmitUser());
        microWorkSubmitHistory.setSubmitDay(microWorkSubmitExById.getSubmitDay());
        microWorkSubmitHistory.setCreatedBy(SecurityUtils.getUserId().toString());
        microWorkSubmitHistory.setCreatedDate(DateUtils.getNowDate());
        microWorkSubmitHistoryMapper.insertMicroWorkSubmitHistory(microWorkSubmitHistory);

        // 4. Ku易记需要submitUser字段
        microWorkSubmit.setSubmitUser(microWorkSubmitExById.getSubmitUser());

        return microWorkSubmit;
    }

    /**
     * 根据产品查询工序list
     *
     */
    @Override
    public List<MicroProcessCommon> findProcessListByProductSeq(String productSeq, String isLastProcess) {
        // 存储结果
        List<MicroProcessCommon> res = new ArrayList<>();
        List<MicroWorkSubmitDto> processListByProductSeq = microWorkSubmitMapper.getProcessByProductSeq(productSeq, isLastProcess, null, null);
        if (CollectionUtils.isEmpty(processListByProductSeq)) {
            return res;
        }
        processListByProductSeq.forEach(obj -> {
            MicroProcessCommon temp = new MicroProcessCommon();
            temp.setProcessName(obj.getOperateProcessName());
            temp.setProcessCode(obj.getOperateProcessCode());
            temp.setProcessSeq(obj.getOperateProcessSeq());
            res.add(temp);
        });
        return res;
    }

    /**
     * 检查该产品当前工序是否已经有最后一道工序
     *
     */
    @Override
    @Deprecated
    public MicroProcessCommon checkIsLastProcess(String productSeq, String processSeq) {

        MicroProcessCommon microProcessCommon = new MicroProcessCommon();
        if (StringUtils.isEmpty(productSeq)) {
            return null;
        } else {
            // 查询该产品下面的最后一道工序
            List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.getProcessByProductSeq(productSeq, IsLastProcessEnum.YES.getCode(), null, null);
            if (CheckObjectUtils.isEmpty(microWorkSubmitDtoList)) {
                return null;
            }
            // 要判断当前序列码是否在最后一道工序的序列码列表中
            // 获取该产品最后一道工序下面的所有工序序列码
            Set<String> collect = microWorkSubmitDtoList.stream().map(MicroWorkSubmitDto::getOperateProcessSeq).collect(Collectors.toSet());
            if (StringUtils.hasText(processSeq)) {
                if (collect.contains(processSeq)) {
                    return null;
                } else {
                    MicroWorkSubmitDto microWorkSubmitDto = microWorkSubmitDtoList.get(0);
                    microProcessCommon.setProcessSeq(microWorkSubmitDto.getOperateProcessSeq());
                    microProcessCommon.setProcessCode(microWorkSubmitDto.getOperateProcessCode());
                    microProcessCommon.setProcessName(microWorkSubmitDto.getOperateProcessName());

                    return microProcessCommon;
                }
            } else {
                MicroWorkSubmitDto microWorkSubmitDto = microWorkSubmitDtoList.get(0);
                microProcessCommon.setProcessSeq(microWorkSubmitDto.getOperateProcessSeq());
                microProcessCommon.setProcessCode(microWorkSubmitDto.getOperateProcessCode());
                microProcessCommon.setProcessName(microWorkSubmitDto.getOperateProcessName());

                return microProcessCommon;
            }
        }
    }

    /**
     * 根据产品 + 工序 获取首尾序的标示
     *
     */
    @Override
    public FirstOrLastProcess getFirstOrLastProcessFlagByRecords(String productSeq, String processSeq) {
        if (!StringUtils.hasText(productSeq) || !StringUtils.hasText(processSeq)) {
            return null;
        }
        return microWorkSubmitMapper.selectIsFirstAndLastProcess(productSeq, processSeq, SubmitStatusEnum.APPROVED.getCode());
    }

    /**
     * 检查该产品当前工序是否已经有首序或者尾序
     *
     */
    @Override
    public CheckFirstOrLastProcess checkIsOrNotHaveFirstOrLastProcess(String productSeq, String processSeq, String processType) { 
        log.info("请求入参为:productSeq:{}-processSeq:{}-processType:{}", productSeq, processSeq, processType);

        CheckFirstOrLastProcess res = new CheckFirstOrLastProcess();
        if (CheckObjectUtils.isAnyEmpty(productSeq, processType)) {
            throw new CustomException("产品序列码和指定工序类型参数均不能为空");
        }

        // 我是首序
        if (ProcessTypeEnum.FIRST_PROCESS.getCode().equals(processType)) {
            // 该产品是否已经有首序
            List<MicroWorkSubmitDto> firstProcessByProductSeq = microWorkSubmitMapper.getProcessByProductSeq(productSeq, null, IsFirstProcessEnum.YES.getCode(), SubmitStatusEnum.APPROVED.getCode());
            // processSeq是新建工序可能为空
            if (StringUtils.isEmpty(processSeq)) {
                if (!CollectionUtils.isEmpty(firstProcessByProductSeq)) {
                    // 选取第一个作为首序（有可能只有一 个）
                    MicroWorkSubmitDto microWorkSubmitDto = firstProcessByProductSeq.get(0);
                    res.setProcessSeq(microWorkSubmitDto.getOperateProcessSeq());
                    res.setProcessCode(microWorkSubmitDto.getOperateProcessCode());
                    res.setProcessName(microWorkSubmitDto.getOperateProcessName());
                    // 不是互斥的 但是存在其他工序作为首序
                    res.setIsOrNotMutex(CommonConstants.NO);
                }
            } else {
                // 根据产品查询报工记录，找到是否有尾序或者首序
                FirstOrLastProcess firstOrLastProcess = microWorkSubmitMapper.selectIsFirstAndLastProcess(productSeq, processSeq, SubmitStatusEnum.APPROVED.getCode());
                // 首先判断该产品 + 工序是否已经被指定为了尾序
                if (firstOrLastProcess != null) {
                    // 如果是尾序
                    if (IsLastProcessEnum.YES.getCode().equals(firstOrLastProcess.getIsLastProcess())) {
                        // 首序判断使用互斥
                        res.setIsOrNotMutex(CommonConstants.YES);
                    } else {
                        if (!CollectionUtils.isEmpty(firstProcessByProductSeq)) {
                            List<String> collect = firstProcessByProductSeq.stream().map(MicroWorkSubmitDto::getOperateProcessSeq).collect(Collectors.toList());
                            if (!collect.contains(processSeq)) {
                                // 选取第一个作为首序（有可能只有一 个）
                                MicroWorkSubmitDto microWorkSubmitDto = firstProcessByProductSeq.get(0);
                                res.setProcessSeq(microWorkSubmitDto.getOperateProcessSeq());
                                res.setProcessCode(microWorkSubmitDto.getOperateProcessCode());
                                res.setProcessName(microWorkSubmitDto.getOperateProcessName());
                                // 不是互斥的 但是存在其他工序作为首序
                                res.setIsOrNotMutex(CommonConstants.NO);
                            }
                        }
                    }
                } else {
                    // 不是尾序
                    if (!CollectionUtils.isEmpty(firstProcessByProductSeq)) {
                        List<String> collect = firstProcessByProductSeq.stream().map(MicroWorkSubmitDto::getOperateProcessSeq).collect(Collectors.toList());
                        if (!collect.contains(processSeq)) {
                            // 选取第一个作为首序（有可能只有一 个）
                            MicroWorkSubmitDto microWorkSubmitDto = firstProcessByProductSeq.get(0);
                            res.setProcessSeq(microWorkSubmitDto.getOperateProcessSeq());
                            res.setProcessCode(microWorkSubmitDto.getOperateProcessCode());
                            res.setProcessName(microWorkSubmitDto.getOperateProcessName());
                            // 不是互斥的 但是存在其他工序作为首序
                            res.setIsOrNotMutex(CommonConstants.NO);
                        }
                    }
                }
            }
        } else if (ProcessTypeEnum.LAST_PROCESS.getCode().equals(processType)) {
            // 我是尾序判断
            // 首先判断该产品 + 工序是否已经被指定为了尾序
            List<MicroWorkSubmitDto> lastProcessByProductSeq = microWorkSubmitMapper.getProcessByProductSeq(productSeq, IsLastProcessEnum.YES.getCode(), null, SubmitStatusEnum.APPROVED.getCode());
            if (StringUtils.isEmpty(processSeq)) {
                if (!CollectionUtils.isEmpty(lastProcessByProductSeq)) {
                    // 选取第一个作为尾序（有可能只有一个）
                    MicroWorkSubmitDto microWorkSubmitDto = lastProcessByProductSeq.get(0);
                    res.setProcessSeq(microWorkSubmitDto.getOperateProcessSeq());
                    res.setProcessCode(microWorkSubmitDto.getOperateProcessCode());
                    res.setProcessName(microWorkSubmitDto.getOperateProcessName());
                    // 不是互斥的 但是存在其他工序作为尾序
                    res.setIsOrNotMutex(CommonConstants.NO);
                }
            } else {
                FirstOrLastProcess firstOrLastProcess = microWorkSubmitMapper.selectIsFirstAndLastProcess(productSeq, processSeq, SubmitStatusEnum.APPROVED.getCode());
                if (firstOrLastProcess != null) {
                    // 如果是尾序
                    if (IsFirstProcessEnum.YES.getCode().equals(firstOrLastProcess.getIsFirstProcess())) {
                        // 尾序判断使用互斥
                        res.setIsOrNotMutex(CommonConstants.YES);
                    } else {
                        if (!CollectionUtils.isEmpty(lastProcessByProductSeq)) {
                            List<String> collect = lastProcessByProductSeq.stream().map(MicroWorkSubmitDto::getOperateProcessSeq).collect(Collectors.toList());
                            if (!collect.contains(processSeq)) {
                                // 选取第一个作为尾序（有可能只有一个）
                                MicroWorkSubmitDto microWorkSubmitDto = lastProcessByProductSeq.get(0);
                                res.setProcessSeq(microWorkSubmitDto.getOperateProcessSeq());
                                res.setProcessCode(microWorkSubmitDto.getOperateProcessCode());
                                res.setProcessName(microWorkSubmitDto.getOperateProcessName());
                                // 不是互斥的 但是存在其他工序作为尾序
                                res.setIsOrNotMutex(CommonConstants.NO);
                            }
                        }
                    }
                } else {
                    if (!CollectionUtils.isEmpty(lastProcessByProductSeq)) {
                        List<String> collect = lastProcessByProductSeq.stream().map(MicroWorkSubmitDto::getOperateProcessSeq).collect(Collectors.toList());
                        if (!collect.contains(processSeq)) {
                            // 选取第一个作为尾序（有可能只有一个）
                            MicroWorkSubmitDto microWorkSubmitDto = lastProcessByProductSeq.get(0);
                            res.setProcessSeq(microWorkSubmitDto.getOperateProcessSeq());
                            res.setProcessCode(microWorkSubmitDto.getOperateProcessCode());
                            res.setProcessName(microWorkSubmitDto.getOperateProcessName());
                            // 不是互斥的 但是存在其他工序作为尾序
                            res.setIsOrNotMutex(CommonConstants.NO);
                        }
                    }
                }
            }
        } else {
            // 既是首序又是尾序的判断
            // 不需要考虑互斥了
            List<MicroWorkSubmitDto> lastProcessByProductSeq = microWorkSubmitMapper.getProcessByProductSeq(productSeq, IsLastProcessEnum.YES.getCode(), null, SubmitStatusEnum.APPROVED.getCode());
            List<MicroWorkSubmitDto> firstProcessByProductSeq = microWorkSubmitMapper.getProcessByProductSeq(productSeq, null, IsFirstProcessEnum.YES.getCode(), SubmitStatusEnum.APPROVED.getCode());

            if (StringUtils.hasText(processSeq)) {
                // 排除当前工序后仍然不为null，则说明仍有其他工序是首序或者尾序
                if (!CollectionUtils.isEmpty(lastProcessByProductSeq)) {
                    lastProcessByProductSeq = lastProcessByProductSeq.stream().filter(obj -> !processSeq.equals(obj.getOperateProcessSeq())).collect(Collectors.toList());
                }
                if (!CollectionUtils.isEmpty(firstProcessByProductSeq)) {
                    firstProcessByProductSeq = firstProcessByProductSeq.stream().filter(obj -> !processSeq.equals(obj.getOperateProcessSeq())).collect(Collectors.toList());
                }
            }

            if (!CollectionUtils.isEmpty(lastProcessByProductSeq)) {
                MicroWorkSubmitDto microWorkSubmitDto = lastProcessByProductSeq.get(0);
                res.setLastProcessSeq(microWorkSubmitDto.getOperateProcessSeq());
                res.setLastProcessCode(microWorkSubmitDto.getOperateProcessCode());
                res.setLastProcessName(microWorkSubmitDto.getOperateProcessName());
                res.setIsOrNotMutex(CommonConstants.NO);
            }
            if (!CollectionUtils.isEmpty(firstProcessByProductSeq)) {
                MicroWorkSubmitDto microWorkSubmitDto = firstProcessByProductSeq.get(0);
                res.setFirstProcessSeq(microWorkSubmitDto.getOperateProcessSeq());
                res.setFirstProcessCode(microWorkSubmitDto.getOperateProcessCode());
                res.setFirstProcessName(microWorkSubmitDto.getOperateProcessName());
                res.setIsOrNotMutex(CommonConstants.NO);
            }
        }
        return res;
    }

    /**
     * 获取产品（产品 + 工序）的良品和不良品率
     *
     */
    @Override
    public List<MicroWorkSubmitDto> selectCompletedProductList(MicroWorkSubmitDto microWorkSubmitDto) {
        // 打印参数
        log.info("请求参数为:{}", JSONObject.toJSONString(microWorkSubmitDto));

        // 区分小程序应用
        Long submitType = supportUtil.getSubmitType();
        microWorkSubmitDto.setSubmitType(submitType);

        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectCompletedProductListByProductAddProcess(microWorkSubmitDto);
        if (CheckObjectUtils.isEmpty(microWorkSubmitDtoList)) {
            return microWorkSubmitDtoList;
        }
        // 设置良品和不良品率
        // 无质检数（check_pass + check_ng = 0）的行算不出比率 → 跳过该行
        // （原实现直接 divide(0) 抛 ArithmeticException: / by zero，接口 500 → 页面"系统异常"）
        List<MicroWorkSubmitDto> validList = new java.util.ArrayList<>();
        for (MicroWorkSubmitDto obj : microWorkSubmitDtoList) {
            BigDecimal totalNums = obj.getCheckNgNum().add(obj.getCheckPassNum());
            if (totalNums.signum() == 0) {
                log.warn("[良品率] {} {} 质检数为0，已跳过该行", obj.getProductName(), obj.getOperateProcessName());
                continue;
            }
            obj.setPassRate(obj.getCheckPassNum().divide(totalNums, 3, RoundingMode.DOWN));
            obj.setNgRate(obj.getCheckNgNum().divide(totalNums, 3, RoundingMode.DOWN));
            validList.add(obj);
        }
        // 按照良品率排序 (降序)
        validList.sort(Comparator.comparing(MicroWorkSubmitDto::getPassRate).reversed());
        return validList;
    }

    /**
     * 查询首次报工或审产信息
     *
     */
    @Override
    public FirstSubmitOrCheckInfoResult selectFirstSubmitOrCheckInfo(String operateType) {
        FirstSubmitOrCheckInfoResult result = new FirstSubmitOrCheckInfoResult();
        String userId = SecurityUtils.getUserId().toString();
        switch (OperateTypeEnum.getEnum(operateType)) {
            case SUBMIT:
                result = microWorkSubmitMapper.selectFirstSubmitInfo(userId);
                break;
            case CHECK:
                result = microWorkSubmitMapper.selectFirstCheckInfo(userId);
                break;
            default:
                break;
        }

        // 设置操作人昵称
        if (null != result && org.springframework.util.StringUtils.hasText(result.getOperator())) {
            String operatorUsername = result.getOperator();
            MicroUser microUser = microUserService.selectUserBaseInfo(operatorUsername, null);
            if (null != microUser && org.springframework.util.StringUtils.hasText(microUser.getNickName())) {
                result.setOperator(microUser.getNickName());
            }
        }

        return result;
    }

    /**
     * 撤销审核
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MicroWorkSubmit undoCheckedRecord(Long id, String type) {
        // 打印参数
        log.info("请求参数为:{}", id.toString());

        // 1. 根据id找到报工记录
        MicroWorkSubmit microWorkSubmit = microWorkSubmitMapper.selectMicroWorkSubmitExById(id);
        microWorkSubmit.setSubmitNickName(getNickName(microWorkSubmit.getSubmitUser()));
        if (CheckObjectUtils.isEmpty(microWorkSubmit)) {
            throw new CustomException("未查询到报工记录信息");
        }
        if (microWorkSubmit.getSubmitStatus().equals(SubmitStatusEnum.UN_APPROVE.getCode())) {
            throw new CustomException("该报工记录还未审核，无法撤销");
        }
        if (microWorkSubmit.getIsComplete().equals(IsCompleteEnum.YES.getCode())) { // add by zyh 完工报告模块需求 20230327 
            throw new CustomException("该报工记录已入库，无法撤销");
        }
        if (microWorkSubmit.getRepairNum().add(microWorkSubmit.getAbandonedNum()).signum() > 0) {
            throw new CustomException("该报工记录已返修，不允许撤销");
        }

        // 2. 变动的数量就是审核的数量，初始化要更新的报工记录
        MicroWorkSubmit recordForUpdate = new MicroWorkSubmit();
        BeanUtils.copyProperties(microWorkSubmit, recordForUpdate);

        // 3. 报工记录变动历史 -> （1）更新报工记录信息(初始化回去) (2) 插入变动历史记录
        MicroWorkSubmitHistory param = new MicroWorkSubmitHistory();
        param.setSubmitNo(microWorkSubmit.getSubmitNo());
        // 查询条件: 报工记录编号 + 创建时间倒序排序
        List<MicroWorkSubmitHistory> microWorkSubmitHistoryList = microWorkSubmitHistoryMapper.selectMicroWorkSubmitHistoryList(param);
        if (CollectionUtils.isEmpty(microWorkSubmitHistoryList)) {
            throw new CustomException("未查询到变动的历史记录");
        }
        // 过滤掉已审核的数据
        microWorkSubmitHistoryList = microWorkSubmitHistoryList.stream()
                .filter(obj -> !CommonConstants.SUBMIT_HISTORY_RECORDS_CHECK.equals(obj.getOperateNode()))
                .collect(Collectors.toList());
        // 初始的报工记录信息（报工人可能会编辑报工记录）
        // 兼容：历史里只有"审核"节点时（演示/导入数据、老数据没写报工快照）过滤后为空 ——
        // 不做字段回滚，只撤销"审核"这个动作本身（原实现直接 get(0) → IndexOutOfBoundsException → 500）
        MicroWorkSubmitHistory firstWorkSubmitHistory =
                microWorkSubmitHistoryList.isEmpty() ? null : microWorkSubmitHistoryList.get(0);
        if (firstWorkSubmitHistory == null) {
            log.warn("[撤销审核] 报工记录{}（submitNo={}）无报工/编辑历史快照，仅撤销审核状态，不回滚产品/工序/数量",
                    microWorkSubmit.getId(), microWorkSubmit.getSubmitNo());
        }
        // 设置要更新的报工记录信息
        recordForUpdate.setDataStatus(DataStatusEnum.UN_CHANGED.getCode());
        recordForUpdate.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());
        // 把审核时应该设置的字段改为null
        if (!microWorkSubmit.getCheckStatus().equals(CheckStatusEnum.FINISHED_INSPECTION.getCode())) {
            recordForUpdate.setCheckPassNum(null);
            recordForUpdate.setCheckNgNum(null);
        }
        // 设置产品和工序信息(编辑报工记录时可能会改变产品和工序)：有快照才回滚
        if (firstWorkSubmitHistory != null) {
            recordForUpdate.setProductSeq(firstWorkSubmitHistory.getProductSeq());
            recordForUpdate.setOperateProcessSeq(firstWorkSubmitHistory.getOperateProcessSeq());
            recordForUpdate.setPreProcessSeq(firstWorkSubmitHistory.getPreProcessSeq());
        }

        recordForUpdate.setCheckDate(null);
        recordForUpdate.setCheckUser(null);
        if (firstWorkSubmitHistory != null) {
            recordForUpdate.setIsLastProcess(firstWorkSubmitHistory.getIsLastProcess());
            recordForUpdate.setIsFirstProcess(firstWorkSubmitHistory.getIsFirstProcess());
            recordForUpdate.setRemark(firstWorkSubmitHistory.getRemark());
            recordForUpdate.setLastUpdDate(firstWorkSubmitHistory.getCreatedDate());
            recordForUpdate.setLastUpdBy(firstWorkSubmitHistory.getCreatedBy());
        }
        microWorkSubmitMapper.updateMicroWorkSubmit(recordForUpdate);

        // 4. 新增报工记录变动历史 （质检撤销不生成报工变动历史记录）
        if (type.equals(UndoCheckRecordTypeEnum.NORMAL_UNDO.getCode())) {
            MicroWorkSubmitHistory submitHistoryForInsert = new MicroWorkSubmitHistory();
            if (firstWorkSubmitHistory != null) {
                BeanUtils.copyProperties(firstWorkSubmitHistory, submitHistoryForInsert);
            } else {
                // 无快照：以当前记录为底稿生成撤销历史（保留审核轨迹）
                BeanUtils.copyProperties(microWorkSubmit, submitHistoryForInsert);
            }
            // 撤销时考虑返修完成数量（质检数为 null 时按 0 处理，避免 NPE）
            BigDecimal checkPassNum = microWorkSubmit.getCheckPassNum() == null ? BigDecimal.ZERO : microWorkSubmit.getCheckPassNum();
            BigDecimal checkNgNum = microWorkSubmit.getCheckNgNum() == null ? BigDecimal.ZERO : microWorkSubmit.getCheckNgNum();
            BigDecimal repairNum = microWorkSubmit.getRepairNum() == null ? BigDecimal.ZERO : microWorkSubmit.getRepairNum();
            submitHistoryForInsert.setPassNum(checkPassNum.add(repairNum));
            submitHistoryForInsert.setNgNum(checkNgNum.subtract(repairNum));
            submitHistoryForInsert.setOperateNode(CommonConstants.SUBMIT_HISTORY_RECORDS_UNDO_CHECK);
            submitHistoryForInsert.setCreatedDate(DateUtils.getNowDate());
            submitHistoryForInsert.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
            microWorkSubmitHistoryMapper.insertMicroWorkSubmitHistory(submitHistoryForInsert);
        }

        // 未撤销前的报工记录数据，该数据中存储了实际的审核数量
        return microWorkSubmit;
    }

    /**
     * 审核前的提示
     *
     */
    @Override
    public String tipsForCheck(Long[] ids) {
        log.info("请求入参为:{}", Arrays.toString(ids));
        if (CheckObjectUtils.isEmpty(ids)) {
            throw new CustomException("未选择报工记录");
        }
        // 查询到ids的数据
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectMicroWorkSubmitExByIds(ids, null, null);
        if (CollectionUtils.isEmpty(microWorkSubmitDtoList)) {
            throw new CustomException("未查询到请求入参的报工记录");
        }
        // 获取日产能等信息
        Map<String, Map<String, Object>> warningMetrics = supportUtil.getWarningMetrics();
        // 产品 + 工序 + 报工人 + 报工日期的报工数量（审核和未审核）
        Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition = microWorkSubmitMapper.obtainedTotalSubmitNumByCondition();

        // 判断异常
        microWorkSubmitDtoList = this.judgeSubmitRecordsException(microWorkSubmitDtoList, warningMetrics, allSubmitTotalNumByCondition);
        if (ids.length > 1) {
            int count = 0;
            for (MicroWorkSubmitDto obj : microWorkSubmitDtoList) {
                if (obj.getOverProductiveCapacityFlag().equals(OverProductiveCapacityFlagEnum.OVER.getCode())
                        || obj.getNegativeStockFlag().equals(NegativeStockFlagEnum.NEGATIVE_STOCK.getCode())
                        || obj.getLowPassRateFlag().equals(LowPassRateFlagEnum.LOW.getCode())) {
                    count += 1;
                }
            }
            if (count != 0) {
                return "审核包含" + ids.length + "条记工明细，有" + count + "条数据可能存在异常，是否确认审核?";
            } else {
                return "是否确认审核?";
            }
        } else {
            MicroWorkSubmitDto microWorkSubmitDtoTemp = microWorkSubmitDtoList.get(0);
            if (microWorkSubmitDtoTemp.getNegativeStockFlag().equals(NegativeStockFlagEnum.NEGATIVE_STOCK.getCode())) {
                return "审核后可能会造成" + microWorkSubmitDtoTemp.getPreProcessName() + "工序负库存，是否确认审核?";
            }
            if (microWorkSubmitDtoTemp.getLowPassRateFlag().equals(LowPassRateFlagEnum.LOW.getCode())) {
                return "记工数量低于工序平均良品率（" + microWorkSubmitDtoTemp.getAvgPassRateByDay() + "）, 是否确认审核?";
            }
            if (microWorkSubmitDtoTemp.getOverProductiveCapacityFlag().equals(OverProductiveCapacityFlagEnum.OVER.getCode())) {
                return "记工数量已超过工序日均产能（" + microWorkSubmitDtoTemp.getAvgProductionCapacityByDay() + "）, 是否确认审核?";
            }
            return "是否确认审核?";
        }
    }

    /**
     * 编辑并审核接口调用前的报工记录异常判断方法
     *
     */
    @Override
    public SubmitRecordException tipsForEditAndCheck(MicroWorkSubmitDto microWorkSubmitDto) {
        log.info("请求参数为:{}", JSONObject.toJSONString(microWorkSubmitDto));

        // 前端如果修改了数量，实际放在了checkPassNum和checkNgNum字段上
        microWorkSubmitDto.setPassNum(microWorkSubmitDto.getCheckPassNum());
        microWorkSubmitDto.setNgNum(microWorkSubmitDto.getCheckNgNum());

        // 构造入参
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = Lists.newArrayList();
        microWorkSubmitDtoList.add(microWorkSubmitDto);

        // 获取产品 + 工序的良品率和产能
        Map<String, Map<String, Object>> warningMetrics = supportUtil.getWarningMetrics();
        // 产品 + 工序 + 报工人 + 报工日期的报工数量（审核和未审核）
        Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition = microWorkSubmitMapper.obtainedTotalSubmitNumByCondition();

        // 异常判断
        this.judgeSubmitRecordsException(microWorkSubmitDtoList, warningMetrics, allSubmitTotalNumByCondition);

        // 返回值
        SubmitRecordException res = new SubmitRecordException();
        MicroWorkSubmitDto microWorkSubmitDtoByException = microWorkSubmitDtoList.get(0);
        BeanUtils.copyProperties(microWorkSubmitDtoByException, res);

        return res;
    }

    /**
     * 审核驳回
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectSubmitRecord(Long[] ids) {
        log.info("请求参数为:{}", Arrays.toString(ids));
        if (CheckObjectUtils.isEmpty(ids)) {
            throw new CustomException("未选择要驳回的报工记录");
        }
        // 根据ids获取报工记录详情，判断报工记录是否异常
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectMicroWorkSubmitByIds(ids);
        if (CollectionUtils.isEmpty(microWorkSubmitList)) {
            throw new CustomException("未查询到报工记录信息");
        }
        microWorkSubmitList.forEach(obj -> {
            if (!obj.getSubmitStatus().equals(SubmitStatusEnum.UN_APPROVE.getCode())) {
                throw new CustomException("id为" + obj.getId() + "的报工记录并不是待审核状态, 不能进行驳回");
            }
        });

        // 更新报工记录的状态信息（驳回状态）, 生成报工变动历史记录
        List<MicroWorkSubmitHistory> insertForHistoryRecords = new ArrayList<>();
        microWorkSubmitList.forEach(obj -> {
            // 已送检,待检的报工记录可以驳回，但是在驳回后设置状态为待送检
            // 驳回时不会更新质检的状态
//            if (obj.getCheckStatus().equals(CheckStatusEnum.ALREADY_SUBMIT_FOR_INSPECTION.getCode())
//                    || obj.getCheckStatus().equals(CheckStatusEnum.FINISHED_INSPECTION.getCode())) {
//                obj.setCheckStatus(CheckStatusEnum.NOT_SUBMIT_FOR_INSPECTION.getCode());
//            }
            obj.setSubmitStatus(SubmitStatusEnum.REJECT.getCode());
            obj.setLastUpdBy(SecurityUtils.getUserId().toString());
            obj.setLastUpdDate(DateUtils.getNowDate());

            MicroWorkSubmitHistory insertHistoryRecord = new MicroWorkSubmitHistory();
            BeanUtils.copyProperties(obj, insertHistoryRecord);
            insertHistoryRecord.setOperateNode(CommonConstants.SUBMIT_HISTORY_RECORDS_REJECT);
            insertHistoryRecord.setCreatedDate(DateUtils.getNowDate());
            insertHistoryRecord.setCreatedBy(SecurityUtils.getUserId().toString());
            insertForHistoryRecords.add(insertHistoryRecord);
        });

        // 批量插入、更新 
        this.batchInsertOrUpdate(microWorkSubmitList, insertForHistoryRecords);

        return true;
    }

    /**
     * 判断记工记录是否存在预警标识
     *
     */
    @Override
    public Boolean isHaveWarnExceptionForWorkSubmits(SubmitRecordQueryParam submitRecordQueryParam) {
        submitRecordQueryParam.setSubmitType(supportUtil.getSubmitType());
        List<MicroWorkSubmit> microWorkSubmits = microWorkSubmitMapper.selectSubmitRecordByUser(submitRecordQueryParam);
        if (CollectionUtils.isEmpty(microWorkSubmits)) {
            return false;
        }

        // 获取产品 + 工序的良品率和产能
        Map<String, Map<String, Object>> warningMetrics = supportUtil.getWarningMetrics();
        // 产品 + 工序 + 报工人 + 报工日期的报工数量（审核和未审核）
        Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition = microWorkSubmitMapper.obtainedTotalSubmitNumByCondition();

        // 拷贝一份对象
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = BeanUtil.copyToList(microWorkSubmits, MicroWorkSubmitDto.class);
        // 异常判断
        microWorkSubmitDtoList = this.judgeSubmitRecordsException(microWorkSubmitDtoList, warningMetrics, allSubmitTotalNumByCondition);
        if (!CollectionUtils.isEmpty(microWorkSubmitDtoList)) {
            long count = microWorkSubmitDtoList.stream()
                    .filter(e -> (Integer.parseInt(e.getNegativeStockFlag()) + Integer.parseInt(e.getLowPassRateFlag())
                            + Integer.parseInt(e.getOverProductiveCapacityFlag()) < 3))
                    .count();
            return count > 0L;
        }

        return false;
    }

    /**
     * 工易派 - 产品维度生产报工汇总分析
     *
     */
    @Override
    public List<ProductiveSubmitSummaryInfoByProduct> selectProductiveSubmitSummaryAnalysisByProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        // 存储结果
        List<ProductiveSubmitSummaryInfoByProduct> result = new ArrayList<>();
        // 分页先查询汇总的报工记录
        List<ProductiveSubmitAnalysisByProduct> productiveSubmitAnalysisByProductList  = microWorkSubmitMapper.selectProductiveSubmitSummaryAnalysisByProduct(productionQualityAnalysisParam);
        if (CollectionUtil.isNotEmpty(productiveSubmitAnalysisByProductList)) {
            // 根据产品查询不同状态下的报工数据
            String productSeqList = productiveSubmitAnalysisByProductList.stream().map(ProductiveSubmitAnalysisByProduct::getProductSeq)
                    .collect(Collectors.joining(","));
            productionQualityAnalysisParam.setProductNameOrCode(null);
            productionQualityAnalysisParam.setProductSeqList(productSeqList);
            List<DifferentStatusSubmitAnalysisByProduct> differentStatusSubmitAnalysisByProducts = microWorkSubmitMapper.selectProductiveSubmitAnalysisByProductInDifferentStatus(productionQualityAnalysisParam);
            Map<String, List<DifferentStatusSubmitAnalysisByProduct>> map = differentStatusSubmitAnalysisByProducts.stream().collect(Collectors.groupingBy(DifferentStatusSubmitAnalysisByProduct::getProductSeq));

            for (ProductiveSubmitAnalysisByProduct productiveSubmitAnalysisByProduct : productiveSubmitAnalysisByProductList) {
                // 存储结果
                ProductiveSubmitSummaryInfoByProduct temp = new ProductiveSubmitSummaryInfoByProduct();
                List<DifferentStatusSubmitAnalysisByProduct> differentStatusSubmitAnalysisByProductList = map.get(productiveSubmitAnalysisByProduct.getProductSeq());

                // 按照未审核和已审核进行区分, list中最多有两种情况
                for (DifferentStatusSubmitAnalysisByProduct differentStatusSubmitAnalysisByProduct : differentStatusSubmitAnalysisByProductList) {
                    if (differentStatusSubmitAnalysisByProduct.getSubmitStatus().equals(SubmitStatusEnum.UN_APPROVE.getCode())) {
                        temp.setPassNum(differentStatusSubmitAnalysisByProduct.getPassNum());
                        temp.setNgNum(differentStatusSubmitAnalysisByProduct.getNgNum());
                    } else {
                        temp.setCheckPassNum(differentStatusSubmitAnalysisByProduct.getPassNum());
                        temp.setCheckNgNum(differentStatusSubmitAnalysisByProduct.getNgNum());
                    }
                }
                temp.setProductSeq(productiveSubmitAnalysisByProduct.getProductSeq());
                temp.setProductCode(productiveSubmitAnalysisByProduct.getProductCode());
                temp.setProductUnit(productiveSubmitAnalysisByProduct.getProductUnit());
                temp.setProductName(productiveSubmitAnalysisByProduct.getProductName());
                temp.setTotalNum(temp.getPassNum().add(temp.getNgNum()).add(temp.getCheckNgNum()).add(temp.getCheckPassNum()));
                result.add(temp);
            }
        }
        return result;
    }

    /**
     * 工易派 - 基于产品的工序维度生产报工汇总分析
     *
     */
    @Override
    public List<ProductiveSubmitSummaryInfoByProcessBaseProduct> selectProductiveSubmitSummaryAnalysisByProcessBaseProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        // 存储结果
        List<ProductiveSubmitSummaryInfoByProcessBaseProduct> result = new ArrayList<>();
        // 查询数据
        List<ProductiveSubmitAnalysisByProcessBaseProduct> productiveSubmitAnalysisByProcessBaseProductList = microWorkSubmitMapper.selectProductiveSubmitAnalysisByProcessBaseProduct(productionQualityAnalysisParam);
        if (CollectionUtil.isNotEmpty(productiveSubmitAnalysisByProcessBaseProductList)) {
            Map<String, List<ProductiveSubmitAnalysisByProcessBaseProduct>> map = productiveSubmitAnalysisByProcessBaseProductList
                    .stream().collect(Collectors.groupingBy(ProductiveSubmitAnalysisByProcessBaseProduct::getProcessSeq));
            for (Map.Entry<String, List<ProductiveSubmitAnalysisByProcessBaseProduct>> entry : map.entrySet()) {
                // 存储结果
                ProductiveSubmitSummaryInfoByProcessBaseProduct temp = new ProductiveSubmitSummaryInfoByProcessBaseProduct();
                for (ProductiveSubmitAnalysisByProcessBaseProduct productiveSubmitAnalysisByProcessBaseProduct : entry.getValue()) {
                    if (productiveSubmitAnalysisByProcessBaseProduct.getSubmitStatus().equals(SubmitStatusEnum.UN_APPROVE.getCode())) {
                        temp.setPassNum(productiveSubmitAnalysisByProcessBaseProduct.getPassNum());
                        temp.setNgNum(productiveSubmitAnalysisByProcessBaseProduct.getNgNum());
                    } else {
                        temp.setCheckPassNum(productiveSubmitAnalysisByProcessBaseProduct.getPassNum());
                        temp.setCheckNgNum(productiveSubmitAnalysisByProcessBaseProduct.getNgNum());
                    }
                }
                // 传递产品和工序相关字段
                ProductiveSubmitAnalysisByProcessBaseProduct copyTemp = entry.getValue().get(0);
                temp.setProductSeq(copyTemp.getProductSeq());
                temp.setProductCode(copyTemp.getProductCode());
                temp.setProductName(copyTemp.getProductName());
                temp.setProductUnit(copyTemp.getProductUnit());
                temp.setProcessCode(copyTemp.getProcessCode());
                temp.setProcessName(copyTemp.getProcessName());
                temp.setProcessSeq(copyTemp.getProcessSeq());
                temp.setTotalNum(temp.getPassNum().add(temp.getNgNum()).add(temp.getCheckPassNum()).add(temp.getCheckNgNum()));
                result.add(temp);
            }

        }
        return result;
    }

    /**
     * 工易派 - 员工维度生产报工数据
     *
     */
    @Override
    public List<ProductiveSubmitAnalysisByUser> selectProductiveSubmitAnalysisByUser(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        return microWorkSubmitMapper.selectProductiveSubmitAnalysisByUser(productionQualityAnalysisParam);
    }

    /**
     * 工易派 - 员工维度下各产品的报工数据
     *
     */
    @Override
    public List<ProductSubmitSummaryInfoBaseUser> selectProductiveSubmitSummaryInfoBaseUser(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        // 存储结果
        List<ProductSubmitSummaryInfoBaseUser> result = new ArrayList<>();
        // 查询数据
        List<ProductiveSubmitAnalysisByProductAndProcessBaseUser> dataList = microWorkSubmitMapper.selectProductiveSubmitAnalysisByProductAndProcessBaseUser(productionQualityAnalysisParam);
        if (CollectionUtil.isNotEmpty(dataList)) {
            // 以产品进行划分
            Map<String, List<ProductiveSubmitAnalysisByProductAndProcessBaseUser>> map = dataList.stream().collect(Collectors.groupingBy(ProductiveSubmitAnalysisByProductAndProcessBaseUser::getProductSeq));
            for (Map.Entry<String, List<ProductiveSubmitAnalysisByProductAndProcessBaseUser>> entry : map.entrySet()) {
                ProductSubmitSummaryInfoBaseUser productTemp = new ProductSubmitSummaryInfoBaseUser();
                List<ProcessSummaryInfoByProductBaseUser> processTempList = new ArrayList<>();
                for (ProductiveSubmitAnalysisByProductAndProcessBaseUser processInfo : entry.getValue()) {
                    ProcessSummaryInfoByProductBaseUser processTemp = new ProcessSummaryInfoByProductBaseUser();
                    processTemp.setProcessCode(processInfo.getProcessCode());
                    processTemp.setProcessSeq(processInfo.getProcessSeq());
                    processTemp.setProcessName(processInfo.getProcessName());
                    processTemp.setPassNum(processInfo.getPassNum());
                    processTemp.setNgNum(processInfo.getNgNum());
                    processTempList.add(processTemp);
                }
                // 设置产品信息
                productTemp.setProductSeq(entry.getKey());
                productTemp.setProductCode(entry.getValue().get(0).getProductCode());
                productTemp.setProductName(entry.getValue().get(0).getProductName());
                productTemp.setProductUnit(entry.getValue().get(0).getProductUnit());
                productTemp.setProcessSummaryInfoByProductBaseUserList(processTempList);
                BigDecimal passNum = processTempList.stream().map(ProcessSummaryInfoByProductBaseUser::getPassNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal ngNum = processTempList.stream().map(ProcessSummaryInfoByProductBaseUser::getNgNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                productTemp.setTotalNum(passNum.add(ngNum));
                result.add(productTemp);
            }
        }
        return result;
    }

    /**
     * 校验报工日
     *
     */
    private Long checkSubmitDay(Date submitDay) {
        // 校验报工日
        if (CheckObjectUtils.isEmpty(submitDay)) {
            throw new CustomException("报工时报工日期不能为空");
        }
        LocalDate currentDate = LocalDate.now();
        LocalDate localDate = submitDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (currentDate.isBefore(localDate)) {
            throw new CustomException("报工日期不能大于当前日期");
        }
        // 日期加上7天
        if (currentDate.isAfter(localDate.plusDays(DAYS_LOOKBACK))) {
            throw new CustomException("只能选择以当前时间开始7天之内对日期进行报工");
        }

        // 判读是否为补录
        if (currentDate.isEqual(localDate)) {
            return ExpiredRecordFlagEnum.NO.getCode();
        } else {
            return ExpiredRecordFlagEnum.YES.getCode();
        }
    }

    /**
     * 批量更新相关记录 (报工、报工历史记录)
     *
     */
    private void batchInsertOrUpdate(List<MicroWorkSubmit> updateListForWorkSubmit, List<MicroWorkSubmitHistory> insertListForSubmitHistory) {
        if (CheckObjectUtils.isNotEmpty(updateListForWorkSubmit)) {
            microWorkSubmitMapper.updateMicroWorkSubmitBatch(updateListForWorkSubmit);
        }
        if (CheckObjectUtils.isNotEmpty(insertListForSubmitHistory)) {
            microWorkSubmitHistoryMapper.insertMicroWorkSubmitHistoryBatch(insertListForSubmitHistory);
        }
    }

    /**
     * 判断报工记录是否有新的产品, list中存储要插入的新的产品数据
     *
     */
    private String createNewProductForSubmit(MicroWorkSubmit microWorkSubmit, List<MicroProduct> insertMicroProducts) {

        String productSeq;
        if (StringUtils.isEmpty(microWorkSubmit.getProductSeq())) {
            // 生成新的产品
            MicroProduct newProduct = microProductService.createNewProduct(microWorkSubmit.getProductName());
            productSeq = newProduct.getProductSeq();
            microWorkSubmit.setProductSeq(productSeq);
            microWorkSubmit.setProductCode(productSeq);
            if (CheckObjectUtils.isNotEmpty(insertMicroProducts)) {
                insertMicroProducts.add(newProduct);
            }
        } else {
            productSeq = microWorkSubmit.getProductSeq();
        }

        return productSeq;
    }

    /**
     * 判断报工记录是否有新的工序, list中存储要插入的新工序数据
     *
     */
    private MicroWorkSubmit createNewProcessForSubmit(MicroWorkSubmit microWorkSubmit, List<MicroProcessCommon> insertMicroProcesses) {
        String preProcessSeq = microWorkSubmit.getPreProcessSeq();
        String operateProcessSeq = microWorkSubmit.getOperateProcessSeq();

        if (StringUtils.isEmpty(preProcessSeq) && StringUtils.hasText(microWorkSubmit.getPreProcessName())) {
            // 判断前工序的名称是否已经生成
            // 存放前工序的结果
            MicroProcessCommon newPreProcess;
            MicroProcessCommon param = new MicroProcessCommon();
            param.setProcessName(microWorkSubmit.getPreProcessName());
            List<MicroProcessCommon> microProcessCommons = microProcessCommonService.selectMicroProcessCommonList(param);
            if (CollectionUtils.isEmpty(microProcessCommons)) {
                // 生成新的前工序
                newPreProcess = microProcessCommonService.createNewProcess(microWorkSubmit.getPreProcessName());
            } else {
                // 取已经生成的工序信息
                newPreProcess = microProcessCommons.get(0);
            }
            preProcessSeq = newPreProcess.getProcessSeq();
            microWorkSubmit.setPreProcessSeq(newPreProcess.getProcessSeq());
            microWorkSubmit.setPreProcessCode(newPreProcess.getProcessCode());
            // 编辑并审核的接口需要该部分
            if (CheckObjectUtils.isNotEmpty(insertMicroProcesses)) {
                insertMicroProcesses.add(newPreProcess);
            }
        }

        if (StringUtils.isEmpty(operateProcessSeq)) {
            // 判断当前工序名称是否已经生成
            MicroProcessCommon newOperateProcess;
            MicroProcessCommon param = new MicroProcessCommon();
            param.setProcessName(microWorkSubmit.getOperateProcessName());
            List<MicroProcessCommon> microProcessCommons = microProcessCommonService.selectMicroProcessCommonList(param);
            if (CollectionUtils.isEmpty(microProcessCommons)) {
                newOperateProcess = microProcessCommonService.createNewProcess(microWorkSubmit.getOperateProcessName());
            } else {
                newOperateProcess = microProcessCommons.get(0);
            }
            operateProcessSeq = newOperateProcess.getProcessSeq();
            microWorkSubmit.setOperateProcessSeq(newOperateProcess.getProcessSeq());
            microWorkSubmit.setOperateProcessCode(newOperateProcess.getProcessCode());
            // 编辑并审核的接口需要该部分
            if (CheckObjectUtils.isNotEmpty(insertMicroProcesses)) {
                insertMicroProcesses.add(newOperateProcess);
            }
        }

        MicroWorkSubmit res = new MicroWorkSubmit();
        res.setPreProcessSeq(preProcessSeq);
        res.setOperateProcessSeq(operateProcessSeq);
        return res;
    }

    /**
     * 库存变动 -> 库存变动历史记录
     *
     * (库存新增或者更新)
     *
     * @param microWorkSubmit          一条报工记录信息
     * @param insertListForStorageHistory 存放库存变动历史记录
     * @param type                        0:报工 - 增加   1: 冲销 - 减少
     */
    @Deprecated
    private void changeStorage(MicroWorkSubmit microWorkSubmit, List<MicroProcessStorageHistory> insertListForStorageHistory, String type) { 
        // 如果前工序存在（产品 + 前工序），进行扣减
        if (StringUtils.hasText(microWorkSubmit.getPreProcessSeq())) {
            // preProcessSeq可能是多个
            List<String> preProcessSeqList = Arrays.stream(microWorkSubmit.getPreProcessSeq().split(",")).collect(Collectors.toList());
            List<String> preProcessCodeList = Arrays.stream(microWorkSubmit.getPreProcessCode().split(",")).collect(Collectors.toList());
            List<String> preProcessNameList = Arrays.stream(microWorkSubmit.getPreProcessName().split(",")).collect(Collectors.toList());

            for (int i = 0; i < preProcessSeqList.size(); i++) {
                MicroProcessStorageDto preProcessParam = new MicroProcessStorageDto();
                preProcessParam.setProcessSeq(preProcessSeqList.get(i));
                preProcessParam.setProductSeq(microWorkSubmit.getProductSeq());
                MicroProcessStorage preProcessStorage = processStorageService.selectMicroProcessStorageByInfo(preProcessParam);

                // 生成前工序库存变动历史记录
                MicroProcessStorageHistory preProcessStorageHistory = new MicroProcessStorageHistory();
                preProcessStorageHistory.setProductSeq(microWorkSubmit.getProductSeq());
                preProcessStorageHistory.setProductCode(microWorkSubmit.getProductCode());
                preProcessStorageHistory.setProductName(microWorkSubmit.getProductName());
                preProcessStorageHistory.setProcessSeq(preProcessSeqList.get(i));
                preProcessStorageHistory.setProcessCode(preProcessCodeList.get(i));
                preProcessStorageHistory.setProcessName(preProcessNameList.get(i));
                // 判断库存变动的原因类型
                if (CommonConstants.STORAGE_CHANGE_TYPE_SUBMIT.equals(type)) {
                    preProcessStorageHistory.setOperateNode(CommonConstants.STORAGE_PROCESS_FLOW_MODIFY);
                    // 库存变动历史记录创建人设置的是报工人
                    preProcessStorageHistory.setCreatedBy(microWorkSubmit.getSubmitUser());
                    // 设置工序流转的详细变动原因
                    preProcessStorageHistory.setRemark("工序" + microWorkSubmit.getOperateProcessName() + "扣减");
                } else {
                    preProcessStorageHistory.setOperateNode(CommonConstants.STORAGE_UNDO_FALLBACK);
                    // 审核撤销的操作人是当前人
                    preProcessStorageHistory.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
                    // 设置审核撤销的详细变动原因
                    if (ExpiredRecordFlagEnum.YES.getCode().equals(microWorkSubmit.getExpiredRecordFlag())) {
                        preProcessStorageHistory.setRemark("撤销" +
                                DateUtils.parseDateToStr("MM-dd", microWorkSubmit.getSubmitDay()) +
                                microWorkSubmit.getSubmitNickName() + "的记工");
                    } else {
                        preProcessStorageHistory.setRemark("撤销" +
                                DateUtils.parseDateToStr("MM-dd HH:mm:ss", microWorkSubmit.getCreatedDate()) +
                                microWorkSubmit.getSubmitNickName() + "的记工");
                    }
                }
                preProcessStorageHistory.setCreatedDate(DateUtils.getNowDate());
                preProcessStorageHistory.setTenantCode(microWorkSubmit.getTenantCode());

                // 如果 前工序 + 产品 无对应的库存记录，则新增
                // 撤销审核的数据正常不会出现库存不存在的情况  
                if (CheckObjectUtils.isEmpty(preProcessStorage)) {
                    MicroProcessStorage preProcessStorageInsert = new MicroProcessStorage();
                    preProcessStorageInsert.setProductSeq(microWorkSubmit.getProductSeq());
                    preProcessStorageInsert.setProcessSeq(preProcessSeqList.get(i));
                    // 口径：工序间流转的只有良品（不良通过返修/报废离开本工序的 ng_num），前工序"欠账"只记良品
                    // 原来记的是 -(已审不良 + 报工良品)，既多扣了不良、又与 MicroProcessStorageServiceImpl 的口径不一致
                    preProcessStorageInsert.setNgNum(BigDecimal.ZERO);
                    preProcessStorageInsert.setPassNum(microWorkSubmit.getCheckPassNum().negate());
                    preProcessStorageInsert.setCreatedDate(DateUtils.getNowDate());
                    preProcessStorageInsert.setTenantCode(microWorkSubmit.getTenantCode());
                    preProcessStorageInsert.setCreatedBy(microWorkSubmit.getSubmitUser());
                    // 直接插入
                    microProcessStorageMapper.insertMicroProcessStorage(preProcessStorageInsert);

                    preProcessStorageHistory.setPassFromNum(BigDecimal.ZERO);
                    preProcessStorageHistory.setPassToNum(preProcessStorageInsert.getPassNum());
                    preProcessStorageHistory.setNgFromNum(BigDecimal.ZERO);
                    preProcessStorageHistory.setNgToNum(BigDecimal.ZERO);
                } else {
                    MicroProcessStorage preProcessStorageUpdate = new MicroProcessStorage();
                    preProcessStorageUpdate.setId(preProcessStorage.getId());
                    // 加减相关库存并更新（口径同上：只对良品加减，不含不良）
                    if (CommonConstants.STORAGE_CHANGE_TYPE_SUBMIT.equals(type)) {
                        // 只扣减良品的数量
                        preProcessStorageUpdate.setPassNum(preProcessStorage.getPassNum().subtract(microWorkSubmit.getCheckPassNum()));
                    } else {
                        // 只加良品的数量
                        preProcessStorageUpdate.setPassNum(preProcessStorage.getPassNum().add(microWorkSubmit.getCheckPassNum()));
                    }
                    preProcessStorageUpdate.setLastUpdDate(DateUtils.getNowDate());
                    preProcessStorageUpdate.setLastUpdBy(microWorkSubmit.getSubmitUser());
                    // 直接更新
                    microProcessStorageMapper.updateMicroProcessStorage(preProcessStorageUpdate);

                    preProcessStorageHistory.setPassFromNum(preProcessStorage.getPassNum());
                    preProcessStorageHistory.setPassToNum(preProcessStorageUpdate.getPassNum());
                    // 不良品前后无变化
                    preProcessStorageHistory.setNgFromNum(preProcessStorage.getNgNum());
                    preProcessStorageHistory.setNgToNum(preProcessStorage.getNgNum());
                }
                insertListForStorageHistory.add(preProcessStorageHistory);
            }
        }

        // 产品 + 当前工序，进行增加
        MicroProcessStorageDto currentProcessParam = new MicroProcessStorageDto();
        currentProcessParam.setProcessSeq(microWorkSubmit.getOperateProcessSeq());
        currentProcessParam.setProductSeq(microWorkSubmit.getProductSeq());
        MicroProcessStorage currentProcessStorage = processStorageService.selectMicroProcessStorageByInfo(currentProcessParam);

        // 生成当前工序库存变动历史记录
        MicroProcessStorageHistory currentProcessStorageHistory = new MicroProcessStorageHistory();
        currentProcessStorageHistory.setProductSeq(microWorkSubmit.getProductSeq());
        currentProcessStorageHistory.setProductCode(microWorkSubmit.getProductCode());
        currentProcessStorageHistory.setProductName(microWorkSubmit.getProductName());
        currentProcessStorageHistory.setProcessSeq(microWorkSubmit.getOperateProcessSeq());
        currentProcessStorageHistory.setProcessCode(microWorkSubmit.getOperateProcessCode());
        currentProcessStorageHistory.setProcessName(microWorkSubmit.getOperateProcessName());
        // 判断库存变动的原因类型
        if (CommonConstants.STORAGE_CHANGE_TYPE_SUBMIT.equals(type)) {
            currentProcessStorageHistory.setOperateNode(CommonConstants.STORAGE_SUBMIT_INBOUND);
            // 库存变动历史记录创建人设置的是报工人
            currentProcessStorageHistory.setCreatedBy(microWorkSubmit.getSubmitUser());
        } else {
            currentProcessStorageHistory.setOperateNode(CommonConstants.STORAGE_UNDO_FALLBACK);
            // 审核撤销的操作是当前人
            currentProcessStorageHistory.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
            // 设置审核撤销的详细变动原因
            if (ExpiredRecordFlagEnum.YES.getCode().equals(microWorkSubmit.getExpiredRecordFlag())) {
                currentProcessStorageHistory.setRemark("撤销" +
                        DateUtils.parseDateToStr("MM-dd", microWorkSubmit.getSubmitDay()) +
                        microWorkSubmit.getSubmitNickName() + "的记工");
            } else {
                currentProcessStorageHistory.setRemark("撤销" +
                        DateUtils.parseDateToStr("MM-dd HH:mm:ss", microWorkSubmit.getCreatedDate()) +
                        microWorkSubmit.getSubmitNickName() + "的记工");
            }
        }
        currentProcessStorageHistory.setCreatedDate(DateUtils.getNowDate());
        currentProcessStorageHistory.setTenantCode(microWorkSubmit.getTenantCode());

        // 撤销审核的数据正常不会出现库存不存在的情况  
        if (CheckObjectUtils.isEmpty(currentProcessStorage)) {
            MicroProcessStorage currentProcessStorageInsert = new MicroProcessStorage();
            currentProcessStorageInsert.setProductSeq(microWorkSubmit.getProductSeq());
            currentProcessStorageInsert.setProcessSeq(microWorkSubmit.getOperateProcessSeq());
            currentProcessStorageInsert.setNgNum(microWorkSubmit.getCheckNgNum());
            currentProcessStorageInsert.setPassNum(microWorkSubmit.getCheckPassNum());
            currentProcessStorageInsert.setCreatedBy(microWorkSubmit.getSubmitUser());
            currentProcessStorageInsert.setCreatedDate(DateUtils.getNowDate());
            currentProcessStorageInsert.setTenantCode(microWorkSubmit.getTenantCode());
            microProcessStorageMapper.insertMicroProcessStorage(currentProcessStorageInsert);

            currentProcessStorageHistory.setPassFromNum(BigDecimal.ZERO);
            currentProcessStorageHistory.setPassToNum(currentProcessStorageInsert.getPassNum());
            currentProcessStorageHistory.setNgFromNum(BigDecimal.ZERO);
            currentProcessStorageHistory.setNgToNum(currentProcessStorageInsert.getNgNum());
        } else {
            // 增加相关库存并更新
            MicroProcessStorage currentProcessStorageUpdate = new MicroProcessStorage();
            currentProcessStorageUpdate.setId(currentProcessStorage.getId());
            // 加减库存数量
            if (CommonConstants.STORAGE_CHANGE_TYPE_SUBMIT.equals(type)) {
                currentProcessStorageUpdate.setPassNum(currentProcessStorage.getPassNum().add(microWorkSubmit.getCheckPassNum()));
                currentProcessStorageUpdate.setNgNum(currentProcessStorage.getNgNum().add(microWorkSubmit.getCheckNgNum()));
                // 更新人是报工人
                currentProcessStorageUpdate.setLastUpdBy(microWorkSubmit.getSubmitUser());
            } else {
                currentProcessStorageUpdate.setPassNum(currentProcessStorage.getPassNum().subtract(microWorkSubmit.getCheckPassNum()));
                currentProcessStorageUpdate.setNgNum(currentProcessStorage.getNgNum().subtract(microWorkSubmit.getCheckNgNum()));
                // 更新人是当前操作人（审产员）
                currentProcessStorageUpdate.setLastUpdBy(String.valueOf(SecurityUtils.getUserId()));
            }
            currentProcessStorageUpdate.setLastUpdDate(DateUtils.getNowDate());
            microProcessStorageMapper.updateMicroProcessStorage(currentProcessStorageUpdate);

            currentProcessStorageHistory.setPassFromNum(currentProcessStorage.getPassNum());
            currentProcessStorageHistory.setPassToNum(currentProcessStorageUpdate.getPassNum());
            currentProcessStorageHistory.setNgFromNum(currentProcessStorage.getNgNum());
            currentProcessStorageHistory.setNgToNum(currentProcessStorageUpdate.getNgNum());
        }
        insertListForStorageHistory.add(currentProcessStorageHistory);
    }

    /**
     * 验证报工记录信息是否发生变化
     *
     * @param microWorkSubmit  提交的报工记录信息
     * @param microWorkSubmitTemp 数据库中存储的报工记录信息
     */
    public boolean validSubmitRecordChange(MicroWorkSubmit microWorkSubmit,  MicroWorkSubmit microWorkSubmitTemp) {
        if (microWorkSubmit.getCheckPassNum().compareTo(microWorkSubmitTemp.getPassNum()) != 0) {
            return true;
        }
        if (microWorkSubmit.getCheckNgNum().compareTo(microWorkSubmitTemp.getNgNum()) != 0) {
            return true;
        }
        // 图片信息
        if (com.cosmo.hhim.common.core.utils.StringUtils.isEmpty(microWorkSubmit.getSubmitPictures()) && com.cosmo.hhim.common.core.utils.StringUtils.isNotEmpty(microWorkSubmitTemp.getSubmitPictures())) {
            return true;
        }
        if (com.cosmo.hhim.common.core.utils.StringUtils.isNotEmpty(microWorkSubmit.getSubmitPictures()) && com.cosmo.hhim.common.core.utils.StringUtils.isEmpty(microWorkSubmitTemp.getSubmitPictures())) {
            return true;
        }
        if (!com.cosmo.hhim.common.core.utils.StringUtils.isNotEmpty(microWorkSubmit.getSubmitPictures()) && com.cosmo.hhim.common.core.utils.StringUtils.isNotEmpty(microWorkSubmitTemp.getSubmitPictures()) && !microWorkSubmit.getSubmitPictures().equals(microWorkSubmitTemp.getSubmitPictures())) {
            return true;
        }
        // 备注信息
        if (com.cosmo.hhim.common.core.utils.StringUtils.isEmpty(microWorkSubmit.getRemark()) && com.cosmo.hhim.common.core.utils.StringUtils.isNotEmpty(microWorkSubmitTemp.getRemark())) {
            return true;
        }
        if (com.cosmo.hhim.common.core.utils.StringUtils.isNotEmpty(microWorkSubmit.getRemark()) && com.cosmo.hhim.common.core.utils.StringUtils.isEmpty(microWorkSubmitTemp.getRemark())) {
            return true;
        }
        if (!com.cosmo.hhim.common.core.utils.StringUtils.isNotEmpty(microWorkSubmit.getRemark()) || !com.cosmo.hhim.common.core.utils.StringUtils.isNotEmpty(microWorkSubmitTemp.getRemark()) || microWorkSubmit.getRemark().equals(microWorkSubmitTemp.getRemark())) {
            return false;
        }
        return true;
    }

    /**
     * 合并两个list （重复天数的则相加）
     *
     * @param productCountForSubmitForWaitCheck 时间范围内未审核的报工数量
     * @param productCountForSubmitForCheck     时间范围内审核的报工数量
     */
    @Deprecated
    public List<MicroWorkSubmitProductCount> calculateProductCountsByDay(List<MicroWorkSubmitProductCount> productCountForSubmitForWaitCheck, List<MicroWorkSubmitProductCount> productCountForSubmitForCheck) {
        // 存放结果
        List<MicroWorkSubmitProductCount> result = new ArrayList<>();

        if (CheckObjectUtils.isNotEmpty(productCountForSubmitForWaitCheck) && CheckObjectUtils.isNotEmpty(productCountForSubmitForCheck)) {

            Map<String, MicroWorkSubmitProductCount> collectTemp = productCountForSubmitForCheck.stream().collect(Collectors.toMap(MicroWorkSubmitProductCount::getSubmitDay, v -> v));
            // 数量相加
            List<MicroWorkSubmitProductCount> finalResult = result;
            productCountForSubmitForWaitCheck.forEach(obj -> {
                MicroWorkSubmitProductCount temp = new MicroWorkSubmitProductCount();
                MicroWorkSubmitProductCount productCountTemp = collectTemp.get(obj.getSubmitDay());
                if (productCountTemp != null) {
                    temp.setTotalCounts(obj.getTotalCounts().add(productCountTemp.getTotalCounts()));
                    temp.setTotalNgNum(obj.getTotalNgNum().add(productCountTemp.getTotalNgNum()));
                    temp.setTotalPassNum(obj.getTotalPassNum().add(productCountTemp.getTotalPassNum()));
                    collectTemp.remove(obj.getSubmitDay());
                } else {
                    BeanUtils.copyProperties(obj, temp);
                }
                temp.setSubmitDay(obj.getSubmitDay());
                finalResult.add(temp);
            });
            // 添加时间上没有匹配的剩余的MicroWorkSubmitProductCount
            finalResult.addAll(collectTemp.values());
        } else if (CheckObjectUtils.isNotEmpty(productCountForSubmitForCheck) && CheckObjectUtils.isEmpty(productCountForSubmitForWaitCheck)) {
            result = productCountForSubmitForCheck;
        } else if (CheckObjectUtils.isNotEmpty(productCountForSubmitForWaitCheck) && CheckObjectUtils.isEmpty(productCountForSubmitForCheck)) {
            result = productCountForSubmitForWaitCheck;
        }

        return result;
    }

    /**
     * 工易派 - 工单维度的审产信息列表
     *
     */
    @Override
    public List<SubmitRecordInfoByWorkOrder> selectSubmitRecordInfoByWorkOrder(CheckParamByWorkOrder checkParamByWorkOrder) {
        return microWorkSubmitMapper.selectSubmitRecordInfoByWorkOrderList(checkParamByWorkOrder);
    }

    /**
     * 工易派 - 某个工单下详细的报工记录信息
     *
     */
    @Override
    public List<DetailSubmitRecordInfoByWorkOrder> selectDetailSubmitRecordInfoByWorkOrderList(String workOrderNo, String submitStatus) {
        List<DetailSubmitRecordInfoByWorkOrder> detailSubmitRecordInfoByWorkOrders = microWorkSubmitMapper.selectDetailSubmitRecordInfoByWorkOrderList(workOrderNo, submitStatus);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(detailSubmitRecordInfoByWorkOrders)) {
            return Collections.emptyList();
        }
        // 设置到工序的总报工数量和报工记录数量
        for (DetailSubmitRecordInfoByWorkOrder detailSubmitRecordInfoByWorkOrder : detailSubmitRecordInfoByWorkOrders) {
            BigDecimal totalNum = BigDecimal.ZERO;
            int recordNum = 0;
            if (!org.apache.commons.collections.CollectionUtils.isEmpty(detailSubmitRecordInfoByWorkOrder.getUserSubmitRecordInfoByWorkOrderList())) {
                for (UserSubmitRecordInfoByWorkOrder user : detailSubmitRecordInfoByWorkOrder.getUserSubmitRecordInfoByWorkOrderList()) {
                    totalNum = totalNum.add(user.getNgNum().add(user.getPassNum()));
                    recordNum = recordNum + user.getIds().split(",").length;
                }
            }
            detailSubmitRecordInfoByWorkOrder.setRecordNum(recordNum);
            detailSubmitRecordInfoByWorkOrder.setTotalNum(totalNum);
        }
        return detailSubmitRecordInfoByWorkOrders;
    }

    /**
     * 获取某个工单某个工序下面的到人的报工记录信息
     *
     */
    @Override
    public List<UserDetailSubmitRecordInfoByWorkOder> selectUserDetailSubmitRecordInfoByWorkOder(UserSubmitRecordQueryParam userSubmitRecordQueryParam) {
        return microWorkSubmitMapper.selectUserDetailSubmitRecordInfoByWorkOder(userSubmitRecordQueryParam);
    }

    /**
     * 获取不同状态下的报工记录条数
     *
     */
    @Override
    public Map<String, Integer> obtainedSubmitRecordNumInDifferentStatus(MicroWorkSubmit microWorkSubmit) {
        return microWorkSubmitMapper.obtainedSubmitRecordNumInDifferentStatus(microWorkSubmit);
    }

    /**
     * 根据产品+现工序获取最近的前工序和当前现工序是否首尾序
     *
     */
    @Override
    public List<MicroLastPreProcessEntity> findLatestPreProcessAndFirstOrLast(List<MicroLastPreProcessEntity> rawNonStandardList) {
        Map<String, MicroLastPreProcessEntity> lastedPreProcessMap = microWorkSubmitMapper.findLastedPreProcess(rawNonStandardList);
        Map<String, MicroLastPreProcessEntity> firstOrLastProcessMap = microWorkSubmitMapper.findFirstOrLastProcess(rawNonStandardList);
        for (MicroLastPreProcessEntity entity : rawNonStandardList) {
            String productSeq = entity.getProductSeq();
            String operateProcessSeq = entity.getOperateProcessSeq();
            String verifyKey = productSeq + "&" + operateProcessSeq;
            if (firstOrLastProcessMap.containsKey(verifyKey)) {
                MicroLastPreProcessEntity firstOrLastEntity = firstOrLastProcessMap.get(verifyKey);
                entity.setIsFirstProcess(firstOrLastEntity.getIsFirstProcess());
                if (firstOrLastEntity.getIsFirstProcess()) {
                    continue;
                }
                entity.setIsLastProcess(firstOrLastEntity.getIsLastProcess());
            }
            if (lastedPreProcessMap.containsKey(verifyKey)) {
                MicroLastPreProcessEntity lastPreProcessEntity = lastedPreProcessMap.get(verifyKey);
                entity.setPreProcessId(lastPreProcessEntity.getPreProcessId());
                entity.setPreProcessCode(lastPreProcessEntity.getPreProcessCode());
                entity.setPreProcessName(lastPreProcessEntity.getPreProcessName());
                entity.setPreProcessSeq(lastPreProcessEntity.getPreProcessSeq());
            }
        }
        return rawNonStandardList;
    }

    @Override
    public Map<String, List<MicroWorkSubmit>> selectMicroRecordByProductList(List<String> productList) {
        List<MicroWorkSubmit> record = microWorkSubmitMapper.selectMicroWorkSubmitByProduct(productList);
        if (CollectionUtils.isEmpty(record)) {
            return Collections.emptyMap();
        } else {
            return record.stream().collect(Collectors.groupingBy(MicroWorkSubmit::getProductSeq));
        }
    }

    @Override
    public List<String> selectAllProductBySubmit(MicroWorkSubmit submit) {
        return microWorkSubmitMapper.selectAllProductBySubmit(submit);
    }

    /**
     * 获取不良品清单 - 产品（已审核）
     *
     */
    @Override
    public List<NgProductByProductFromAlreadyCheck> selectNgProductListFromAlreadyCheck(String productNameOrCode) {
        Long submitType = supportUtil.getSubmitType();
        List<NgProductByProductFromAlreadyCheck> ngProductList = microWorkSubmitMapper.selectNgProductListFromAlreadyCheck(productNameOrCode, submitType);
        if (CollectionUtils.isEmpty(ngProductList)) {
            return ngProductList;
        }

        String productSeqList = ngProductList.stream().map(NgProductByProductFromAlreadyCheck::getProductSeq).collect(Collectors.joining(","));
        List<NgProductAndProcessByProductFromAlreadyCheck> productAndProcessList = microWorkSubmitMapper.selectNgProductAndProcessListFromAlreadyCheck(productSeqList, submitType);
        Map<String, List<NgProductAndProcessByProductFromAlreadyCheck>> map = productAndProcessList.stream().collect(Collectors.groupingBy(NgProductAndProcessByProductFromAlreadyCheck::getProductSeq));

        for (NgProductByProductFromAlreadyCheck ngProductByProductFromAlreadyCheck : ngProductList) {
            List<NgProductAndProcessByProductFromAlreadyCheck> ngProductAndProcessListTemp = map.get(ngProductByProductFromAlreadyCheck.getProductSeq());
            ngProductByProductFromAlreadyCheck.setNgProductAndProcessCheckList(ngProductAndProcessListTemp);
        }

        return ngProductList;
    }

    /**
     * 获取不良品清单 - 员工（已审核）
     *
     */
    @Override
    public List<NgProductByUserFromAlreadyCheck> selectNgProductListByUserFromAlreadyCheck(String submitNickName) {
        Long submitType = supportUtil.getSubmitType();
        List<NgProductByUserFromAlreadyCheck> ngProductListByUser = microWorkSubmitMapper.selectNgProductByUserFromAlreadyCheck(submitNickName, submitType);
        if (CollectionUtils.isEmpty(ngProductListByUser)) {
            return ngProductListByUser;
        }

        List<Long> submitUserList = ngProductListByUser.stream().map(NgProductByUserFromAlreadyCheck::getSubmitUser).collect(Collectors.toList());
        List<NgProductAndProcessByUserFromAlreadyCheck> ngProductAndProcessByUserList = microWorkSubmitMapper.selectNgProductAndProcessByUserFromAlreadyCheck(submitUserList, submitType);
        Map<Long, List<NgProductAndProcessByUserFromAlreadyCheck>> map = ngProductAndProcessByUserList.stream().collect(Collectors.groupingBy(NgProductAndProcessByUserFromAlreadyCheck::getSubmitUser));

        for (NgProductByUserFromAlreadyCheck ngProductByUserFromAlreadyCheck : ngProductListByUser) {
            List<NgProductAndProcessByUserFromAlreadyCheck> ngProductAndProcessListTemp = map.get(ngProductByUserFromAlreadyCheck.getSubmitUser());
            ngProductByUserFromAlreadyCheck.setNgProductAndProcessByUserFromAlreadyCheckList(ngProductAndProcessListTemp);
        }

        return ngProductListByUser;
    }

    /**
     * 计算报工的数量 （已审核、未审核）
     * 只计算良品数量
     *
     */
    @Override
    public TotalSubmitNumDTO obtainedSubmitNum(MicroWorkSubmit microWorkSubmit) {
        log.info("请求的参数为{}", JSONObject.toJSONString(microWorkSubmit));
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectMicroWorkSubmitList(microWorkSubmit);

        BigDecimal waitCheckNum = BigDecimal.ZERO;
        BigDecimal checkNum = BigDecimal.ZERO;
        // 计算数量
        for (MicroWorkSubmit temp : microWorkSubmitList) {
            // 待审核数量
            if (temp.getSubmitStatus().equals(SubmitStatusEnum.UN_APPROVE.getCode())) {
                waitCheckNum = waitCheckNum.add(temp.getPassNum());
            } else if (temp.getSubmitStatus().equals(SubmitStatusEnum.APPROVED.getCode())) {
                checkNum = checkNum.add(temp.getCheckPassNum());
            }

        }
        TotalSubmitNumDTO result = new TotalSubmitNumDTO();
        result.setCheckNum(checkNum);
        result.setWaitCheckNum(waitCheckNum);
        return result;
    }

    /**
     * 根据工序ID获取报工记录
     *
     */
    @Override
    public List<MicroSelectEntity> selectReProcessByProcessIds(Long[] ids) {
        return microWorkSubmitMapper.selectReProcessByProcessIds(ids);
    }

    /**
     * 获取用户不同状态报工记录信息
     *
     */
    @Override
    public List<MicroWorkSubmit> getSubmitInfoInDifferentStatusByUser(Date startDate, Date endDate, Long submitStatus) {
        String userId = SecurityUtils.getUserId().toString();
        Long submitType = supportUtil.getSubmitType();
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectWorkSubmitExInfosBySubmitUserAndDay(userId, startDate, endDate, submitType, submitStatus);
        return microWorkSubmitList;
    }

    /**
     * 获取用户不同状态报工记录汇总信息
     *
     */
    @Override
    public SubmitTotalInfoInDifferentStatusByUser getSubmitSummaryInfoByUser(Date startDate, Date endDate, Long submitStatus) {
        String userId = SecurityUtils.getUserId().toString();
        Long submitType = supportUtil.getSubmitType();
        return microWorkSubmitMapper.getSubmitSummaryInfoByUser(userId, startDate, endDate, submitType, submitStatus);
    }

    /**
     * 报工记录的异常判断
     *
     * @param warningMetrics         产品 + 工序的日产能和良品率
     */
    public List<MicroWorkSubmitDto> judgeSubmitRecordsException(List<MicroWorkSubmitDto> microWorkSubmitDtoList,
                                                                Map<String, Map<String, Object>> warningMetrics,
                                                                Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition) {
        // 异常判断
        for (MicroWorkSubmitDto obj : microWorkSubmitDtoList) {
            this.judgeSingleSubmitRecordException(obj,warningMetrics, allSubmitTotalNumByCondition);
        }
        return microWorkSubmitDtoList;
    }

    /**
     * 单条报工记录异常判断
     *
     */
    public void judgeSingleSubmitRecordException(MicroWorkSubmitDto microWorkSubmitDto, 
                                                               Map<String, Map<String, Object>> warningMetrics,
                                                               Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition) {
        // 1. 负库存风险判断
        List<BigDecimal> preProcessTotalNumList = new ArrayList<>();
        if (StringUtils.hasText(microWorkSubmitDto.getPreProcessSeq())) {
            List<String> preProcessSeqList = Arrays.stream(microWorkSubmitDto.getPreProcessSeq().split(",")).collect(Collectors.toList());
            for (String preProcessKey : preProcessSeqList) {
                MicroProcessStorageDto preProcessParam = new MicroProcessStorageDto();
                preProcessParam.setProductSeq(microWorkSubmitDto.getProductSeq());
                preProcessParam.setProcessSeq(preProcessKey);
                // 查询该产品+前工序是否已经有库存
                List<MicroProcessStorage> microProcessStorages = microProcessStorageMapper.selectMicroProcessStorageList(preProcessParam);
                // 还要找当前产品 + 该工序的未审核的报工良品数量
                CountForProductAndProcess countForProductAndProcess = microWorkSubmitMapper.obtainedCountForProductAndProcess(microWorkSubmitDto.getProductSeq(), microWorkSubmitDto.getPreProcessSeq(), SubmitStatusEnum.UN_APPROVE.getCode());
                // 设置前工序的未审核数量
                microWorkSubmitDto.setPreProcessWaitCheckPassNum(countForProductAndProcess.getTotalNum());

                BigDecimal preProcessTotalNum;
                if (CollectionUtils.isEmpty(microProcessStorages)) {
                    preProcessTotalNum = countForProductAndProcess.getTotalNum();
                } else {
                    MicroProcessStorage preProcessStorage = microProcessStorages.get(0);
                    preProcessTotalNum = preProcessStorage.getPassNum().add(countForProductAndProcess.getTotalNum());
                }
                preProcessTotalNumList.add(preProcessTotalNum);
            }
        }
        // 负库存标签
        // 口径：只有良品在工序间流转，所以判断"会不会扣成负库存"只比良品（原来的 passNum + ngNum 会把不良算进去 → 误报负库存）
        if (!CollectionUtils.isEmpty(preProcessTotalNumList)) {
            for (BigDecimal preProcessTotalNum : preProcessTotalNumList) {
                if (preProcessTotalNum.subtract(microWorkSubmitDto.getPassNum()).signum() < 0) {
                    microWorkSubmitDto.setNegativeStockFlag(NegativeStockFlagEnum.NEGATIVE_STOCK.getCode());
                    break;
                } else {
                    microWorkSubmitDto.setNegativeStockFlag(NegativeStockFlagEnum.POSITIVE_STOCK.getCode());
                }
            }
        } else {
            microWorkSubmitDto.setNegativeStockFlag(NegativeStockFlagEnum.POSITIVE_STOCK.getCode());
        }

        // 2. 良品率偏低判断
        // 获取该产品+工序的良品率和产能
        Map<String, Object> warningMetricsBySeq = warningMetrics.get(microWorkSubmitDto.getProductSeq() + "_" + microWorkSubmitDto.getOperateProcessSeq());
        // 如果为空，说明没有形成库存，也就没有良品率
        if (CheckObjectUtils.isNotEmpty(warningMetricsBySeq)) {
            BigDecimal totalNumForRate = microWorkSubmitDto.getPassNum().add(microWorkSubmitDto.getNgNum());
            if (totalNumForRate.signum() == 0) {
                // 报工数为 0 时算不出良品率：按"正常"处理并跳过比值判断
                // （原实现直接 divide(0) 抛 ArithmeticException: / by zero，会把整个记工列表接口打成 500）
                log.warn("[良品率] 产品{} 工序{} 报工数为0，跳过良品率判断",
                        microWorkSubmitDto.getProductSeq(), microWorkSubmitDto.getOperateProcessSeq());
                microWorkSubmitDto.setLowPassRateFlag(LowPassRateFlagEnum.NORMAL.getCode());
            } else {
                BigDecimal passRateTemp = microWorkSubmitDto.getPassNum().divide(totalNumForRate, 4, RoundingMode.DOWN);
                // 设置当前良品率
                microWorkSubmitDto.setPassRate(passRateTemp);
                BigDecimal passRate = new BigDecimal(warningMetricsBySeq.get(DataWarnTypeEnum.PASS_RATE.getCode()).toString());
                if (passRateTemp.compareTo(passRate.multiply(PASS_RATE_THRESHOLD)) < 0) {
                    // 设置日均良品率
                    microWorkSubmitDto.setAvgPassRateByDay(passRate);
                    microWorkSubmitDto.setLowPassRateFlag(LowPassRateFlagEnum.LOW.getCode());
                } else {
                    microWorkSubmitDto.setLowPassRateFlag(LowPassRateFlagEnum.NORMAL.getCode());
                }
            }
        } else {
            microWorkSubmitDto.setLowPassRateFlag(LowPassRateFlagEnum.NORMAL.getCode());
        }

        // 3. 超产能判断
        // 报工数量是要累加的, 当天的所有记工数量
        // 添加统计时间范围字段
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String conditionKey = microWorkSubmitDto.getProductSeq() + "_" + microWorkSubmitDto.getOperateProcessSeq() + "_"
                + microWorkSubmitDto.getSubmitUser() + "_" + simpleDateFormat.format(microWorkSubmitDto.getSubmitDay());
        // 原则上不能为空
        TotalSubmitNumByCondition totalSubmitNumByCondition = allSubmitTotalNumByCondition.get(conditionKey);
        BigDecimal totalNumTemp = BigDecimal.ZERO;
        String overProductiveCapacityExceptionIds = null;
        if (totalSubmitNumByCondition != null) {
            if (totalSubmitNumByCondition.getTotalSubmitNum() != null) {
                totalNumTemp = totalSubmitNumByCondition.getTotalSubmitNum();
            }
            if (totalSubmitNumByCondition.getIds() != null) {
                overProductiveCapacityExceptionIds = totalSubmitNumByCondition.getIds();
            }
        }

        MicroCapacityUpLimitEntity upLimitEntity = new MicroCapacityUpLimitEntity.Builder()
                .withOperateProcessSeq(microWorkSubmitDto.getOperateProcessSeq())
                .withProductSeq(microWorkSubmitDto.getProductSeq())
                .withUserId(microWorkSubmitDto.getSubmitUser())
                .build();
        BigDecimal totalNum = supportUtil.getCapacityUpLimit(upLimitEntity);

        if (totalNumTemp.compareTo(totalNum.multiply(CAPACITY_OVER_THRESHOLD)) > 0) {
            // 设置日产能
            microWorkSubmitDto.setAvgProductionCapacityByDay(totalNum);
            microWorkSubmitDto.setOverProductiveCapacityFlag(OverProductiveCapacityFlagEnum.OVER.getCode());
            // 新增超产能风险异常字段跳转ids
            microWorkSubmitDto.setOverProductiveCapacityExceptionRecordIds(overProductiveCapacityExceptionIds);
            microWorkSubmitDto.setTodaySubmitNum(totalNumTemp);
        } else {
            microWorkSubmitDto.setOverProductiveCapacityFlag(OverProductiveCapacityFlagEnum.NOT_OVER.getCode());
        }
    }

    /**
     * 递归执行每个节点, 从尾巴开始找
     *
     * @param productSeq                          产品序列码 - 需要从最外侧获取,内部没有报工记录就获取不到
     * @param microProcessChainBindDTOList        工艺链信息
     * @param currentNodeList                     当前操作节点 从尾巴节点往上 -> 可能有多个
     * @param microWorkSubmitDtoList              当前操作节点对应的报工记录信息
     * @param allStorageMap                       一次查出所有产品 + 工序的库存信息
     * @param allDetailCheckSubmitRecordByProduct 存储所有经过业务处理之后的展示信息
     * @param warningMetrics   良品率指标
     * @param allSubmitTotalNumByCondition 产品 + 工序 + 人的报工总数量
     * @param ids 存放符合工艺的报工记录id
     */
    @Deprecated
    public void standardCraftLoop(String productSeq,
                                  String productUnit,
                                  List<MicroWorkSubmitDto> microWorkSubmitDtoList,
                                  List<MicroProcessChainBindEntity> microProcessChainBindDTOList,
                                  List<MicroProcessChainBindEntity> currentNodeList,
                                  Map<String, StorageForProductAndProcess> allStorageMap,
                                  List<DetailCheckSubmitRecordByProduct> allDetailCheckSubmitRecordByProduct,
                                  Map<String, Map<String, Object>> warningMetrics,
                                  Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition,
                                  List<Long> ids) {
        // 业务操作， 数据计算
        List<DetailCheckSubmitRecordByProduct> detailCheckSubmitRecordsByProduct = getDetailCheckSubmitRecordByProduct(productSeq,
                productUnit, currentNodeList, microWorkSubmitDtoList, allStorageMap, warningMetrics, allSubmitTotalNumByCondition, ids);
        // 如果已经存在了各工序的详细报工信息，则要进一步计算转序的数量, 实际计算的是上一层的信息
        if (!CollectionUtils.isEmpty(allDetailCheckSubmitRecordByProduct)) {
            for (DetailCheckSubmitRecordByProduct currentProduct : detailCheckSubmitRecordsByProduct) {
                List<DetailCheckSubmitRecordByProduct> beforeDetailRecordList = new ArrayList<>();
                // 从之前存入的详细记录信息里面获取报工数量，作为当前工序的流转数量
                for (DetailCheckSubmitRecordByProduct beforeProduct : allDetailCheckSubmitRecordByProduct) {
                    if (beforeProduct.getPreProcessSeq().contains(currentProduct.getProcessSeq())) {
                        // 塞入前工序
                        beforeDetailRecordList.add(beforeProduct);
                        // 累加方式 设置转下序的数量
                        currentProduct.setProcessFlowNum(currentProduct.getProcessFlowNum().add(beforeProduct.getSubmitNum()));

                        // 转下序设置 累加的方式
                        ProcessFlow flowTemp = new ProcessFlow();
                        flowTemp.setFlowNum(beforeProduct.getSubmitNum());
                        flowTemp.setProcessName(beforeProduct.getProcessName());
                        List<ProcessFlow> flowList = new ArrayList<>();
                        if (!CollectionUtils.isEmpty(currentProduct.getProcessFlowList())) {
                            flowList = currentProduct.getProcessFlowList();
                        }
                        flowList.add(flowTemp);
                        currentProduct.setProcessFlowList(flowList);
                    }
                }
                BigDecimal checkNum = currentProduct.getStockNum().add(currentProduct.getSubmitNum()).subtract(currentProduct.getProcessFlowNum());
                currentProduct.setCheckNum(checkNum);

                // 设置标示
                if (checkNum.signum() < 0) {
                    currentProduct.setCommonWarnFlag(CommonConstants.YES);
                    currentProduct.setCommonWarnMessage("后工序累计产出已大于本工序累计产出");

                    beforeDetailRecordList.forEach(obj -> {
                        obj.setOverSubmitWarnFlag(CommonConstants.YES);
                        obj.setOverSubmitWarnMessage("本工序累计报工大于前工序累计已产出");
                    });
                }
            }
        }

        // 找到当前节点的上一层节点
        List<MicroProcessChainBindEntity> nextNodeList = new ArrayList<>();
        for (MicroProcessChainBindEntity microProcessChainBindDTO : microProcessChainBindDTOList) {
            for (MicroProcessChainBindEntity currentNode : currentNodeList) {
                if (!CommonConstants.ROOT_PROCESS_SEQ.equals(currentNode.getParentProcessSeq()) && currentNode.getParentProcessSeq().contains(microProcessChainBindDTO.getProcessSeq())) {
                    nextNodeList.add(microProcessChainBindDTO);
                }
            }
        }

        allDetailCheckSubmitRecordByProduct.addAll(detailCheckSubmitRecordsByProduct);
        if (!CollectionUtils.isEmpty(nextNodeList)) {
            // 并序情况，nextNodeList去重，如果有工序相同的则合并前工序为一个字段
            nextNodeList = this.mergeNextNodeList(nextNodeList);
            standardCraftLoop(productSeq, productUnit, microWorkSubmitDtoList, microProcessChainBindDTOList, nextNodeList, allStorageMap,
                                allDetailCheckSubmitRecordByProduct, warningMetrics, allSubmitTotalNumByCondition, ids);
        }
    }

    /**
     * 根据标准工艺获取每个工序节点的实际业务信息
     *
     * @param productSeq             产品序列码 - 需要从最外侧获取,内部没有报工记录就获取不到
     * @param currentNodeList        当前操作节点 从尾巴节点往上 -> 可能有多个
     * @param microWorkSubmitDtoList 当前操作节点对应的
     * @param allStorageMap          一次查出所有产品 + 工序的库存信息
     * @param ids 存放符合工艺的报工记录id
     */
    public List<DetailCheckSubmitRecordByProduct> getDetailCheckSubmitRecordByProduct(String productSeq,
                                                                                      String productUnit,
                                                                                      List<MicroProcessChainBindEntity> currentNodeList,
                                                                                      List<MicroWorkSubmitDto> microWorkSubmitDtoList,
                                                                                      Map<String, StorageForProductAndProcess> allStorageMap,
                                                                                      Map<String, Map<String, Object>> warningMetrics,
                                                                                      Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition,
                                                                                      List<Long> ids) {
        // 存储当前节点下面的详细业务信息
        List<DetailCheckSubmitRecordByProduct> currentNodeDetailResult = new ArrayList<>();

        for (MicroProcessChainBindEntity node : currentNodeList) {
            // 获取该工序对应的报工记录信息
            // 判断逻辑：1. 当前工序不为空时，要确定前工序包含在工艺中当前工序的前工序中 2. 当前工序为空时，要确定工艺中的当前工序的前工序为0，即为首序
            List<MicroWorkSubmitDto> records = microWorkSubmitDtoList.stream().filter(obj -> obj.getOperateProcessSeq().equals(node.getProcessSeq())
                                                && ((obj.getPreProcessSeq() != null && Arrays.stream(obj.getPreProcessSeq().split(",")).allMatch(processSeq -> node.getParentProcessSeq().contains(processSeq)))
                                                    || (obj.getPreProcessSeq() == null && CommonConstants.ROOT_PROCESS_SEQ.equals(node.getParentProcessSeq()))))
                                                .collect(Collectors.toList());
            // 查询该工序的库存数量
            StorageForProductAndProcess storageForProductAndProcess = allStorageMap.get(productSeq + "_" + node.getProcessSeq());
            // 设置返回值
            DetailCheckSubmitRecordByProduct temp = new DetailCheckSubmitRecordByProduct();
            temp.setProcessName(node.getProcessName());
            temp.setProcessSeq(node.getProcessSeq());
            temp.setProcessCode(node.getProcessCode());
            temp.setProductSeq(productSeq);
            temp.setProductUnit(productUnit);
            // 设置前工序序列码, 用于在loop中转下序的数量计算
            temp.setPreProcessSeq(node.getParentProcessSeq());
            // 库存设置
            if (storageForProductAndProcess == null) {
                temp.setStockNum(BigDecimal.ZERO);
            } else {
                temp.setStockNum(storageForProductAndProcess.getPassNum());
            }
            if (CollectionUtils.isEmpty(records)) {
                temp.setSubmitNum(BigDecimal.ZERO);
                temp.setRecordNum(0);
            } else {
                // 存放符合工艺的报工记录id
                ids.addAll(records.stream().map(MicroWorkSubmitDto::getId).collect(Collectors.toList()));
                // 获取到人的报工记录信息
                List<UserDetailSubmitRecordInfo> userList = this.getUserDetailSubmitRecordInfo(records, warningMetrics, allSubmitTotalNumByCondition);
                // 计算良品数（已质检的取质检数量）
                BigDecimal totalPassNum = userList.stream().map(UserDetailSubmitRecordInfo::getPassNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                // 计算不良品数（已质检的取质检后不良品数量）
                BigDecimal totalNgNum = userList.stream().map(UserDetailSubmitRecordInfo::getNgNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                temp.setSubmitNum(totalPassNum.add(totalNgNum));
                temp.setRecordNum(records.size());
                temp.setUserDetailSubmitRecordInfos(userList);
            }
            // 首序设置
            if (CommonConstants.YES.equals(node.getParentProcessSeq())) {
                temp.setIsFirstProcess(CommonConstants.YES);
            } else {
                temp.setIsFirstProcess(CommonConstants.NO);
            }
            // 初始化转下序为0
            temp.setProcessFlowNum(BigDecimal.ZERO);
            // 尾序设置
            if (CommonConstants.YES.equals(node.getIsLastProcess())) {
                // 转序为0，审核之后的数量可以直接计算出来
                temp.setCheckNum(temp.getStockNum().add(temp.getSubmitNum()).subtract(temp.getProcessFlowNum()));
            }
            temp.setIsLastProcess(node.getIsLastProcess());
            currentNodeDetailResult.add(temp);
        }
        return currentNodeDetailResult;
    }

    /**
     * 合并相同当前工序的节点,并重置ParentProcessSeq字段
     *
     */
    public List<MicroProcessChainBindEntity> mergeNextNodeList(List<MicroProcessChainBindEntity> nextNodeList) {
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

    /**
     * 1. 标准工艺判断 -> 其实乱序属于非标准的工艺（报工可能是混乱的）
     * 2. 真正的标准工艺 -> 要判断不符合标准工艺的报工记录作为异常数据进行跳转
     *
     * 过滤出符合标准工艺的所有报工记录
     *
     */
    public List<Long> filterSubmitRecordByStandardTech(List<MicroWorkSubmitDto> microWorkSubmitDtoList,
                                                       List<MicroProcessChainBindEntity> bindEntityList) {
        List<Long> ids = new ArrayList<>();
        if (!CollectionUtils.isEmpty(bindEntityList)) {
            // 只有一个产品，也就只有一种工艺
            int techPattern = bindEntityList.get(0).getTechPattern();
            // 按照当前工序进行分组
            Map<String, List<MicroWorkSubmitDto>> map = microWorkSubmitDtoList.stream().collect(Collectors.groupingBy(MicroWorkSubmitDto::getOperateProcessSeq));
            // 标准工艺 顺序
            if (techPattern == TechPatternEnum.SERIAL.getCode()) {
                for (Map.Entry<String, List<MicroWorkSubmitDto>> entry : map.entrySet()) {
                    // 找工艺链中前工序，正常工艺链不会出现前工序为空的情况 (如果是首序，则前空序为0)
                    List<MicroProcessChainBindEntity> preProcessChainList = bindEntityList.stream().filter(obj -> obj.getProcessSeq().equals(entry.getKey())).collect(Collectors.toList());
                    // 当前工序就不符合工艺路线
                    if (CollectionUtils.isEmpty(preProcessChainList)) {
                        ids.addAll(entry.getValue().stream().map(MicroWorkSubmitDto::getId).collect(Collectors.toList()));
                    } else {
                        List<String> preProcessSeqList = preProcessChainList.stream().map(MicroProcessChainBindEntity::getParentProcessSeq).distinct().collect(Collectors.toList());
                        List<MicroWorkSubmitDto> submitListTemp = entry.getValue();
                        for (MicroWorkSubmitDto microWorkSubmitDto : submitListTemp) {
                            // 判定前工序是否符合工艺路线
                            boolean conditionFlag = (StringUtils.isEmpty(microWorkSubmitDto.getPreProcessSeq()) && preProcessSeqList.contains("0"))
                                    || (StringUtils.hasText(microWorkSubmitDto.getPreProcessSeq())
                                    && Arrays.stream(microWorkSubmitDto.getPreProcessSeq().split(",")).allMatch(processSeq -> preProcessSeqList.contains(processSeq)));
                            if (!conditionFlag) {
                                ids.add(microWorkSubmitDto.getId());
                            }
                        }
                    }
                }
                // 标准工艺乱序
            } else if (techPattern == TechPatternEnum.UN_ORDER.getCode()) {
                // 排除尾序的工艺链
                List<String> chainListExcludeLastProcess = bindEntityList.subList(0, bindEntityList.size() - 1).stream().map(MicroProcessChainBindEntity::getProcessSeq).collect(Collectors.toList());
                for (Map.Entry<String, List<MicroWorkSubmitDto>> entry : map.entrySet()) {
                    // 确定当前工序是否在工艺链中
                    List<MicroProcessChainBindEntity> processChainList = bindEntityList.stream().filter(obj -> obj.getProcessSeq().equals(entry.getKey())).collect(Collectors.toList());
                    // 如果当前工序不在工艺链的工序中，则都是不符合工艺的记录
                    if (CollectionUtils.isEmpty(processChainList)) {
                        ids.addAll(entry.getValue().stream().map(MicroWorkSubmitDto::getId).collect(Collectors.toList()));
                    } else {
                        // 判断前工序是否在工艺链中
                        List<MicroWorkSubmitDto> submitListTemp = entry.getValue();
                        // 默认sort为0就是首序
                        Integer sort = processChainList.get(0).getSort();
                        for (MicroWorkSubmitDto microWorkSubmitDto : submitListTemp) {
                            // 是首序, 但是前工序不为null
                            if (sort == 0) {
                                if (StringUtils.hasText(microWorkSubmitDto.getPreProcessSeq())) {
                                    ids.add(microWorkSubmitDto.getId());
                                }
                            } else {
                                // 不是首序，则需要前工序，并且前工序必须在工艺链中（排除尾序）
                                boolean flag = StringUtils.isEmpty(microWorkSubmitDto.getPreProcessSeq())
                                        || (StringUtils.hasText(microWorkSubmitDto.getPreProcessSeq()) && !chainListExcludeLastProcess.contains(microWorkSubmitDto.getPreProcessSeq()));
                                if (flag) {
                                    ids.add(microWorkSubmitDto.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
        return ids;
    }


    /**
     * 根据报工记录构建展示信息, 要根据工艺的信息用于区分不符合标准顺序工艺的报工记录
     *
     * 前工序只会有一个
     *
     */
    public List<DetailCheckSubmitRecordByProduct> createDetailSubmitRecordDisplayInfo(String productSeq, 
                                                                                      String productUnit,
                                                                                      List<MicroWorkSubmitDto> microWorkSubmitDtoList,
                                                                                      Map<String, StorageForProductAndProcess> allStorageMap,
                                                                                      Map<String, Map<String, Object>> warningMetrics,
                                                                                      Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition,
                                                                                      Integer techPattern) {

        List<DetailCheckSubmitRecordByProduct> res = new ArrayList<>();
        // 按照当前工序进行分组划分
        Map<String, List<MicroWorkSubmitDto>> recordByProcessMap = microWorkSubmitDtoList.stream().collect(Collectors.groupingBy(MicroWorkSubmitDto::getOperateProcessSeq));
        for (String processSeqKey : recordByProcessMap.keySet()) {
            // 获取当前工序的报工记录信息
            List<MicroWorkSubmitDto> recordList = recordByProcessMap.get(processSeqKey);
            // 用于工序流转列表设置工序名称
            String processName = recordList.get(0).getOperateProcessName();

            // 1. 该工序是否已经存在了相关整合的信息
            Optional<DetailCheckSubmitRecordByProduct> alreadyHave = res.stream().filter(obj -> obj.getProcessSeq().equals(processSeqKey)).findFirst();
            // 出现的情况是（前工序也有了报工）
            if (alreadyHave.isPresent()) {
                DetailCheckSubmitRecordByProduct alreadyDetailRecordInfo = alreadyHave.get();
                // 设置到报工人的信息
                List<UserDetailSubmitRecordInfo> userList = this.getUserDetailSubmitRecordInfo(recordList, warningMetrics, allSubmitTotalNumByCondition);
                BigDecimal passNum = userList.stream().map(UserDetailSubmitRecordInfo::getPassNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal ngNum = userList.stream().map(UserDetailSubmitRecordInfo::getNgNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                // 重新设置报工数量
                alreadyDetailRecordInfo.setSubmitNum(alreadyDetailRecordInfo.getSubmitNum().add(passNum.add(ngNum)));
                alreadyDetailRecordInfo.setUserDetailSubmitRecordInfos(userList);
                // 报工记录数
                alreadyDetailRecordInfo.setRecordNum(alreadyDetailRecordInfo.getRecordNum() + recordList.size());
                if (!StringUtils.hasText(alreadyDetailRecordInfo.getIsFirstProcess()) && !StringUtils.hasText(alreadyDetailRecordInfo.getIsLastProcess())) {
                    // 非标准工艺或者标准工艺乱序才设置信息, 顺序标准工艺在外侧最后排序的时候设置首尾序、工序编码和名称信息
                    if (techPattern == null) {
                        this.setRecordBaseInfo(alreadyDetailRecordInfo, recordList, CURRENT);
                    }
                }
            } else {
                DetailCheckSubmitRecordByProduct currentDetailSubmitRecord = new DetailCheckSubmitRecordByProduct();
                // 按照当前工序新增一个要整合的展示信息
                // 非标准工艺或者标准工艺乱序才设置信息
                if (techPattern == null) {
                    this.setRecordBaseInfo(currentDetailSubmitRecord, recordList, CURRENT);
                }

                currentDetailSubmitRecord.setProductSeq(productSeq);
                currentDetailSubmitRecord.setProcessSeq(processSeqKey);
                currentDetailSubmitRecord.setProductUnit(productUnit);
                // 设置库存数
                StorageForProductAndProcess storageForProductAndProcess = allStorageMap.get(productSeq + "_" + processSeqKey);
                currentDetailSubmitRecord.setStockNum(storageForProductAndProcess != null ? storageForProductAndProcess.getPassNum() : BigDecimal.ZERO);
                // 设置条数
                currentDetailSubmitRecord.setRecordNum(recordList.size());
                // 获取到人的报工记录信息
                List<UserDetailSubmitRecordInfo> userList = this.getUserDetailSubmitRecordInfo(recordList, warningMetrics, allSubmitTotalNumByCondition);
                // 设置报工数量
                BigDecimal passNum = userList.stream().map(UserDetailSubmitRecordInfo::getPassNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal ngNum = userList.stream().map(UserDetailSubmitRecordInfo::getNgNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                currentDetailSubmitRecord.setSubmitNum(passNum.add(ngNum));
                currentDetailSubmitRecord.setUserDetailSubmitRecordInfos(userList);
                // 转下序的数量为0
                currentDetailSubmitRecord.setProcessFlowNum(BigDecimal.ZERO);

                res.add(currentDetailSubmitRecord);
            }

            // 2. 如果当前工序的前工序有好几种情况，分别生成各自的展示信息, 要区分是标准工艺的顺序还是乱序
            Map<String, List<MicroWorkSubmitDto>> recordByPreProcessMap = new HashMap<>(16);
            // 标准工艺的顺序形式, 前工序可能是并序而且是用逗号隔开的
            if (techPattern != null && techPattern.equals(TechPatternEnum.SERIAL.getCode())) {
                // 针对并序的情况进行拆分，每一个前工序都有相同的报工记录信息
                String preProcessSeq = recordList.get(0).getPreProcessSeq();
                if (preProcessSeq != null) {
                    List<String> preProcessSeqList = Arrays.stream(preProcessSeq.split(",")).collect(Collectors.toList());
                    for (String preProcessSeqKey : preProcessSeqList) {
                        recordByPreProcessMap.put(preProcessSeqKey, recordList);
                    }
                }
            } else {
                recordByPreProcessMap = recordList.stream().filter(obj -> StringUtils.hasText(obj.getPreProcessSeq()))
                                                  .collect(Collectors.groupingBy(MicroWorkSubmitDto::getPreProcessSeq));
            }

            if (!CollectionUtils.isEmpty(recordByPreProcessMap)) {
                // 设置已经加入结果的前工序信息
                Optional<DetailCheckSubmitRecordByProduct> tempOptional = res.stream().filter(obj -> obj.getProcessSeq().equals(processSeqKey)).findFirst();
                if (tempOptional.isPresent()) {
                    DetailCheckSubmitRecordByProduct detailRecordInfo = tempOptional.get();
                    detailRecordInfo.setPreProcessSeq(recordByPreProcessMap.keySet().stream().collect(Collectors.joining(",")));
                }

                for (String preProcessSeqKey : recordByPreProcessMap.keySet()) {
                    List<MicroWorkSubmitDto> recordListByPreProcessSeq = recordByPreProcessMap.get(preProcessSeqKey);
                    BigDecimal passNumByPre = recordListByPreProcessSeq.stream().map(MicroWorkSubmitDto::getPassNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal ngNumByPre = recordListByPreProcessSeq.stream().map(MicroWorkSubmitDto::getNgNum).reduce(BigDecimal.ZERO, BigDecimal::add);

                    Optional<DetailCheckSubmitRecordByProduct> alreadyHaveByPre = res.stream().filter(obj -> obj.getProcessSeq().equals(preProcessSeqKey)).findFirst();
                    if (alreadyHaveByPre.isPresent()) {
                        DetailCheckSubmitRecordByProduct alreadyDetailRecordInfoByPre = alreadyHaveByPre.get();
                        // 设置流转数量 累加
                        alreadyDetailRecordInfoByPre.setProcessFlowNum(alreadyDetailRecordInfoByPre.getProcessFlowNum().add(passNumByPre.add(ngNumByPre)));
                        // 工序流转详情列表 累加
                        ProcessFlow temp = new ProcessFlow();
                        temp.setFlowNum(passNumByPre.add(ngNumByPre));
                        temp.setProcessName(processName);
                        List<ProcessFlow> flowList = new ArrayList<>();
                        if (!CollectionUtils.isEmpty(alreadyDetailRecordInfoByPre.getProcessFlowList())) {
                            flowList = alreadyDetailRecordInfoByPre.getProcessFlowList();
                        }
                        flowList.add(temp);
                        alreadyDetailRecordInfoByPre.setProcessFlowList(flowList);
                    } else {
                        // 添加新的前工序构成的记录信息
                        DetailCheckSubmitRecordByProduct preDetailSubmitRecord = new DetailCheckSubmitRecordByProduct();
                        if (techPattern == null) {
                            // 非标准工艺或者标准工艺乱序才设置信息
                            this.setRecordBaseInfo(preDetailSubmitRecord, recordListByPreProcessSeq, PRE);
                        }

                        preDetailSubmitRecord.setProductSeq(productSeq);
                        preDetailSubmitRecord.setProcessSeq(preProcessSeqKey);
                        preDetailSubmitRecord.setProductUnit(productUnit);
                        // 库存
                        StorageForProductAndProcess storageByPre = allStorageMap.get(productSeq + "_" + preProcessSeqKey);
                        preDetailSubmitRecord.setStockNum(storageByPre != null ? storageByPre.getPassNum() : BigDecimal.ZERO);
                        // 报工数
                        preDetailSubmitRecord.setSubmitNum(BigDecimal.ZERO);
                        // 流转数量
                        preDetailSubmitRecord.setProcessFlowNum(passNumByPre.add(ngNumByPre));

                        // 设置工序流转列表
                        ProcessFlow temp = new ProcessFlow();
                        temp.setFlowNum(preDetailSubmitRecord.getProcessFlowNum());
                        temp.setProcessName(processName);
                        List<ProcessFlow> processFlowsTemp = new ArrayList<>();
                        processFlowsTemp.add(temp);
                        preDetailSubmitRecord.setProcessFlowList(processFlowsTemp);

                        res.add(preDetailSubmitRecord);
                    }
                }
            }
        }

        for (DetailCheckSubmitRecordByProduct info : res) {
            // 设置审核后的数量
            BigDecimal checkNum = info.getStockNum().add(info.getSubmitNum()).subtract(info.getProcessFlowNum());
            info.setCheckNum(checkNum);
            if (checkNum.signum() < 0) {
                info.setCommonWarnFlag(CommonConstants.YES);
                info.setCommonWarnMessage("后工序累计产出已大于本工序累计产出");
                // 找出以当前工序作为负工序的工序信息, 并更新它的标示位
                List<DetailCheckSubmitRecordByProduct> collect = res.stream().filter(obj -> obj.getPreProcessSeq() != null && obj.getPreProcessSeq().contains(info.getProcessSeq()))
                        .collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(collect)) {
                    for (DetailCheckSubmitRecordByProduct record : collect) {
                        record.setOverSubmitWarnFlag(CommonConstants.YES);
                        record.setOverSubmitWarnMessage("本工序累计报工大于前工序累计已产出");
                    }
                }
            } else {
                info.setCommonWarnFlag(CommonConstants.NO);
            }
        }

        return res;
    }

    /**
     * 获取当前工序维度中到每个报工人的报工记录信息
     *
     */
    public List<UserDetailSubmitRecordInfo> getUserDetailSubmitRecordInfo(List<MicroWorkSubmitDto> microWorkSubmitDtoList,
                                                                          Map<String, Map<String, Object>> warningMetrics,
                                                                          Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition) {
        // 设置到人的信息
        List<UserDetailSubmitRecordInfo> userList = new ArrayList<>();
        Map<String, List<MicroWorkSubmitDto>> recordByUserMap = microWorkSubmitDtoList.stream().collect(Collectors.groupingBy(MicroWorkSubmitDto::getSubmitUser));
        for (Map.Entry<String, List<MicroWorkSubmitDto>> user : recordByUserMap.entrySet()) {
            UserDetailSubmitRecordInfo userTemp = new UserDetailSubmitRecordInfo();

            // 计算良品数（已质检的取质检数量）
            BigDecimal totalPassNum = user.getValue().stream().map(obj -> {
                if (obj.getCheckStatus().equals(CheckStatusEnum.FINISHED_INSPECTION.getCode())) {
                    return obj.getCheckPassNum();
                } else {
                    return obj.getPassNum();
                }}).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 计算良品数（已质检的取质检后不良品数量）
            BigDecimal totalNgNum = user.getValue().stream().map(obj -> {
                if (obj.getCheckStatus().equals(CheckStatusEnum.FINISHED_INSPECTION.getCode())) {
                    return obj.getCheckNgNum();
                } else {
                    return obj.getNgNum();
                }}).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 设置到工人维度的报工信息
            userTemp.setNgNum(totalNgNum);
            userTemp.setPassNum(totalPassNum);
            userTemp.setIds(user.getValue().stream().map(MicroWorkSubmitDto::getId).collect(Collectors.toList()));
            userTemp.setNickName(user.getValue().get(0).getSubmitNickName());
            userTemp.setUserId(Long.valueOf(user.getKey()));
            // 判断待检标示
            userTemp.setWaitQcFlag(user.getValue().stream().anyMatch(obj -> obj.getCheckStatus().equals(CheckStatusEnum.ALREADY_SUBMIT_FOR_INSPECTION.getCode())) ? 0L : 1L);

            // 判断警示标示
            List<MicroWorkSubmitDto> submitRecordsException = this.judgeSubmitRecordsException(user.getValue(), warningMetrics, allSubmitTotalNumByCondition);
            int num = 0;
            for (MicroWorkSubmitDto obj : submitRecordsException) {
                if (obj.getNegativeStockFlag().equals(NegativeStockFlagEnum.NEGATIVE_STOCK.getCode())
                    || obj.getLowPassRateFlag().equals(LowPassRateFlagEnum.LOW.getCode())
                    || obj.getOverProductiveCapacityFlag().equals(OverProductiveCapacityFlagEnum.OVER.getCode())) {
                    num ++;
                }
            }
            userTemp.setExceptionRecordNum(num);
            if (num == 0) {
                userTemp.setWarnFlag(CommonConstants.NO);
            } else {
                userTemp.setWarnFlag(CommonConstants.YES);
            }
            userList.add(userTemp);
        }
        return userList;
    }

    /**
     * 获取记录中的基础信息
     *
     * @param type 当前工序 , 前工序
     */
    public void setRecordBaseInfo(DetailCheckSubmitRecordByProduct detailCheckSubmitRecordByProduct, List<MicroWorkSubmitDto> recordList, String type) {

        Optional<String> productNameOptional = recordList.stream().map(MicroWorkSubmitDto::getProductName).findFirst();
        if (!productNameOptional.isPresent()) {
            throw new CustomException("记录中不存在产品名称信息");
        }
        String productName = productNameOptional.get();
        detailCheckSubmitRecordByProduct.setProductName(productName);

        Optional<String> productCodeOptional = recordList.stream().map(MicroWorkSubmitDto::getProductCode).findFirst();
        if (!productCodeOptional.isPresent()) {
            throw new CustomException("记录中不存在产品编码信息");
        }
        String productCode = productCodeOptional.get();
        detailCheckSubmitRecordByProduct.setProductCode(productCode);

        // 前工序进来的时候不能设置首尾序信息（因为他是根据当前工序来设置，不合理）
        if (CURRENT.equals(type)) {
            this.setFirstAndLastProcessFlag(detailCheckSubmitRecordByProduct, recordList);
        }

        Optional<String> processCodeOptional;
        if (PRE.equals(type)) {
            processCodeOptional = recordList.stream().map(MicroWorkSubmitDto::getPreProcessCode).findFirst();
        } else {
            processCodeOptional = recordList.stream().map(MicroWorkSubmitDto::getOperateProcessCode).findFirst();
        }
        if (!processCodeOptional.isPresent()) {
            throw new CustomException("记录中不存在产品编码信息");
        }
        String processCode = processCodeOptional.get();
        detailCheckSubmitRecordByProduct.setProcessCode(processCode);

        Optional<String> processNameOptional;
        if (PRE.equals(type)) {
            processNameOptional = recordList.stream().map(MicroWorkSubmitDto::getPreProcessName).findFirst();
        } else {
            processNameOptional = recordList.stream().map(MicroWorkSubmitDto::getOperateProcessName).findFirst();
        }
        if (!processNameOptional.isPresent()) {
            throw new CustomException("记录中不存在工序编码信息");
        }
        String processName = processNameOptional.get();
        detailCheckSubmitRecordByProduct.setProcessName(processName);
    }

    /**
     * 设置基础信息中的首尾序信息
     *
     */
    public void setFirstAndLastProcessFlag(DetailCheckSubmitRecordByProduct detailCheckSubmitRecordByProduct, List<MicroWorkSubmitDto> recordList)  {
        Optional<Long> isLastProcessOptional = recordList.stream().map(obj -> Long.parseLong(obj.getIsLastProcess())).min(Long::compareTo);
        if (!isLastProcessOptional.isPresent()) {
            throw new CustomException("记录中不存在工序尾序信息");
        }
        String isLastProcess = isLastProcessOptional.get().toString();
        detailCheckSubmitRecordByProduct.setIsLastProcess(isLastProcess);

        Optional<Long> isFirstProcessOptional = recordList.stream().map(obj -> Long.parseLong(obj.getIsFirstProcess())).min(Long::compareTo);
        if (!isFirstProcessOptional.isPresent()) {
            throw new CustomException("记录中不存在工序首序信息");
        }
        String isFirstProcess = isFirstProcessOptional.get().toString();
        detailCheckSubmitRecordByProduct.setIsFirstProcess(isFirstProcess);
    }

    /**
     * 针对非标准工艺的详细展示信息找到一个合适的顺序
     *
     */
    public List<DetailCheckSubmitRecordByProduct> findValidChainForNotStandardCraft(List<DetailCheckSubmitRecordByProduct> detailCheckSubmitRecordByProductList) {
        // 缓存
        LinkedList<DetailCheckSubmitRecordByProduct> cacheChain = new LinkedList<>();

        // 需要先找出一个头
        // processSeq在输入集合中是唯一的
        for (int i = 0; i < detailCheckSubmitRecordByProductList.size(); i++) {
            DetailCheckSubmitRecordByProduct record = detailCheckSubmitRecordByProductList.get(i);
            // 第一个对象用于初始化链表
            if (i == 0) {
                if (StringUtils.hasText(record.getPreProcessSeq())) {
                    // 前一个工序的实体, 前工序可能有多个
                    List<DetailCheckSubmitRecordByProduct> recordTempList = detailCheckSubmitRecordByProductList.stream()
                            .filter(obj -> record.getPreProcessSeq().contains(obj.getProcessSeq()))
                            .collect(Collectors.toList());
                    cacheChain.addAll(recordTempList);
                }
                cacheChain.add(record);
            } else {
                // 判断当前工序是否有前工序
                if (StringUtils.hasText(record.getPreProcessSeq())) {
                    // 前一个工序的实体, 前工序可能有多个
                    List<DetailCheckSubmitRecordByProduct> recordTempList = detailCheckSubmitRecordByProductList.stream()
                            .filter(obj -> record.getPreProcessSeq().contains(obj.getProcessSeq()))
                            .collect(Collectors.toList());

                    // 如果缓存中已经有了当前的对象，则考虑它是否有前工序
                    int index = cacheChain.indexOf(record);
                    // 缓存中没有该对象, 则加在链表的末尾
                    if (index == -1) {
                        // 先设置前工序
                        recordTempList.forEach(obj -> {
                            if (!cacheChain.contains(obj)) {
                                cacheChain.add(obj);
                            }
                        });
                        // 后设置当前工序
                        cacheChain.add(record);
                    } else {
                        recordTempList.forEach(obj -> {
                            int num = 0;

                            if (!cacheChain.contains(obj)) {
                                cacheChain.add(index + num, obj);
                                num++;
                            }
                        });
                    }
                } else {
                    // 判断缓存中是否已经有了当前对象
                    int index = cacheChain.indexOf(record);
                    if (index == -1) {
                        cacheChain.add(record);
                    }
                }
            }
        }
        return cacheChain;
    }

    /**
     * 产品维度审核列表 按照标准工艺的顺序方式进行排序并且设置基础信息
     *
     */
    public List<DetailCheckSubmitRecordByProduct> detailSubmitRecordInfoByProductFromSequentialStandardTech(List<DetailCheckSubmitRecordByProduct> detailCheckSubmitRecordByProductList,
                                                                                                            List<MicroProcessChainBindEntity> microProcessChainBindEntityList) {
        // 存储排好序的结果
        LinkedList<DetailCheckSubmitRecordByProduct> cacheChain = new LinkedList<>();
        // 过滤掉并序的多条记录, 需要再次排序一下
        List<MicroProcessChainBindEntity> sortTech = microProcessChainBindEntityList.stream().collect(Collectors
                .collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing(MicroProcessChainBindEntity::getProcessSeq))), ArrayList::new))
                .stream().sorted(Comparator.comparing(MicroProcessChainBindEntity::getSort)).collect(Collectors.toList());

        for (MicroProcessChainBindEntity tech : sortTech) {
            for (DetailCheckSubmitRecordByProduct detailCheckSubmitRecordByProduct : detailCheckSubmitRecordByProductList) {
                if (tech.getProcessSeq().equals(detailCheckSubmitRecordByProduct.getProcessSeq())) {
                    // 设置符合标准工艺的工序库存，根据标准工艺的首尾序信息
                    detailCheckSubmitRecordByProduct.setIsLastProcess(tech.getIsLastProcess());
                    // 首序
                    if (!StringUtils.isEmpty(tech.getParentProcessSeq()) && CommonConstants.ROOT_PROCESS_SEQ.equals(tech.getParentProcessSeq())) {
                        detailCheckSubmitRecordByProduct.setIsFirstProcess(CommonConstants.YES);
                    } else {
                        detailCheckSubmitRecordByProduct.setIsFirstProcess(CommonConstants.NO);
                    }
                    detailCheckSubmitRecordByProduct.setProcessCode(tech.getProcessCode());
                    detailCheckSubmitRecordByProduct.setProcessName(tech.getProcessName());
                    cacheChain.add(detailCheckSubmitRecordByProduct);
                }
            }
        }
        return cacheChain;
    }

    /**
     * 产品维度审核列表 按照标准工艺的乱序方式进行排序并且设置基础信息 中间序不需要考虑首尾序标示信息
     *
     *
     */
    public List<DetailCheckSubmitRecordByProduct> detailSubmitRecordInfoByProductFromNonSequentialStandardTech(List<DetailCheckSubmitRecordByProduct> detailCheckSubmitRecordByProductList,
                                                                                                               List<MicroProcessChainBindEntity> microProcessChainBindEntityList) {
        // 存储排好序的结果
        LinkedList<DetailCheckSubmitRecordByProduct> cacheChain = new LinkedList<>();

        // 顺序是按照sort排好的
        for (int i = 0; i < microProcessChainBindEntityList.size(); i++) {
            MicroProcessChainBindEntity entity = microProcessChainBindEntityList.get(i);
            for (DetailCheckSubmitRecordByProduct detailCheckSubmitRecordByProduct : detailCheckSubmitRecordByProductList) {
                if (entity.getProcessSeq().equals(detailCheckSubmitRecordByProduct.getProcessSeq())) {
                    detailCheckSubmitRecordByProduct.setProcessName(entity.getProcessName());
                    detailCheckSubmitRecordByProduct.setProcessCode(entity.getProcessCode());
                    // 首序可以通过sort == 0来判断
                    if (entity.getSort() == 0) {
                        detailCheckSubmitRecordByProduct.setIsFirstProcess(IsFirstProcessEnum.YES.getCode());
                        detailCheckSubmitRecordByProduct.setIsLastProcess(IsLastProcessEnum.NO.getCode());
                    }
                    // 尾序只有一个，可以通过index来判断
                    if (i == microProcessChainBindEntityList.size() - 1) {
                        detailCheckSubmitRecordByProduct.setIsFirstProcess(IsFirstProcessEnum.NO.getCode());
                        detailCheckSubmitRecordByProduct.setIsLastProcess(IsLastProcessEnum.YES.getCode());
                    }
                    cacheChain.add(detailCheckSubmitRecordByProduct);
                }
            }
        }
        return cacheChain;
    }
}
