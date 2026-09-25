/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.excel;

import cn.afterturn.easypoi.handler.inter.IExcelDictHandler;
import com.cosmo.hhim.common.core.domain.DictData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class IExcelDictHandlerImpl implements IExcelDictHandler {
    private Map<String,List<DictData>> dicts;
    @Override
    public String toName(String s, Object o, String s1, Object o1) {
        if(dicts.entrySet()==null){
            return "";
        }
        for(Map.Entry<String, List<DictData>> list:dicts.entrySet()){
            if(list.getValue()==null){
                continue;
            }
            if(s.equals(list.getKey())){
                for(DictData dictData:list.getValue()){
                    if(o1!=null && dictData.getDictValue().equals(o1.toString())){
                       return  dictData.getDictLabel();
                    }

                }
            }
        }
        return "";

    }

    @Override
    public String toValue(String s, Object o, String s1, Object o1) {
        return null;
    }

    public IExcelDictHandlerImpl(Map<String, List<DictData>> dicts){
        this.dicts=dicts;
    }
}
