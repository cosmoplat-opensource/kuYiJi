##功能简介
权限拦截器 feign拦截器
## 内置功能
- 访问权限拦截器
- feign拦截器
- 若依自带的访问权限拦截器
- 数据脱敏


## 功能一：访问权限拦截器（责任人：张耀晖）
使用参考项目：在制品库存-微应用

### 第一步：pom引入common-security依赖

### 第二步：启动类添加@EnableAccessAuth注解
注意：basePackages属性指定需要权限控制扫描的controller的包路径

eg：
```java
@EnableAccessAuth(basePackages = {"com.cosmo.hhim.micro.controller"})
```

### 第三步：自定义实现CustomizedAccessAuthFilterCondition接口

实现两个方法：
1. getLoginUser()---获取登录用户信息（目的：获取登录用户所拥有的角色权限信息）
2. isSkipRequestFilter()---设置需要跳过不做访问权限控制的请求

注意：一定要在自定义实现类上添加@Component注解，让spring帮忙实例化并注入到IOC容器中

eg:
```java
package com.cosmo.hhim.micro.filter;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.accessauth.filter.CustomizedAccessAuthFilterCondition;
import com.cosmo.hhim.common.security.pojo.LoginUser;
import com.cosmo.hhim.micro.constant.CommonConstants;
import com.cosmo.hhim.micro.domain.MicroRole;
import com.cosmo.hhim.micro.entity.MicroUserCompleteInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zyh
 * @createTime 2022-11-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MicroAccessAuthFilterCondition implements CustomizedAccessAuthFilterCondition {

    private final RedisCache redisCache;

    @Override
    public LoginUser getLoginUser(HttpServletRequest request) {
        String type = request.getHeader(CacheConstants.DETAILS_TYPE);
        String username = this.getUsername(request);
        String token = this.getToken(request);

        if (!StringUtils.hasText(type) || !StringUtils.hasText(username) || !StringUtils.hasText(token)) {
            return null;
        }

        Object cacheObject = redisCache.getCacheObject(getTokenRedisKey(token, username, type));
        if (!(cacheObject instanceof MicroUserCompleteInfo)) {
            return null;
        }
        MicroUserCompleteInfo microUserCompleteInfo = (MicroUserCompleteInfo) cacheObject;
        Set<String> rolesSet = microUserCompleteInfo.getMicroRoles().stream().map(MicroRole::getRoleCode).collect(Collectors.toSet());

        LoginUser loginUser = new LoginUser();
        loginUser.setUsername(username);
        loginUser.setRoles(rolesSet);
        return loginUser;
    }

    @Override
    public boolean isSkipRequestFilter(HttpServletRequest request) {
        String applicationSign = request.getHeader(Constants.APPLICATION_SIGN);
        String authorization = request.getHeader(CacheConstants.HEADER);

        // 1.请求应用标识如果非在制品库存，则跳过
        if (!StringUtils.hasText(applicationSign) || !CommonConstants.MICRO_PROCESS_APPLICATION.equals(applicationSign)) {
            return true;
        }

        // 2.请求header中未携带token信息，则跳过（token权限认证已经在gateway做过处理，故这里未携带则跳过）
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(CacheConstants.TOKEN_PREFIX)) {
            return true;
        }
        return false;
    }

    /**
     * 获取redis的token key
     *
     * @param token
     * @param username
     * @param deviceType
     * @return
     */
    private String getTokenRedisKey(String token, String username, String deviceType) {
        return Constants.LOGIN_MICRO_TOKEN_KEY + username + ":" + deviceType + ":" + token;
    }

    /**
     * 获取请求token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(CacheConstants.HEADER);
        if (StringUtils.hasText(token) && token.startsWith(CacheConstants.TOKEN_PREFIX)) {
            token = token.replace(CacheConstants.TOKEN_PREFIX, "");
        }
        return token;
    }

    /**
     * 获取用户名
     *
     * @param request
     * @return
     */
    private String getUsername(HttpServletRequest request) {
        String username = request.getHeader(CacheConstants.DETAILS_USERNAME);
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return ServletUtils.urlDecode(username);
    }
}

```

### 第四步：在需要权限控制的Controller方法上添加@AccessAuth标识

注意：allowAccessRoles属性设置为可以访问此接口的角色编码


## 功能二：数据脱敏（责任人：张耀晖）
- 支持请求指定自定义字段信息脱敏
- 支持导出Excel的指定字段脱敏
- 可根据用户权限自动判断需不需要针对自定义脱敏字段进行脱敏处理

使用参考项目：OMS

### 第一步：pom引入common-security依赖

### 第二步：