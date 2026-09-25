/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.custom.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField;
import com.cosmo.hhim.micro.base.domain.entity.custom.SimpleExtendFieldInfo;
import com.cosmo.hhim.micro.base.domain.mapper.custom.MicroExtendFieldMapper;
import com.cosmo.hhim.micro.base.domain.service.custom.IMicroExtendFieldService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 扩展字段Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
@Service
public class MicroExtendFieldServiceImpl implements IMicroExtendFieldService
{
    @Autowired
    private MicroExtendFieldMapper microExtendFieldMapper;

    /**
     * 查询扩展字段
     * 
     * @param id 扩展字段ID
     * @return 扩展字段
     */
    @Override
    public MicroExtendField selectMicroExtendFieldById(Long id)
    {
        return microExtendFieldMapper.selectMicroExtendFieldById(id);
    }

    /**
     * 查询扩展字段列表
     * 
     * @param microExtendField 扩展字段
     * @return 扩展字段
     */
    @Override
    public List<MicroExtendField> selectMicroExtendFieldList(MicroExtendField microExtendField)
    {
        return microExtendFieldMapper.selectMicroExtendFieldList(microExtendField);
    }

    /**
     * 新增扩展字段
     * 
     * @param microExtendField 扩展字段
     * @return 结果
     */
    @Override
    public int insertMicroExtendField(MicroExtendField microExtendField)
    {
        return microExtendFieldMapper.insertMicroExtendField(microExtendField);
    }

    /**
     * 修改扩展字段
     * 
     * @param microExtendField 扩展字段
     * @return 结果
     */
    @Override
    public int updateMicroExtendField(MicroExtendField microExtendField)
    {
        return microExtendFieldMapper.updateMicroExtendField(microExtendField);
    }

    /**
     * 批量删除扩展字段
     * 
     * @param ids 需要删除的扩展字段ID
     * @return 结果
     */
    @Override
    public int deleteMicroExtendFieldByIds(Long[] ids)
    {
        return microExtendFieldMapper.deleteMicroExtendFieldByIds(ids);
    }

    /**
     * 删除扩展字段信息
     * 
     * @param id 扩展字段ID
     * @return 结果
     */
    @Override
    public int deleteMicroExtendFieldById(Long id)
    {
        return microExtendFieldMapper.deleteMicroExtendFieldById(id);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 保存自定义字段
     * @date 2023/3/24 11:28
     * @param microExtendField
     * @return com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField
     **/
    @Override
    public MicroExtendField saveMicroExtendField(MicroExtendField microExtendField) {
        MicroExtendField param = new MicroExtendField();
        // todo 要确认是否以名称作为唯一判断
        param.setExtFieldLabel(microExtendField.getExtFieldLabel());
        param.setFieldType(microExtendField.getFieldType());
        param.setExtFieldOption(microExtendField.getExtFieldOption());
        List<MicroExtendField> microExtendFields = microExtendFieldMapper.selectMicroExtendFieldList(param);
        if (CollectionUtil.isNotEmpty(microExtendFields)){
            microExtendField = microExtendFields.get(0);
            if (ActiveFlagStandardEnum.DISABLE.getCode().equals(microExtendField.getActiveFlag())){
                microExtendField.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
                this.updateMicroExtendField(microExtendField);
            }
            return microExtendField;
        }
        generateExtField(microExtendField);
        this.insertMicroExtendField(microExtendField);
        return microExtendField;
    }

    /**
     * 返回自定义字段的基础信息
     *
     * @param extendContent
     * @return
     */
    @Override
    public List<SimpleExtendFieldInfo> selectSimpleExtendFieldInfoList(String extendContent) {
        // 存储结果
        List<SimpleExtendFieldInfo> extendFieldInfoList = new ArrayList<>();
        Map<String, String> map = JSONObject.parseObject(extendContent, new TypeReference<Map<String, String>>(){});
        if (!map.isEmpty()) {
            Set<String> fieldList = map.keySet();
            List<MicroExtendField> microExtendFieldListTemp = microExtendFieldMapper.selectMicroExtendFieldListByExtFieldList(fieldList);
            for (String key : map.keySet()) {
                SimpleExtendFieldInfo temp = new SimpleExtendFieldInfo();
                List<MicroExtendField> extendFieldList = microExtendFieldListTemp.stream()
                        .filter(obj -> obj.getExtField().equals(key))
                        .collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(extendFieldList)) {
                    temp.setFieldName(extendFieldList.get(0).getExtFieldLabel());
                    temp.setFieldValue(map.get(key));
                    extendFieldInfoList.add(temp);
                }
            }
        }
        return extendFieldInfoList;
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 初始化自定义字段名称
     * @date 2023/3/24 11:28
     * @param microExtendField
     * @return void
     **/
    private void generateExtField(MicroExtendField microExtendField) {
        int num = microExtendFieldMapper.selectMaxExtField() + 1;
        microExtendField.setExtField(CommonConstants.CUSTOM_FIELD_GENERATE_PREFIX + num);
    }
}
