/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.datamask.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.cosmo.hhim.common.core.constant.enums.DataMaskType;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.datamask.annotations.DataMask;
import com.cosmo.hhim.common.security.datamask.cache.PricePermissionCache;
import com.cosmo.hhim.common.security.pojo.LoginUser;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @description 数据脱敏JSON解析自定义过滤器
 * @createTime 2022-01-14
 */
public class DataMaskBeanPropertyFilter implements ValueFilter {


    @Override
    public Object process(Object obj, String name, Object value) {
        value = dateFieldProcess(obj,name,value);
        try {
            DataMask dataMask = null;
            Class<?> objClass = obj.getClass();
            while (objClass != null && !Objects.equals(objClass.getName(), Object.class.getName())){
                try {
                    if(objClass.getDeclaredField(name).getAnnotation(DataMask.class) != null){
                        dataMask = objClass.getDeclaredField(name).getAnnotation(DataMask.class);
                        break;
                    }
                }catch (Exception e){

                }

                objClass = objClass.getSuperclass();
            }
            if (dataMask != null && value != null) {
                DataMaskType type = dataMask.dataMaskType();

                // 判断当前登录用户是否拥有该价格权限，没有则对字段值脱敏处理
                List<String> pricePermissions = PricePermissionCache.getCache();
                if (CollectionUtils.isEmpty(pricePermissions)) {
                    LoginUser loginUser = SecurityUtils.getLoginUser();
                    if (null != loginUser
                            && !CollectionUtils.isEmpty(loginUser.getPricePermissions())) {
                        PricePermissionCache.saveCache(loginUser.getPricePermissions());
                        pricePermissions = loginUser.getPricePermissions();
                    }
                }
                if (!CollectionUtils.isEmpty(pricePermissions) && pricePermissions.contains(type.getCode())) {
                    return value;
                }
                value = dataMask.padChar();
            }
        } catch (Exception e) {
            return value;
        }
        return value;
    }

    /**
     * 处理JsonFormat注解的日期
     * @param obj
     * @param name
     * @param value
     * @return
     */
    private Object dateFieldProcess(Object obj, String name, Object value) {
        //字段是否是Date类型（value.getClass() 在 value 非 null 时恒非 null，冗余判断已去除）
        if(value != null && Objects.equals(value.getClass().getName(), Date.class.getName())){
            try {
                //Date字段是否有JsonFormat注解
                if(obj.getClass().getDeclaredField(name).getAnnotation(JsonFormat.class) != null){
                    JsonFormat jsonFormat = obj.getClass().getDeclaredField(name).getAnnotation(JsonFormat.class);
                    SimpleDateFormat sdf = new SimpleDateFormat(jsonFormat.pattern());
                    value = sdf.format(value);
                }else{
                    //如果该Date类型字段没有JsonFormat注解，则默认格式化为 yyyy-MM-dd HH:mm:ss
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    value = sdf.format(value);
                }
            }catch (Exception e){
                //如果该Date类型字段没有JsonFormat注解，则默认格式化为 yyyy-MM-dd HH:mm:ss
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                value = sdf.format(value);
            }
        }
        return value;
    }
}
