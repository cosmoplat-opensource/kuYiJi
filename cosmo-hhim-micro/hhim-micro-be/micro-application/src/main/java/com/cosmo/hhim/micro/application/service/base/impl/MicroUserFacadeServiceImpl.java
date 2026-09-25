/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.Sha256Utils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.base.MicroUserEventTrackingAssembler;
import com.cosmo.hhim.micro.application.dto.base.MicroUserDTO;
import com.cosmo.hhim.micro.application.dto.base.MicroUserRecommendButtonDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroUserFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.*;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTenantIndividuationConfigService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserIndividuationConfigService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
import com.cosmo.hhim.micro.infrastructure.config.MicroStatisticsConfig;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.enums.RoleCodeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.PersonalizedOptionEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.UserRecommendButtonEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.UserRecommendPageEnum;
import com.cosmo.hhim.micro.infrastructure.util.UserLoginStatisticsUtil;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserEventTrackingEntity;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserMostUseButtonEntity;
import com.cosmo.hhim.micro.integration.domain.service.IMicroUserEventTrackingService;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.security.SecureRandom;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroUserFacadeServiceImpl implements IMicroUserFacadeService {

    @Autowired
    private MicroUserMapper microUserMapper;
    @Autowired
    private IMicroUserService microUserService;
    @Autowired
    private IMicroTenantIndividuationConfigService tenantConfigService;
    @Autowired
    private IMicroUserIndividuationConfigService userConfigService;
    @Autowired
    private IMicroUserEventTrackingService trackingService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private MicroStatisticsConfig statisticsConfig;

    /** 安全的随机数源（用于超时时间随机抖动） */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 缓存所有租户下用户信息
     *
     * @param signAndCodeMap app_sign(k)和app_code(v)对应关系
     */
    @Override
    public void cacheAllCustomerUserInfo(Map<String, String> signAndCodeMap, String customerCode) {
        List<MicroUser> users = microUserMapper.selectAllUserCompleteInfo();
        if (!CollectionUtils.isEmpty(users)) {
            for (MicroUser user : users) {
                this.cacheUserInfo(customerCode, user, signAndCodeMap);
            }
        }
    }

    /**
     * 多个微应用多次缓存
     *
     * @param customerCode 租户编码
     * @param user         用户信息
     * @param configMap    app_sign(k)和app_code(v)对应关系（方法参数传递，避免单例共享可变字段导致的并发/跨租户串数据问题）
     */
    private void cacheUserInfo(String customerCode, MicroUser user, Map<String, String> configMap) {
        List<MicroUserAppRe> appRes = user.getMicroUserAppRes();
        if (!CollectionUtils.isEmpty(appRes)) {
            for (MicroUserAppRe appRe : appRes) {
                String appCode = appRe.getAppCode();
                String appSign = configMap.get(appCode);
                if (appSign != null) {
                    redisCache.setCacheObject(CommonConstants.USER_CACHE_INFO + appSign + ":" + customerCode + ":" + user.getId(),
                            user, (int) calcTimeout(), TimeUnit.SECONDS);
                }
            }
        }
    }

    /**
     * 计算到今日结束的时间差
     *
     * @return 时间差 秒级
     */
    private static long calcTimeout() {
        long between = DateUtil.between(new Date(), DateUtil.endOfDay(new Date()), DateUnit.SECOND);
        return between + SECURE_RANDOM.nextInt(600) + 60;
    }

    @Override
    public int editUser(MicroUserEditEntity microUser) {
        Long editId = microUser.getId();
        if (editId == null) {
            throw new CustomException("无法获取用户信息");
        }
        if (RoleCodeEnum.QUALITY_INSPECTOR.getCode().equals(microUser.getRoleCode())) {
            MicroTenantIndividuationConfig config = tenantConfigService.selectMicroTenantIndividuationConfigByTenant();
            String openQC = "1"; 
            if (config == null || openQC.equals(config.getSubmitInspectSwitch())) {
                throw new CustomException("如需配置质检员角色，请联系企业管理员启用送检！");
            }
        }
        return microUserService.updateMicroUser(microUser);
    }

    /**
     * 获取用户完整信息
     *
     * @param username
     * @return
     */
    @Override
    public MicroUserDTO getUserCompleteInfo(String username) {
        MicroUser user = microUserService.findMicroUserCompleteInfo(username, null);
        return this.personalizedRecommendation(user);
    }

    /**
     * 是否个性化推荐或弹窗
     *
     * @param microUser
     */
    private MicroUserDTO personalizedRecommendation(MicroUser microUser) {
        MicroUserDTO dto = BeanUtil.copyProperties(microUser, MicroUserDTO.class);
        if (microUser != null) {
            Long userId = microUser.getId();
            try {
                // 先查询是否存在个性化配置,空值区分是否14天,14天以后的配置2,14天以内不配置
                // 有值,正常返回值
                MicroUserIndividuationConfig userConfig = userConfigService.selectUserConfigByUserId(userId);
                if (userConfig == null) {
                    int loginDays = UserLoginStatisticsUtil.countUserLoginDays(userId);
                    if (loginDays < statisticsConfig.getPersonalizedThreshold()) {
                        dto.setPersonalRecommend(PersonalizedOptionEnum.DISPLAY.getCode());
                    } else {
                        dto.setPersonalRecommend(PersonalizedOptionEnum.SHOW_MODAL.getCode());
                    }
                } else {
                    dto.setPersonalRecommend(userConfig.getPersonalized());
                }
                UserLoginStatisticsUtil.recordUserLogin(userId);
            } catch (Exception e) {
                log.error("Failed to record login and personalized recommendation:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            }
        }
        return dto;
    }

    /**
     * 是否开启个人推荐
     * ⭐️这里的insert into 使用的是 replace into
     * ⭐️因为表中唯一索引设置的是user_id, tenant_code
     *
     * @param option
     * @return
     */
    @Override
    public int enablePersonalized(Integer option) {
        validOption(option);
        return userConfigService.insertMicroUserIndividuationConfig(convertUserConfigQuery(option));
    }

    private MicroUserIndividuationConfig convertUserConfigQuery(Integer option) {
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        MicroUserIndividuationConfig config = new MicroUserIndividuationConfig();
        config.setUserId(userId);
        config.setPersonalized(option);
        config.setTenantCode(tenantCode);
        return config;
    }

    private void validOption(Integer option) {
        if (!option.equals(PersonalizedOptionEnum.DISABLE.getCode()) && !option.equals(PersonalizedOptionEnum.ENABLE.getCode())) {
            throw new CustomException("无法获取个性化推荐信息");
        }
    }

    /**
     * 根据pageType找到根据用户使用情况,推荐出的页面链接
     *
     * @param pageType
     * @return
     */
    @Override
    public String findUserPersonalizedRecommendPage(String pageType) {
        String initPage = "index";
        if (UserRecommendPageEnum.getEnum(pageType) == null) {
            return initPage;
        }
        Long userId = SecurityUtils.getUserId();
        MicroUserIndividuationConfig userConfig = userConfigService.selectUserConfigByUserId(userId);
        Map<String, Double> usingTimesMap = new HashMap<>(8);
        List<String> sortList = new ArrayList<>();
        switch (UserRecommendPageEnum.getEnum(pageType)) {
            case INDEX:
                usingTimesMap = trackingService.getUsingTimesByRefCondition(MicroUserEventTrackingAssembler.assembleRefCondition(statisticsConfig, userConfig));
                sortList = statisticsConfig.getNavPages();
                break;
            case CHECK_DIMENSIONS:
                usingTimesMap = trackingService.getUsingTimesByCurrentCondition(MicroUserEventTrackingAssembler.assembleCurrentPageCondition(statisticsConfig, userConfig));
                sortList = statisticsConfig.getCurrentPages();
                break;
            default:
                break;
        }
        return sortResultMap(usingTimesMap, sortList);
    }

    /**
     * 对map<页面,分值>进行排序,如果分值相同,按照配置文件中的顺序排列
     * 最终拿到最终结果
     *
     * @param usingTimesMap map<页面,分值>
     * @param sortList      配置文件中的顺序
     * @return
     */
    private String sortResultMap(Map<String, Double> usingTimesMap, List<String> sortList) {
        if (CollectionUtils.isEmpty(sortList)) {
            sortList = new ArrayList<>();
        }
        ImmutableList<String> build = ImmutableList.<String>builder().addAll(sortList).build();
        return usingTimesMap.entrySet().stream()
                .max(Comparator.comparing(Map.Entry<String, Double>::getValue)
                        .thenComparingInt(o -> build.indexOf(o.getKey())))
                .map(Map.Entry::getKey).orElse("");
    }

    /**
     * 根据用户输入的报工数据找到根据用户使用情况,推荐出的按钮(记工/送检)
     *
     * @param dataList
     * @return
     */
    @Override
    public List<String> findUserPersonalizedRecommendButton(List<MicroUserRecommendButtonDTO> dataList) {
        List<String> buttonList = new ArrayList<>();
        if (CollectionUtils.isEmpty(dataList)) {
            return buttonList;
        }
        // 是否开启送检
        MicroTenantIndividuationConfig config = tenantConfigService.selectMicroTenantIndividuationConfigByTenant();
        String submitInspectSwitch = config.getSubmitInspectSwitch();
        String disableSwitch = "1";
        if (disableSwitch.equals(submitInspectSwitch)) {
            buttonList.add(UserRecommendButtonEnum.SUBMIT.getDesc());
            return buttonList;
        }
        return trackingService.findUserPersonalizedRecommendButton(assembleButtonEntity(dataList, config));
    }

    /**
     * 组装查询参数
     *
     * @param dataList
     * @param config
     * @return
     */
    private MicroUserMostUseButtonEntity assembleButtonEntity(List<MicroUserRecommendButtonDTO> dataList, MicroTenantIndividuationConfig config) {
        MicroUserMostUseButtonEntity buttonEntity = new MicroUserMostUseButtonEntity();
        List<String> eventCodeList = convertEventCodeList();
        MicroUserEventTrackingEntity trackingEntity = new MicroUserEventTrackingEntity();
        trackingEntity.setEventType("CLICK");
        trackingEntity.setApplicationSign(SecurityUtils.getApplicationSign());
        trackingEntity.setCreatedDate(config.getCreatedDate());
        Set<String> eventContentList = convertDataList(dataList);
        buttonEntity.setConditionEntity(trackingEntity);
        buttonEntity.setUserId(SecurityUtils.getUserId());
        buttonEntity.setEventCodeList(eventCodeList);
        buttonEntity.setEventContentList(new ArrayList<>(eventContentList));
        return buttonEntity;
    }

    private List<String> convertEventCodeList() {
        List<String> codeList = new ArrayList<>();
        // SHA-256 事件编码取前 32 位(与前端 sha256Short 截断口径一致,兼容 event_code varchar(40))
        codeList.add(Sha256Utils.sha256Hex(UserRecommendButtonEnum.SUBMIT.getDesc(), Sha256Utils.ENCODE).substring(0, 32));
        codeList.add(Sha256Utils.sha256Hex(UserRecommendButtonEnum.QC.getDesc(), Sha256Utils.ENCODE).substring(0, 32));
        return codeList;
    }

    private Set<String> convertDataList(List<MicroUserRecommendButtonDTO> dataList) {
        return dataList.stream().map(d -> d.getProductSeq() + "&" + d.getOperateProcessSeq()).collect(Collectors.toSet());
    }

}
