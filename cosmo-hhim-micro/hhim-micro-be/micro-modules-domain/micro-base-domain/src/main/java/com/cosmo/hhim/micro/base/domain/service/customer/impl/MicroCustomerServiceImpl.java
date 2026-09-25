/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.customer.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.pojo.LoginUser;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.customer.MicroCustomer;
import com.cosmo.hhim.micro.base.domain.mapper.customer.MicroCustomerMapper;
import com.cosmo.hhim.micro.base.domain.service.customer.IMicroCustomerService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 客户基础Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 */
@Service
public class MicroCustomerServiceImpl implements IMicroCustomerService
{
    @Autowired
    private MicroCustomerMapper microCustomerMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 查询客户基础
     * 
     * @param id 客户基础ID
     * @return 客户基础
     */
    @Override
    public MicroCustomer selectMicroCustomerById(Long id)
    {
        return microCustomerMapper.selectMicroCustomerById(id);
    }

    /**
     * 查询客户基础列表
     * 
     * @param microCustomer 客户基础
     * @return 客户基础
     */
    @Override
    public List<MicroCustomer> selectMicroCustomerList(MicroCustomer microCustomer)
    {
        return microCustomerMapper.selectMicroCustomerList(microCustomer);
    }

    /**
     * 新增客户基础
     * 
     * @param microCustomer 客户基础
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMicroCustomer(MicroCustomer microCustomer)
    {
        //生成客户编码
        generateCustomerCode(microCustomer);
        return microCustomerMapper.insertMicroCustomer(microCustomer);
    }

    /**
     * @author cosmo-hhim-open Team
     * @param customerName
     * @return boolean
     **/
    @Override
    public boolean isUniqueCustomerName(String customerName) {
        if (StringUtils.isEmpty(customerName)){
            return true;
        }
        MicroCustomer param = new MicroCustomer();
        param.setCustomerName(customerName);
        param.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        return CollectionUtil.isEmpty(microCustomerMapper.selectMicroCustomerListWithoutLike(param));
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microCustomer
     * @return void
     **/
    private void generateCustomerCode(MicroCustomer microCustomer) {
        String customerCode = redisCache.incrCodeByDay(CommonConstants.CUSTOMER_GENERATE_PREFIX,1,1);
        microCustomer.setCustomerCode(customerCode);
    }

    /**
     * 修改客户基础
     * 
     * @param microCustomer 客户基础
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroCustomer(MicroCustomer microCustomer)
    {
        LoginUser user = SecurityUtils.getLoginUser();
        if (!ObjectUtils.isEmpty(user)){
            microCustomer.setUpdateBy(user.getUsername());
        }
        microCustomer.setUpdatedDate(DateUtils.getNowDate());
        return microCustomerMapper.updateMicroCustomer(microCustomer);
    }

    /**
     * 批量删除客户基础
     * 
     * @param ids 需要删除的客户基础ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMicroCustomerByIds(Long[] ids)
    {
        return microCustomerMapper.deleteMicroCustomerByIds(ids);
    }

    /**
     * 删除客户基础信息
     * 
     * @param id 客户基础ID
     * @return 结果
     */
    @Override
    public int deleteMicroCustomerById(Long id)
    {
        return microCustomerMapper.deleteMicroCustomerById(id);
    }
}
