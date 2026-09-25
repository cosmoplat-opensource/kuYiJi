/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.domain.BarcodeSerializeParam;
import com.cosmo.hhim.common.core.enums.BarcodeSerialize;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.exception.WmsServiceException;
import com.cosmo.hhim.common.core.utils.utils.EntityUtils;
import org.apache.commons.collections4.map.LinkedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 条码序列化工具类
 * 条码分为3端，前区 |[ 排序码，加密标记 ]
 * 前区多个字段用 | 分割
 *
 * @author cosmo-hhim-open Team
 */
public class BarcodeSerializeUtils {
    private static final Logger log = LoggerFactory.getLogger(BarcodeSerializeUtils.class);

    /**
     * 通过参数批量生成序列化后的条码-不加密
     *
     * @param params 数据集
     * @param sorts  排序码
     * @return List<String>
     */
    public static List<String> createBarcodes(List<BarcodeSerializeParam> params, LinkedList<String> sorts) {
        return createBarcodes(params, sorts, BarcodeSerialize.epPlag.N.getCode());
    }

    /**
     * 通过参数批量生成序列化后的条码-自选
     *
     * @param params 数据集
     * @param sorts  排序码
     * @param epPlag 加密标记
     * @return List<String>
     */
    public static List<String> createBarcodes(List<BarcodeSerializeParam> params, LinkedList<String> sorts, int epPlag) {
        List<String> results = new ArrayList<>();
        if (CheckObjectUtils.isEmpty(params)) {
            throw new CustomException("参数不能为空");
        }
        if (CheckObjectUtils.isEmpty(sorts)) {
            throw new CustomException("排序码不能为空");
        }
        StringBuffer result;
        for (BarcodeSerializeParam param : params) {
            result = new StringBuffer();
            param.getClass().getDeclaredFields();
            for (String sort : sorts) {
                if (CheckObjectUtils.isEmpty(BarcodeSerialize.Sort.valueOfCode(sort).getColumn())) {
                    throw new RuntimeException("序列码" + sort + "未查询到");
                }
                result.append(EntityUtils.getValFildName(param, BarcodeSerialize.Sort.valueOfCode(sort).getColumn())).append("|");
            }
            LinkedList<String> sortsString = new LinkedList<>(sorts);
            sortsString.add(epPlag + "");
            result.append("[");
            for (String obj : sortsString) {
                result.append(StringUtils.getObjectString(obj));
            }
            result.append("]");
            results.add(result.toString());
        }
        return results;
    }

    /**
     * 批量反序列化解析条码
     *
     * @return List<BarcodeSerializeParam>
     */
    public static List<BarcodeSerializeParam> analysisBarcodes(List<String> params) {
        if (CheckObjectUtils.isEmpty(params)) {
            throw new CustomException("参数不能为空");
        }
        //先解析出前区和后区
        List<BarcodeSerializeParam> results = new ArrayList<>();
        BarcodeSerializeParam result;
        for (String param : params) {
            result = new BarcodeSerializeParam();
            LinkedHashMap<String,Object> map = new LinkedHashMap();
            try {
                if (!param.contains("[") || !param.contains("]") || !param.contains("|")) {
                    throw new CustomException("不合法的数据");
                }
                String[] sorts = getSorts(param);
                String[] barcode = getBarcodes(param);
                for (int i = 0; i < sorts.length; i++) {
                    EntityUtils.setValFildName(result, BarcodeSerialize.Sort.valueOfCode(sorts[i]).getColumn(), barcode[i]);
                    map.put(BarcodeSerialize.Sort.valueOfCode(sorts[i]).getColumn(),barcode[i]);
                }
                //String epFlagString = split1[0].substring(split1[0].length() - 1);
            } catch (Exception e) {
                log.error("", e);
                result.setFlag(false);
            }
            result.setOriString(param);
            map.put("flag",result.isFlag());
            map.put("oriString",result.getOriString());
            result.setBarcodeMap(map);
            results.add(result);
        }
        return results;
    }

    //获取排序码数组
    public static String[] getSorts(String param) {
        String[] split = param.split("\\[");
        String[] split1 = split[1].split("\\]");
        String[] sorts = split1[0].substring(0, split1[0].length() - 1).split("|");
        return sorts;
    }

    //获取条码信息数据
    public static String[] getBarcodes(String param) {
        String[] split = param.split("\\[");
        String[] barcode = new String[getSorts(param).length];
        String barString = split[0] + "|";
        for (int i = 0; i < barcode.length; i++) {
            barcode[i] = barString.substring(0,barString.indexOf( "|"));
            barString = barString.substring(barString.indexOf("|") + 1);
        }

        return barcode;
    }

    //map类型的返回参数
    public static List<LinkedHashMap<String, Object>> analysisBarcodesToMap(List<String> params) {
        List<BarcodeSerializeParam> barcodeSerializeParams = BarcodeSerializeUtils.analysisBarcodes(params);
        List<LinkedHashMap<String, Object>> results = barcodeSerializeParams.stream().map(obj -> {
            LinkedHashMap<String, Object> objectMap = obj.getBarcodeMap();
            for (Iterator<LinkedMap.Entry<String, Object>> car = objectMap.entrySet().iterator(); car.hasNext(); ) {
                LinkedMap.Entry<String, Object> entry = car.next();
                Map<String, Object> val = new HashMap<>();
                val.put("value", entry.getValue());
                try {
                    Field field = obj.getClass().getDeclaredField(entry.getKey());
                    Excel attr = field.getAnnotation(Excel.class);
                    val.put("name", attr.name());
                    entry.setValue(val);
                } catch (Exception e) {
                    throw new WmsServiceException(e);
                }
            }
            return objectMap;
        }).collect(Collectors.toList());
        return results;
    }


}
