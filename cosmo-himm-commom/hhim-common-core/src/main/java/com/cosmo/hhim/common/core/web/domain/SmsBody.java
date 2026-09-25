/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.web.domain;

import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息推送
 *
 * @author cosmo-hhim-open Team
 * @date 2021-03-16
 */
@Data
public class SmsBody extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 系统编码
     */
    private String appsCode;

    /**
     * 消息类型  消息类型：10-消息；20-待办
     */
    private String sysType;

    /** smsType  为10消息时         为20时是代办
     * 1:工单下发
     * 2:生产批次第一道工序上线
     * 3:生产批次最后一道工序自检完成
     * 4:自检异常提报
     * 5:评审结果为报废
     * 6:评审结果为返工
     * 7:评审结果为返修
     * 8:返修结果为已完成
     * 9:返工结果为已完成
     */
    /**
     * 业务流程    为20时是代办
     * 1:工单下发
     * 2:质量复核
     * 3:质量评审
     * 4:返修办理
     * 5:返工办理
     * 6:物料异常办理
     * 7:送检变更为首件变更送检时提交
     */
    private String nodeType;

    /** 节点类型    10时
     * 1:工单下发                    工单id
     * 2:生产批次第一道工序上线            生产批次id
     * 3:生产批次最后一道工序自检完成      生产批次id
     * 4:自检异常提报                生产批次id
     * 5:评审结果为报废              工序异常待办id
     * 6:评审结果为返工              工序异常待办id
     * 7:评审结果为返修              工序异常待办id
     * 8:返修结果为已完成            工序异常待办id
     * 9:返工结果为已完成            工序异常待办id
     */

    /**
     * 节点类型    20时
     * 1:工单下发                    工单id
     * 2:生产批次第一道工序上线            生产批次id
     * 3:生产批次最后一道工序自检完成      生产批次id
     * 4:自检异常提报                生产批次id
     * 5:评审结果为报废              工序异常待办id
     * 6:评审结果为返工              工序异常待办id
     * 7:评审结果为返修              工序异常待办id
     * 8:返修结果为已完成            工序异常待办id
     * 9:返工结果为已完成            工序异常待办id
     */
    private String todoPk;


    /**
     * 提交时间
     */
    private Date submitTime;

    /**
     * 提交人
     */
    private String userName;
    /**
     * 消息链接(PC端)
     */
    private String SmsLink;
    /**
     * 消息链接(APP端)
     */
    private String SmsLinkApp;

    // 新增map

    /**
     * 消息模板内容填充map
     * key :  {XX1}
     * value:	内容
     */
    private Map<String, Object> smsTemplateMap;

    // 过滤数据权限使用
    /**
     * 权限类型 生产（工厂--F，车间-W，产线--L） wms（AREA -库区，FAC-工厂，WH-仓库）
     */
    private String dataType;

    /**
     * 编码
     */
    private String dataCode;

    /**
     * 应用类型(cim/wms)
     */
    private String appType;


    /**
     * 多数据源信息
     */
    private String db;
    private String schema;
    private String customerCode;

    public Map<String, Object> getSmsTemplateMap() {
        if (smsTemplateMap == null) {
            smsTemplateMap = new HashMap<>();
        }
        return smsTemplateMap;
    }
}
