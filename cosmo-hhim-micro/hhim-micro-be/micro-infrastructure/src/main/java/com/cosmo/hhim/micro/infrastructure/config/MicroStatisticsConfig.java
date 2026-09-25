/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 微应用用户推荐统计分析配置类
 *
 * @author cosmo-hhim-open Team
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "statistics")
public class MicroStatisticsConfig {
    /**
     * 个性化开启阈值
     */
    private Integer personalizedThreshold;
    /**
     * 衰减因子
     */
    private BigDecimal decayFactor;
    /**
     * 导航页集合
     */
    private List<String> navPages;
    /**
     * 当前页集合(维度页)
     */
    private List<String> currentPages;
    /**
     *
     */
    private Integer submitCountThreshold = 30;
    /**
     * 送检与记工的比例
     */
    private Double qcAndSubmitRadio = 0.6;
    /**
     * 连续质检次数
     */
    private Integer consecutiveQc = 3;

}
