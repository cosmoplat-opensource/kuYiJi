/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.accessauth.request.mapping;

import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author cosmo-hhim-open Team
 * @description 请求参数条件表达式配置解析类
 * @createTime 2022-10-28
 */
public class ParamRequestCondition {
    
    private final Set<ParamExpression> expressions;
    
    public ParamRequestCondition(String... expressions) {
        this.expressions = parseExpressions(expressions);
    }
    
    private Set<ParamExpression> parseExpressions(String... params) {
        if (ObjectUtils.isEmpty(params)) {
            return Collections.emptySet();
        }
        Set<ParamExpression> expressions = new LinkedHashSet<>(params.length);
        for (String param : params) {
            expressions.add(new ParamExpression(param));
        }
        return expressions;
    }
    
    public Set<ParamExpression> getExpressions() {
        return expressions;
    }
    
    public ParamRequestCondition getMatchingCondition(HttpServletRequest request) {
        for (ParamExpression expression : this.expressions) {
            if (!expression.match(request)) {
                return null;
            }
        }
        return this;
    }
    
    static class ParamExpression {
        
        private final String name;
        
        private final String value;
        
        private final boolean isNegated;
        
        ParamExpression(String expression) {
            int separator = expression.indexOf('=');
            if (separator == -1) {
                this.isNegated = expression.startsWith("!");
                this.name = isNegated ? expression.substring(1) : expression;
                this.value = null;
            } else {
                this.isNegated = (separator > 0) && (expression.charAt(separator - 1) == '!');
                this.name = isNegated ? expression.substring(0, separator - 1) : expression.substring(0, separator);
                this.value = expression.substring(separator + 1);
            }
        }
        
        public final boolean match(HttpServletRequest request) {
            boolean isMatch;
            if (this.value != null) {
                isMatch = matchValue(request);
            } else {
                isMatch = matchName(request);
            }
            return this.isNegated != isMatch;
        }
        
        private boolean matchName(HttpServletRequest request) {
            return request.getParameterMap().containsKey(this.name);
        }
        
        private boolean matchValue(HttpServletRequest request) {
            return ObjectUtils.nullSafeEquals(this.value, request.getParameter(this.name));
        }
    }
}
