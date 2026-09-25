/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroSimpleUserInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.entity.storage.StorageForProductAndProcess;
import com.cosmo.hhim.micro.base.domain.entity.submit.*;
import com.cosmo.hhim.micro.base.domain.entity.warn.DataHealth;
import com.cosmo.hhim.micro.base.domain.entity.warn.ProcessWarn;
import com.cosmo.hhim.micro.base.domain.entity.warn.ProductWarn;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroProductMapper;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessCommonMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessWarnMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroDataWarnService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.entity.MicroCapacityUpLimitEntity;
import com.cosmo.hhim.micro.infrastructure.enums.*;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroDataWarnServiceImpl implements IMicroDataWarnService {

    /** 修改记录数量阈值 */
    private static final int MODIFIED_RECORD_THRESHOLD = 3;
    /** 连续未报工天数阈值 */
    private static final int INACTIVE_DAYS_THRESHOLD = 7;
    /** 三天时间范围 */
    private static final long THREE_DAY_RANGE = 4;
    /** 连续天数阈值 */
    private static final int CONSECUTIVE_DAYS_THRESHOLD = 3;
    /** 报工数量预警倍数 */
    private static final long WARN_MULTIPLIER = 2L;
    /** 良品率阈值（低于历史良品率的95%则预警） */
    private static final BigDecimal PASS_RATE_THRESHOLD = new BigDecimal("0.95");

    @Autowired
    private MicroProcessWarnMapper microProcessWarnMapper;
    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;
    @Autowired
    private MicroWorkSubmitHistoryMapper microWorkSubmitHistoryMapper;
    @Autowired
    private MicroProcessCommonMapper processCommonMapper;
    @Autowired
    private MicroProductMapper productMapper;
    @Autowired
    private MicroUserMapper microUserMapper;
    @Autowired
    private MicroSupportUtil supportUtil;
    @Autowired
    private MicroProcessStorageMapper microProcessStorageMapper;
    @Autowired
    private RedisCache redisCache;

    /**
     * 过滤出有多道尾序的产品
     *
     */
    @Override
    public List<ProductWarn> selectMultiLastProcessForProduct() {
        List<ProductWarn> productWarns = microProcessWarnMapper.selectMultiLastProcessForProduct();
        if (CollectionUtils.isEmpty(productWarns)) {
            return productWarns;
        }

        // 过滤出尾工序大于1的产品
        List<ProductWarn> productWarnsByFilter = productWarns.stream().filter(p -> p.getProcessWarnList().size() > 1).collect(Collectors.toList());
        return productWarnsByFilter;
    }

    /**
     * 过滤出没有尾序的产品
     *
     */
    @Override
    public List<ProductWarn> selectNotHaveLastProcessForProduct() {
        return microProcessWarnMapper.selectNotHaveLastProcessForProduct();
    }

    /**
     * 主数据清理
     *
     */
    @Override
    public List checkMasterData(String masterType) {
        if (DataWarnTypeEnum.getEnum(masterType) == null) {
            return Collections.emptyList();
        }
        if (DataWarnTypeEnum.MASTER_PROCESS.getCode().equals(masterType)) {
            // 先获取所有报工/库存涉及的工序列表
            // process not in 以上的工序列表 查出来
            Set<String> processSeqs = microWorkSubmitMapper.selectAllProcess();
            Iterator<String> iterator = processSeqs.iterator();
            Set<String> addSet = new HashSet<>();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (StringUtils.isEmpty(next)) {
                    iterator.remove();
                    continue;
                }
                if (next.contains(",")) {
                    String[] split = next.split(",");
                    addSet.addAll(Arrays.stream(split).collect(Collectors.toSet()));
                    iterator.remove();
                }
            }
            processSeqs.addAll(addSet);
            return processCommonMapper.selectMasterProcessWarnList(processSeqs);
        } else if (DataWarnTypeEnum.MASTER_PRODUCT.getCode().equals(masterType)) {
            return productMapper.selectMasterProductWarnList();
        }
        return Collections.emptyList();
    }

    @Override
    public Long checkMasterDataCount(String masterType) {
        if (DataWarnTypeEnum.getEnum(masterType) == null) {
            return 0L;
        }
        if (DataWarnTypeEnum.MASTER_PROCESS.getCode().equals(masterType)) {
            return processCommonMapper.selectTotalProcessCount();
        } else if (DataWarnTypeEnum.MASTER_PRODUCT.getCode().equals(masterType)) {
            return productMapper.selectTotalProductCount();
        }
        return 0L;
    }


    /**
     * 过滤出没有首序的产品
     *
     */
    @Override
    public List<ProductWarn> selectNotHaveFirstProcessForProduct() {
        return microProcessWarnMapper.selectNotHaveFirstProcessForProduct();
    }

    /**
     * 工序异常 - 指定工序（首序、尾序）
     *
     * @param processWarnType 异常类型:  1. 无首序 2. 无尾序 3. 多尾序
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignFirstOrLastProcessToProduct(String productSeq, String processSeq, String processWarnType) { 
        // 打印参数
        log.info("请求入参为:productSeq:{},processSeq:{},processWarnType:{}", productSeq, processSeq, processWarnType);
        // 1. 校验参数
        if (CheckObjectUtils.isAnyEmpty(productSeq,
                processSeq,
                processWarnType)) {
            throw new CustomException("产品序列码、工序序列码和异常告警类型参数不能为空");
        }

        // 2. 找到该产品未审核记录的情况
        MicroWorkSubmit paramTempByIsFirstProcess = new MicroWorkSubmit();
        paramTempByIsFirstProcess.setProductSeq(productSeq);
        paramTempByIsFirstProcess.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectMicroWorkSubmitExList(paramTempByIsFirstProcess);
        if (CollectionUtils.isEmpty(microWorkSubmitDtoList)) {
//            throw new CustomException("该产品下无可更新的报工记录, 指定工序无效");
            return false;
        }

        // 存储因为数据治理造成的报工变动记录和更新的记录
        List<MicroWorkSubmit> microWorkSubmitListForUpdate = new ArrayList<>();
        List<MicroWorkSubmitHistory> microWorkSubmitHistoryListForInsert = new ArrayList<>();
        // 存储业务操作处理完成的所有报工记录信息
        List<MicroWorkSubmitDto> res = null;

        // 2. 判断异常类型
        // 3. 指定工序
        // 4. 生成变动历史记录，增加新的操作节点
        switch (processWarnType) {
            case CommonConstants.NOT_HAVE_FIRST_PROCESS_WARN_TYPE:
                // 业务操作
                // (1) 过滤出来需要进行更改的报工记录
                res = microWorkSubmitDtoList.stream()
                        .filter(obj -> obj.getIsFirstProcess().equals(IsFirstProcessEnum.YES.getCode()) || obj.getOperateProcessSeq().equals(processSeq))
                        .collect(Collectors.toList());

                // (2) 设定为首序的记录则设置相关字段
                if (CollectionUtils.isEmpty(res.stream().
                        filter(obj -> obj.getOperateProcessSeq().equals(processSeq))
                        .collect(Collectors.toList()))) {
                    return false;
                } else {
                    res.forEach(obj -> {
                        // 如果未审核记录中有已经设定的首序且并不属于指定的工序，则取消首序
                        if (obj.getIsFirstProcess().equals(IsFirstProcessEnum.YES.getCode())) {
                            obj.setIsFirstProcess(IsFirstProcessEnum.NO.getCode());
                        }

                        if (obj.getOperateProcessSeq().equals(processSeq)) {
                            // 指定首序
                            obj.setIsFirstProcess(IsFirstProcessEnum.YES.getCode());
                            // 如果要指定的首序存在前工序的话要置为空
                            if (CheckObjectUtils.isNotEmpty(obj.getPreProcessSeq())) {
                                obj.setPreProcessCode(null);
                                obj.setPreProcessName(null);
                                obj.setPreProcessSeq(null);
                                obj.setPreProcessGroup(null);
                            }
                        }
                    });
                }
                break;
            case CommonConstants.NOT_HAVE_LAST_PROCESS_WARN_TYPE:
                // (1) 过滤出来需要进行更改的报工记录
                res = microWorkSubmitDtoList.stream()
                        .filter(obj -> obj.getIsLastProcess().equals(IsLastProcessEnum.YES.getCode()) || obj.getOperateProcessSeq().equals(processSeq))
                        .collect(Collectors.toList());
                // (2) 处理相关业务
                if (CollectionUtils.isEmpty(res.stream()
                        .filter(obj -> obj.getOperateProcessSeq().equals(processSeq))
                        .collect(Collectors.toList()))) {
                    return false;
                } else {
                    res.forEach(obj -> {
                        // 取消尾序
                        if (obj.getIsLastProcess().equals(IsLastProcessEnum.YES.getCode())) {
                            obj.setIsLastProcess(IsLastProcessEnum.NO.getCode());
                        }
                        // 指定尾序
                        if (obj.getOperateProcessSeq().equals(processSeq)) {
                            obj.setIsLastProcess(IsLastProcessEnum.YES.getCode());
                        }
                    });
                }
                break;
            case CommonConstants.MULTI_LAST_PROCESS_WARN_TYPE:
                //（1）过滤出不符合指定工序且设置为最后一道工序的报工记录
                res = microWorkSubmitDtoList.stream()
                        .filter(obj -> obj.getIsLastProcess().equals(IsLastProcessEnum.YES.getCode()) && !obj.getOperateProcessSeq().equals(processSeq))
                        .collect(Collectors.toList());
                // (2) 处理业务
                if (!CollectionUtils.isEmpty(res)) {
                    res.forEach(obj -> {
                        obj.setIsLastProcess(IsLastProcessEnum.NO.getCode());
                    });
                } else {
                    return false;
                }
                break;
            default:
                break;
        }

        if (!CollectionUtils.isEmpty(res)) {
            copyRecordForUpdateOrInsert(res, microWorkSubmitListForUpdate, microWorkSubmitHistoryListForInsert);
        }
        // 复制对象 -> 更新的报工记录、插入的变动历史
        if (!CollectionUtils.isEmpty(microWorkSubmitListForUpdate)) {
            microWorkSubmitMapper.updateMicroWorkSubmitBatch(microWorkSubmitListForUpdate);
        }
        if (!CollectionUtils.isEmpty(microWorkSubmitHistoryListForInsert)) {
            microWorkSubmitHistoryMapper.insertMicroWorkSubmitHistoryBatch(microWorkSubmitHistoryListForInsert);
        }

        return true;
    }

    /**
     * 时间范围内报工十次且被纠正过数量的报工人
     * 时间范围设定在两周之内
     *
     */
    @Override
    public Set<Submitter> findModifiedRecordOverThreeTimesEmployees(Date startDate, Date endDate) {
        log.info("请求入参为：{}-{}", startDate, endDate);
        // 存放结果
        Set<Submitter> res = new HashSet<>();

        // 1.找出该租户下是员工的userId
        String appCode = remoteQueryAppCode(SecurityUtils.getApplicationSign());
        Map<String, MicroSimpleUserInfo> simpleUserInfosMap = microUserMapper.selectMicroUserByRoleCode(RoleCodeEnum.WORKER.getCode(), appCode);
        if (CollectionUtils.isEmpty(simpleUserInfosMap)) {
            return res;
        }
        Set<String> userIdList = simpleUserInfosMap.keySet();
        log.info("该租户下的报工人有:{}", simpleUserInfosMap.values().stream().map(MicroSimpleUserInfo::getUserName).collect(Collectors.toList()).toString());

        // 2.获取时间范围内的报工记录信息(审核完成)
        SubmitRecordQueryParam param = new SubmitRecordQueryParam();
        param.setStartDate(startDate);
        param.setEndDate(endDate);
        param.setSubmitType(supportUtil.getSubmitType());
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectSubmitRecordByUser(param);
        if (CollectionUtils.isEmpty(microWorkSubmitList)) {
            return res;
        }

        // 3.按照员工进行分组
        Map<String, List<MicroWorkSubmit>> collect = microWorkSubmitList.stream().collect(Collectors.groupingBy(MicroWorkSubmit::getSubmitUser));
        log.info("近期内有报工记录的员工为:{}", collect.keySet().toString());

        // 业务操作
        collect.keySet().forEach(obj -> {
            // 1. 报工角色  2.有待审核记录
            boolean hasUnapprovedRecords = microWorkSubmitList.stream()
                    .anyMatch(record -> record.getSubmitUser().equals(obj)
                            && SubmitStatusEnum.UN_APPROVE.getCode().equals(record.getSubmitStatus()));
            if (userIdList.contains(obj) && hasUnapprovedRecords) {
                List<MicroWorkSubmit> microWorkSubmitListTemp = collect.get(obj);

                // 已审核单据被纠正超过三次
                List<MicroWorkSubmit> workSubmitListTemp = microWorkSubmitListTemp
                        .stream()
                        .filter(record -> SubmitStatusEnum.APPROVED.getCode().equals(record.getSubmitStatus()))
                        .filter(record -> DataStatusEnum.CHANGED.getCode().equals(record.getDataStatus()))
                        .collect(Collectors.toList());

                if (workSubmitListTemp.size() >= MODIFIED_RECORD_THRESHOLD) {
                    Submitter submitterTemp = new Submitter();
                    submitterTemp.setSubmitNickUser(workSubmitListTemp.get(0).getSubmitNickName());
                    submitterTemp.setSubmitUser(simpleUserInfosMap.get(obj).getUserName());
                    submitterTemp.setModifiedNums(workSubmitListTemp.size());
                    res.add(submitterTemp);
                }
            }
        });
        return res;
    }

    /**
     * 时间范围内连续三天没有报工的工人
     *
     */
    @Override
    @Deprecated
    public Set<Submitter> findConsecutiveThreeDaysNoRecordEmployees(LocalDate startDate, LocalDate endDate) {
        log.info("请求入参为：{}-{}", startDate, endDate);
        // 校验传递的时间是否符合三天的范围
        if (endDate.toEpochDay() - startDate.toEpochDay() < THREE_DAY_RANGE) {
            throw new CustomException("时间范围小于三天");
        }
        // 存放结果
        Set<Submitter> res = new HashSet<>();

        // 1.找出该租户下是员工的userId
        String appCode = remoteQueryAppCode(SecurityUtils.getApplicationSign());
        Map<String, MicroSimpleUserInfo> simpleUserInfosMap = microUserMapper.selectMicroUserByRoleCode(RoleCodeEnum.WORKER.getCode(), appCode);
        if (CollectionUtils.isEmpty(simpleUserInfosMap)) {
            return res;
        }
        Set<String> userIdList = simpleUserInfosMap.keySet();
        log.info("该租户下的报工人有:{}", simpleUserInfosMap.values().stream().map(MicroSimpleUserInfo::getUserName).collect(Collectors.toList()).toString());

        // 2.查询时间范围内的所有人的报工信息(报工人、报工时间已去重)
        List<Submitter> submitterWithSubmitDay = microWorkSubmitMapper.findSubmitterWithSubmitDay(startDate, endDate, null);
        if (CollectionUtils.isEmpty(submitterWithSubmitDay)) {
            return res;
        }

        // 3.按照submit_user进行分组进行判断
        Map<String, List<Submitter>> collect = submitterWithSubmitDay.stream().collect(Collectors.groupingBy(Submitter::getSubmitUser));
        collect.keySet().forEach(obj -> {
            if (userIdList.contains(obj)) {
                Submitter temp = new Submitter();
                // 获取昵称
                String nickName = getNickName(obj);
                temp.setSubmitNickUser(nickName);
                temp.setSubmitUser(simpleUserInfosMap.get(obj).getUserName());
                // 业务需求判断
                List<Submitter> submitters = collect.get(obj);
                if (submitters.size() < CONSECUTIVE_DAYS_THRESHOLD) {
                    res.add(temp);
                } else {
                    // 连续三天的判断 (时间上已经排好序了) 窗口
                    for (int i = 1; i < submitters.size(); i++) {
                        Submitter preSubmitter = submitters.get(i - 1);
                        Submitter tempSubmitter = submitters.get(i);
                        // 如果前一天和当前的大于三天则加入结果
                        if (tempSubmitter.getSubmitDay().toEpochDay() - preSubmitter.getSubmitDay().toEpochDay() > THREE_DAY_RANGE) {
                            res.add(temp);
                        }
                    }
                    // 以传递参数的最后一天为终止，在判断一下每个报工人的报工时间是否符合三天的规则
                    if (endDate.toEpochDay() - submitters.get(submitters.size() - 1).getSubmitDay().toEpochDay() > THREE_DAY_RANGE) {
                        res.add(temp);
                    }
                }
            }
        });
        return res;
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
     * 如果时间范围内没有报工记录的员工也就是7天内没有报工记录的员工
     *
     */
    @Override
    public Set<Submitter> findConsecutiveSevenDaysNoRecordEmployees() {
        // 存储结果
        Set<Submitter> res = new HashSet<>();
        // 获取当前时间
        LocalDate currentDate = LocalDate.now();

        // 1.找出该租户下是员工的userId
        String appCode = remoteQueryAppCode(SecurityUtils.getApplicationSign());
        Map<String, MicroSimpleUserInfo> simpleUserInfosMap = microUserMapper.selectMicroUserByRoleCode(RoleCodeEnum.WORKER.getCode(), appCode);
        if (CollectionUtils.isEmpty(simpleUserInfosMap)) {
            return res;
        }
        Set<String> userIdList = simpleUserInfosMap.keySet();
        log.info("该租户下的报工人有:{}", simpleUserInfosMap.values().stream().map(MicroSimpleUserInfo::getUserName).collect(Collectors.toList()).toString());

        // 2.查询时间范围内的所有人的报工信息(报工人、报工时间已去重)
        List<Submitter> submitterWithSubmitDay = microWorkSubmitMapper.findSubmitterWithSubmitDay(null, currentDate, null);
        if (CollectionUtils.isEmpty(submitterWithSubmitDay)) {
            return res;
        }
        log.info("报工的人有:{}", submitterWithSubmitDay.stream().map(Submitter::getSubmitUser).distinct().collect(Collectors.toList()).toString());
        // 按照submitUser进行分组
        Map<String, List<Submitter>> collect = submitterWithSubmitDay.stream().collect(Collectors.groupingBy(Submitter::getSubmitUser));

        // 3.业务操作
        collect.keySet().forEach(obj -> {
            // 属于员工的报工记录
            if (userIdList.contains(obj)) {
                List<Submitter> submitterListTemp = collect.get(obj);
                // 按照提报时间倒序
                submitterListTemp.sort((t1, t2) -> t2.getSubmitDay().compareTo(t1.getSubmitDay()));
                // 获取提报人最后的提报时间
                LocalDate lastSubmitDay = submitterListTemp.get(0).getSubmitDay();
                Long dayGap = currentDate.toEpochDay() - lastSubmitDay.toEpochDay();
                if (dayGap >= INACTIVE_DAYS_THRESHOLD) {
                    Submitter submitterTemp = new Submitter();
                    // 获取昵称
                    String nickName = getNickName(obj);
                    submitterTemp.setSubmitNickUser(nickName);
                    submitterTemp.setSubmitUser(simpleUserInfosMap.get(obj).getUserName());
                    submitterTemp.setNotSubmitDayNums(dayGap);
                    res.add(submitterTemp);
                }
                userIdList.remove(obj);
            }
        });
        // 创建了角色但是从没有报工过的人
        if (!CollectionUtils.isEmpty(userIdList)) {
            // 要根据用户的创建时间过滤一遍
            List<MicroUser> microUserList = microUserMapper.selectMicroUserListByUserIdList(userIdList);
            if (CollectionUtils.isEmpty(microUserList)) {
                throw new CustomException("未查询到" + userIdList.toString() + "的用户信息");
            }
            Map<Long, List<MicroUser>> microUserMap = microUserList.stream().collect(Collectors.groupingBy(MicroUser::getId));
            // 创建日期和当前时间相差7天的则加入到结果中
            userIdList.forEach(obj -> {
                // 只有一条
                List<MicroUser> microUsers = microUserMap.get(Long.valueOf(obj));
                // date -> localDate
                LocalDate createDate = microUsers.get(0).getCreatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                Long timeGap = currentDate.toEpochDay() - createDate.toEpochDay();
                if (timeGap >= INACTIVE_DAYS_THRESHOLD) {
                    Submitter submitterTemp = new Submitter();
                    submitterTemp.setSubmitUser(simpleUserInfosMap.get(obj).getUserName());
                    // 获取昵称
                    String nickName = getNickName(obj);
                    submitterTemp.setSubmitNickUser(nickName);
                    submitterTemp.setNotSubmitDayNums(timeGap);
                    res.add(submitterTemp);
                }
            });
        }
        return res;
    }

    /**
     * 超出日产能的员工
     *
     */
    @Override
    public List<Submitter> findOverDayCapacityEmployee() {
        // 存储结果
        List<Submitter> submitterList = new ArrayList<>();

        // 1. 是否要过滤出来只是工人的报工
        String appCode = remoteQueryAppCode(SecurityUtils.getApplicationSign());
        Map<String, MicroSimpleUserInfo> simpleUserInfosMap = microUserMapper.selectMicroUserByRoleCode(RoleCodeEnum.WORKER.getCode(), appCode);
        if (CollectionUtils.isEmpty(simpleUserInfosMap)) {
            return submitterList;
        }
        Set<String> userIdList = simpleUserInfosMap.keySet();
        log.info("该租户下的报工人有:{}", simpleUserInfosMap.values().stream().map(MicroSimpleUserInfo::getUserName).collect(Collectors.toList()).toString());

        // 2. 找出当天到人的报工记录并获取日产能
        List<ProductiveCapacityByDay> productiveCapacityByDayList = microWorkSubmitMapper.obtainedProductiveCapacityByDayForEmployee(LocalDate.now());

        // 3. 获取工厂维度下的人均日产能（如果日产能为空, 说明还没有审核过的报工记录）
        BigDecimal avgDayProductiveCapacityForEmployee = microWorkSubmitMapper.obtainedAverageDayProductiveCapacityForEmployeeFromFactory();
        if (avgDayProductiveCapacityForEmployee == null) {
            return submitterList;
        }
        log.info("该工厂的人均日均产能为:{}", avgDayProductiveCapacityForEmployee.toString());

        // 4. 根据日产能判断报工人是否存在问题
        if (avgDayProductiveCapacityForEmployee.signum() == 0) {
            return submitterList;
        }
        productiveCapacityByDayList.stream()
                .filter(obj -> {
                    // 1.报工角色  2.大于1.1被的平均日产能  3.有待审核记录
                    MicroWorkSubmit param = new MicroWorkSubmit();
                    param.setSubmitUser(obj.getSubmitUser());
                    param.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());
                    return userIdList.contains(obj.getSubmitUser())
                            && obj.getTotalNum().compareTo(avgDayProductiveCapacityForEmployee.multiply(BigDecimal.valueOf(1.1))) > 0
                            && microWorkSubmitMapper.selectMicroWorkSubmitList(param).size() > 0;
                })
                .forEach(obj -> {
                    Submitter submitterTemp = new Submitter();
                    submitterTemp.setSubmitUser(simpleUserInfosMap.get(obj.getSubmitUser()).getUserName());
                    submitterTemp.setSubmitNickUser(getNickName(obj.getSubmitUser()));
                    submitterTemp.setAvgDayProductiveCapacity(avgDayProductiveCapacityForEmployee);
                    submitterList.add(submitterTemp);
                });
        return submitterList;
    }

    /**
     * 检查报工数量是否符合规范
     *
     */
    @Override
    public String checkSubmitNum(BigDecimal totalSubmitNum, String submitUser, String productSeq, String processSeq) {
        BigDecimal avgSubmitNum = microWorkSubmitMapper.getAvgSubmitNumForUserAndProductAndProcess(submitUser, productSeq, processSeq);
        // 如果数量为null
        // 1. 报工审核次数不到10次
        // 2. 人、产品和工序没有报工信息
        if (avgSubmitNum == null) {
            return null;
        } else {
            if (avgSubmitNum.multiply(BigDecimal.valueOf(WARN_MULTIPLIER)).compareTo(totalSubmitNum) < 0) {
                return "本次报工产品合计数量超历史报工数量的2倍";
            } else if (totalSubmitNum.multiply(BigDecimal.valueOf(WARN_MULTIPLIER)).compareTo(avgSubmitNum) < 0) {
                return "本次报工产品数量低于历史报工数量的1/2";
            } else {
                return "符合报工数量安全范围值";
            }
        }
    }

    /**
     * 计算数据健康度
     *
     */
    @Override
    public DataHealth calculateDataHealth() {
        DataHealth res = new DataHealth();
        // 1. 找到总的未审核报工记录
        MicroWorkSubmit param = new MicroWorkSubmit();
        param.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectMicroWorkSubmitExList(param);
        // 不存在未审核的报工记录
        if (CollectionUtils.isEmpty(microWorkSubmitDtoList)) {
            res.setTotalWaitCheckSubmitRecords(0);
            res.setAbnormalSubmitRecords(0);
            res.setDataHealth(BigDecimal.valueOf(100));
            return res;
        }

        // 2. 获取报工的条数
        int totalWaitCheckSubmitRecords = microWorkSubmitDtoList.size();
        res.setTotalWaitCheckSubmitRecords(totalWaitCheckSubmitRecords);

        // 3. 判断每条报工记录的异常： 1. 产品有多个尾序 2. 异常标签
        Set<String> productAndProcessSeqSet = new HashSet<>();
        List<ProductWarn> productWarns = this.selectMultiLastProcessForProduct();
        // 多尾序异常: 通过productSeq + processSeq来寻找异常未审核记录
        for (ProductWarn productWarn : productWarns) {
            List<ProcessWarn> processWarnList = productWarn.getProcessWarnList();
            if (!CollectionUtils.isEmpty(processWarnList)) {
                for (ProcessWarn processWarn : processWarnList) {
                    productAndProcessSeqSet.add(productWarn.getWarnProductSeq() + "_" + processWarn.getWarnProcessSeq());
                }
            }
        }

        // 查找异常的数据
        int temp = 0;
        int negativeStockRecords = 0;
        int lowPassRateRecords = 0;
        int overProductiveCapacityRecords = 0;
        StringBuilder negativeStockRecordIds = new StringBuilder();
        StringBuilder lowPassRateRecordIds = new StringBuilder();
        StringBuilder overProductiveCapacityRecordIds = new StringBuilder();


        // 获取产品 + 工序的良品率和产能
        Map<String, Map<String, Object>> warningMetrics = supportUtil.getWarningMetrics();
        // 查询全部库存情况 -> 避免单个查询速度过慢
        Map<String, StorageForProductAndProcess> storageMap = microProcessStorageMapper.selectStorageByProductAndProcess();
        // 还要找当前产品 + 该工序的未审核的报工良品数量
        Map<String, CountForProductAndProcess> countForAllProductAndProcessMap = microWorkSubmitMapper.obtainedCountForAllProductAndProcess(SubmitStatusEnum.UN_APPROVE.getCode());
        // 产品 + 工序 + 报工人 + 报工日期的报工数量（审核和未审核）
        Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition = microWorkSubmitMapper.obtainedTotalSubmitNumByCondition();

        // 业务操作
        for (MicroWorkSubmitDto obj : microWorkSubmitDtoList) {
            // 异常判断
            this.singleSubmitRecordExceptionJudgement(obj, warningMetrics, storageMap, countForAllProductAndProcessMap, allSubmitTotalNumByCondition);
            // 有任何一个异常标签都是异常数据
            if (obj.getNegativeStockFlag().equals(NegativeStockFlagEnum.NEGATIVE_STOCK.getCode())
                    || obj.getLowPassRateFlag().equals(LowPassRateFlagEnum.LOW.getCode())
                    || obj.getOverProductiveCapacityFlag().equals(OverProductiveCapacityFlagEnum.OVER.getCode())
                    //产品有多个尾序
                    || productAndProcessSeqSet.contains(obj.getProductSeq() + "_" + obj.getOperateProcessSeq())) {
                temp += 1;
            }

            // 改变原有判断逻辑 -> 纯粹是为了想获取具体异常记录数
            if (obj.getNegativeStockFlag().equals(NegativeStockFlagEnum.NEGATIVE_STOCK.getCode())) {
                negativeStockRecords += 1;
                negativeStockRecordIds.append(obj.getId()).append(",");
            }
            if (obj.getLowPassRateFlag().equals(LowPassRateFlagEnum.LOW.getCode())) {
                lowPassRateRecords += 1;
                lowPassRateRecordIds.append(obj.getId()).append(",");
            }
            if (obj.getOverProductiveCapacityFlag().equals(OverProductiveCapacityFlagEnum.OVER.getCode())) {
                overProductiveCapacityRecords += 1;
                overProductiveCapacityRecordIds.append(obj.getId()).append(",");
            }
        }

        // 4. 获取异常的报工记录数
        res.setAbnormalSubmitRecords(temp);
        // 5. 计算健康度
        // 待审记录数为 0 时无法计算（原实现直接 divide(0) → 数据健康接口 500）：没有待审即没有异常，按 100% 处理
        if (totalWaitCheckSubmitRecords == 0) {
            res.setDataHealth(BigDecimal.ONE);
        } else {
            res.setDataHealth(BigDecimal.valueOf(totalWaitCheckSubmitRecords).subtract(BigDecimal.valueOf(temp))
                    .divide(BigDecimal.valueOf(totalWaitCheckSubmitRecords), 4, RoundingMode.HALF_DOWN));
        }
        // 6. 设置记工的具体异常数值
        res.setNegativeStockRecords(negativeStockRecords);
        res.setLowPassRateRecords(lowPassRateRecords);
        res.setOverProductiveCapacityRecords(overProductiveCapacityRecords);
        // 7. 设置异常对应的报工记录id
        res.setNegativeStockRecordIds(negativeStockRecordIds.length() > 0 ? negativeStockRecordIds.deleteCharAt(negativeStockRecordIds.length() - 1).toString() : "");
        res.setLowPassRateRecordIds(lowPassRateRecordIds.length() > 0 ? lowPassRateRecordIds.deleteCharAt(lowPassRateRecordIds.length() - 1).toString() : "");
        res.setOverProductiveCapacityRecordIds(overProductiveCapacityRecordIds.length() > 0 ? overProductiveCapacityRecordIds.deleteCharAt(overProductiveCapacityRecordIds.length() - 1).toString() : "");

        return res;
    }

    /**
     * 获取要更新的报工记录和要插入的报工变动历史
     *
     */
    public void copyRecordForUpdateOrInsert(List<MicroWorkSubmitDto> microWorkSubmitDtoList, List<MicroWorkSubmit> microWorkSubmitListForUpdate, List<MicroWorkSubmitHistory> microWorkSubmitHistoryListForInsert) {

        microWorkSubmitDtoList.stream().forEach(obj -> {
            // 1.copy报工记录对象
            MicroWorkSubmit microWorkSubmitTemp = new MicroWorkSubmit();
            BeanUtils.copyProperties(obj, microWorkSubmitTemp);
            microWorkSubmitListForUpdate.add(microWorkSubmitTemp);

            // 2.copy变动记录对象
            MicroWorkSubmitHistory microWorkSubmitHistoryTemp = new MicroWorkSubmitHistory();
            BeanUtils.copyProperties(obj, microWorkSubmitHistoryTemp);
            microWorkSubmitHistoryTemp.setOperateNode(CommonConstants.SUBMIT_HISTORY_RECORDS_DATA_GOVERNANCE);
            microWorkSubmitHistoryTemp.setCreatedDate(DateUtils.getNowDate());
            microWorkSubmitHistoryTemp.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
            microWorkSubmitHistoryListForInsert.add(microWorkSubmitHistoryTemp);
        });
    }

    /**
     * 单条报工记录的异常判断
     *
     */
    public void singleSubmitRecordExceptionJudgement(MicroWorkSubmitDto microWorkSubmitDto, Map<String, Map<String, Object>> warningMetrics,
                                                     Map<String, StorageForProductAndProcess> storageMap,
                                                     Map<String, CountForProductAndProcess> countForAllProductAndProcessMap,
                                                     Map<String, TotalSubmitNumByCondition> allSubmitTotalNumByCondition) {

        // 1. 负库存风险判断, 有多个前工序，只要其中一个为负数就设置该标示
        // 前工序的库存 + 未审核数
        List<BigDecimal> preProcessTotalNumList = new ArrayList<>();
        if (StringUtils.hasText(microWorkSubmitDto.getPreProcessSeq())) {
            List<String> preProcessSeqList = Arrays.stream(microWorkSubmitDto.getPreProcessSeq().split(",")).collect(Collectors.toList());
            for (String preProcessSeqKey : preProcessSeqList) {
                // 查询该产品+前工序是否已经有库存
                StorageForProductAndProcess storageForProductAndProcessTemp = storageMap.get(microWorkSubmitDto.getProductSeq() + "_" + preProcessSeqKey);
                // 当前产品 + 该工序的未审核的报工良品数量
                CountForProductAndProcess countForProductAndProcessTemp = countForAllProductAndProcessMap.get(microWorkSubmitDto.getProductSeq() + "_" + preProcessSeqKey);
                // 存储计算之后的数量
                BigDecimal preProcessTotalNum;
                // 计算前工序的报工数量
                if (storageForProductAndProcessTemp == null && countForProductAndProcessTemp == null) {
                    preProcessTotalNum = BigDecimal.ZERO;
                } else if (storageForProductAndProcessTemp == null && countForProductAndProcessTemp != null) {
                    preProcessTotalNum = countForProductAndProcessTemp.getTotalNum();
                } else if (storageForProductAndProcessTemp != null && countForProductAndProcessTemp == null) {
                    preProcessTotalNum = storageForProductAndProcessTemp.getPassNum();
                } else {
                    preProcessTotalNum = countForProductAndProcessTemp.getTotalNum().add(storageForProductAndProcessTemp.getPassNum());
                }
                preProcessTotalNumList.add(preProcessTotalNum);
            }
        }
        // 负库存标签
        // 口径：只有良品在工序间流转，判断"会不会扣成负库存"只比良品（原来把不良也算进去 → 误报）
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
        // 如果为空，说明没有形成库存，也就没有产能和良品率
        if (CheckObjectUtils.isNotEmpty(warningMetricsBySeq)) {
            BigDecimal totalNumForRate = microWorkSubmitDto.getPassNum().add(microWorkSubmitDto.getNgNum());
            // 报工数为 0 时算不出良品率（原实现直接 divide(0) → 数据预警接口 500）→ 按"正常"处理并跳过比值判断
            if (totalNumForRate.signum() == 0) {
                log.warn("[预警] 产品{} 工序{} 报工数为0，跳过良品率判断",
                        microWorkSubmitDto.getProductSeq(), microWorkSubmitDto.getOperateProcessSeq());
                microWorkSubmitDto.setLowPassRateFlag(LowPassRateFlagEnum.NORMAL.getCode());
            } else {
                BigDecimal passRateTemp = microWorkSubmitDto.getPassNum().divide(totalNumForRate, 4, RoundingMode.DOWN);
                BigDecimal passRate = new BigDecimal(warningMetricsBySeq.get(DataWarnTypeEnum.PASS_RATE.getCode()).toString());
                if (passRateTemp.compareTo(passRate.multiply(PASS_RATE_THRESHOLD)) < 0) {
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
        if (totalSubmitNumByCondition != null && totalSubmitNumByCondition.getTotalSubmitNum() != null) {
            totalNumTemp = totalSubmitNumByCondition.getTotalSubmitNum();
        }

        MicroCapacityUpLimitEntity upLimitEntity = new MicroCapacityUpLimitEntity.Builder()
                .withOperateProcessSeq(microWorkSubmitDto.getOperateProcessSeq())
                .withProductSeq(microWorkSubmitDto.getProductSeq())
                .withUserId(microWorkSubmitDto.getSubmitUser())
                .build();
        BigDecimal totalNum = supportUtil.getCapacityUpLimit(upLimitEntity);
        BigDecimal upperThreshold = BigDecimal.valueOf(1.1);
        if (totalNumTemp.compareTo(totalNum.multiply(upperThreshold)) > 0) {
            microWorkSubmitDto.setOverProductiveCapacityFlag(OverProductiveCapacityFlagEnum.OVER.getCode());
        } else {
            microWorkSubmitDto.setOverProductiveCapacityFlag(OverProductiveCapacityFlagEnum.NOT_OVER.getCode());
        }
    }

    /**
     * 调用三方平台根据应用标识查询应用编码
     *
     */
    public String remoteQueryAppCode(String appSign) {
        Map<String, Object> cacheMap = redisCache.getCacheMap(CommonConstants.REDIS_APP_SIGN_CODE_MAPPING_KEY);
        return CollectionUtils.isEmpty(cacheMap) ? "" : (String) cacheMap.get(appSign);
    }
}
