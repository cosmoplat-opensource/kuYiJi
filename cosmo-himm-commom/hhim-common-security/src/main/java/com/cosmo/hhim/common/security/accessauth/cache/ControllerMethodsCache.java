/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.accessauth.cache;

import com.cosmo.hhim.common.security.accessauth.request.mapping.ParamRequestCondition;
import com.cosmo.hhim.common.security.accessauth.request.mapping.RequestMappingInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.cosmo.hhim.common.core.constant.CacheConstants.REQUEST_PATH_SEPARATOR;

/**
 * @author cosmo-hhim-open Team
 * @description Controller方法和请求路径的映射信息缓存
 * @createTime 2022-10-28
 */
@Slf4j
public class ControllerMethodsCache {

    // 维护spring @RequestMapping解析信息与@Controller中方法的映射关系
    private ConcurrentMap<RequestMappingInfo, Method> methods = new ConcurrentHashMap<>();

    // 维护 请求方式 + url ---> @RequestMapping解析信息的映射关系
    private final ConcurrentMap<String, List<RequestMappingInfo>> urlLookup = new ConcurrentHashMap<>();

    /**
     * 获取请求对应的Controller的Method
     * @param request
     * @return
     */
    public Method getMethod(HttpServletRequest request) {
        String path = getPath(request);
        String httpMethod = request.getMethod();
        String urlKey = httpMethod + REQUEST_PATH_SEPARATOR + path;
        List<RequestMappingInfo> requestMappingInfos = urlLookup.get(urlKey);
        if (CollectionUtils.isEmpty(requestMappingInfos)) {
            return null;
        }
        List<RequestMappingInfo> matchedInfo = findMatchedInfo(requestMappingInfos, request);
        if (CollectionUtils.isEmpty(matchedInfo)) {
            return null;
        }
        RequestMappingInfo bestMatch = matchedInfo.get(0);
        if (matchedInfo.size() > 1) {
            RequestMappingInfo.RequestMappingInfoComparator comparator = new RequestMappingInfo.RequestMappingInfoComparator();
            matchedInfo.sort(comparator);
            bestMatch = matchedInfo.get(0);
            RequestMappingInfo secondBestMatch = matchedInfo.get(1);
            if (comparator.compare(bestMatch, secondBestMatch) == 0) {
                throw new RuntimeException("通过请求URL获取映射方法时，获取到两个最佳结果！requestUrl:"
                        + request.getRequestURI() + "---> {" + bestMatch + ", " + secondBestMatch + "}");
            }
        }
        return methods.get(bestMatch);
    }

    /**
     * 获取请求的URL信息
     * @param request
     * @return
     */
    private String getPath(HttpServletRequest request) {
        // 直接取请求路径（getRequestURI 本身不含查询串，避免 new URI 解析带来的注入面）
        return request.getRequestURI();
    }

    /**
     * 根据请求URL获取所映射的方法
     * @param requestMappingInfos
     * @param request
     * @return
     */
    private List<RequestMappingInfo> findMatchedInfo(List<RequestMappingInfo> requestMappingInfos, HttpServletRequest request) {
        List<RequestMappingInfo> matchedInfo = new ArrayList<>();
        for (RequestMappingInfo requestMappingInfo : requestMappingInfos) {
            ParamRequestCondition matchingCondition = requestMappingInfo.getParamRequestCondition().getMatchingCondition(request);
            if (matchingCondition != null) {
                matchedInfo.add(requestMappingInfo);
            }
        }
        return matchedInfo;
    }

    /**
     * 从指定包中查询目标方法
     *
     * @param packageName
     */
    public void initClassMethod(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(RequestMapping.class);

        for (Class clazz : classesList) {
            initClassMethod(clazz);
        }
        log.info("初始化指定包中查询的目标方法完成...");
    }

    /**
     * 从目标类集合中查询目标方法
     *
     * @param classesList
     */
    public void initClassMethod(Set<Class<?>> classesList) {
        for (Class clazz : classesList) {
            initClassMethod(clazz);
        }
    }

    /**
     * 从目标类中查询目标方法
     *
     * @param clazz
     */
    private void initClassMethod(Class<?> clazz) {
        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
        for (String classPath : requestMapping.value()) {
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    parseSubAnnotations(method, classPath);
                    continue;
                }
                requestMapping = method.getAnnotation(RequestMapping.class);
                RequestMethod[] requestMethods = requestMapping.method();
                if (requestMethods.length == 0) {
                    requestMethods = new RequestMethod[1];
                    requestMethods[0] = RequestMethod.GET;
                }
                for (String methodPath : requestMapping.value()) {
                    String urlKey = requestMethods[0].name() + REQUEST_PATH_SEPARATOR + classPath + methodPath;
                    addUrlAndMethodRelation(urlKey, requestMapping.params(), method);
                }
            }
        }
    }

    /**
     * 解析RequestMapping的子注解
     * @param method
     * @param classPath
     */
    private void parseSubAnnotations(Method method, String classPath) {

        final GetMapping getMapping = method.getAnnotation(GetMapping.class);
        final PostMapping postMapping = method.getAnnotation(PostMapping.class);
        final PutMapping putMapping = method.getAnnotation(PutMapping.class);
        final DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        final PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);

        if (getMapping != null) {
            put(RequestMethod.GET, classPath, getMapping.value(), getMapping.params(), method);
        }

        if (postMapping != null) {
            put(RequestMethod.POST, classPath, postMapping.value(), postMapping.params(), method);
        }

        if (putMapping != null) {
            put(RequestMethod.PUT, classPath, putMapping.value(), putMapping.params(), method);
        }

        if (deleteMapping != null) {
            put(RequestMethod.DELETE, classPath, deleteMapping.value(), deleteMapping.params(), method);
        }

        if (patchMapping != null) {
            put(RequestMethod.PATCH, classPath, patchMapping.value(), patchMapping.params(), method);
        }

    }

    /**
     * 根据requestPaths是否有值，对urlKey进行拼接处理
     * @param requestMethod
     * @param classPath
     * @param requestPaths
     * @param requestParams
     * @param method
     */
    private void put(RequestMethod requestMethod, String classPath, String[] requestPaths, String[] requestParams,
                     Method method) {
        if (ArrayUtils.isEmpty(requestPaths)) {
            String urlKey = requestMethod.name() + REQUEST_PATH_SEPARATOR + classPath;
            addUrlAndMethodRelation(urlKey, requestParams, method);
            return;
        }
        for (String requestPath : requestPaths) {
            String urlKey = requestMethod.name() + REQUEST_PATH_SEPARATOR + classPath + requestPath;
            addUrlAndMethodRelation(urlKey, requestParams, method);
        }
    }

    /**
     * 1.添加url与spring requestMapping解析信息的映射关系
     * 2.添加requestMapping解析信息与controller中方法的映射关系
     * @param urlKey
     * @param requestParam
     * @param method
     */
    private void addUrlAndMethodRelation(String urlKey, String[] requestParam, Method method) {
        RequestMappingInfo requestMappingInfo = new RequestMappingInfo();
        requestMappingInfo.setParamRequestCondition(new ParamRequestCondition(requestParam));
        List<RequestMappingInfo> requestMappingInfos = urlLookup.get(urlKey);
        if (requestMappingInfos == null) {
            urlLookup.putIfAbsent(urlKey, new ArrayList<>());
            requestMappingInfos = urlLookup.get(urlKey);

            String urlKeyBackup = urlKey + "/";
            urlLookup.putIfAbsent(urlKeyBackup, requestMappingInfos);
        }
        requestMappingInfos.add(requestMappingInfo);
        methods.put(requestMappingInfo, method);
    }
}
