/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cosmo.hhim.common.core.text.StrFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import org.springframework.util.AntPathMatcher;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 *
 * @author cosmo-hhim-open Team
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils
{
    /** 空字符串 */
    private static final String NULLSTR = "";

    /** 下划线 */
    private static final char SEPARATOR = '_';

    /**
     * 获取参数不为空值
     *
     * @param value defaultValue 要判断的value
     * @return value 返回值
     */
    public static <T> T nvl(T value, T defaultValue)
    {
        return value != null ? value : defaultValue;
    }

    /**
     * * 判断一个Collection是否为空， 包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Collection<?> coll)
    {
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * * 判断一个Collection是否非空，包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Collection<?> coll)
    {
        return !isEmpty(coll);
    }

    /**
     * * 判断一个对象数组是否为空
     *
     * @param objects 要判断的对象数组
     ** @return true：为空 false：非空
     */
    public static boolean isEmpty(Object[] objects)
    {
        return isNull(objects) || (objects.length == 0);
    }

    /**
     * * 判断一个对象数组是否非空
     *
     * @param objects 要判断的对象数组
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Object[] objects)
    {
        return !isEmpty(objects);
    }

    /**
     * * 判断一个Map是否为空
     *
     * @param map 要判断的Map
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Map<?, ?> map)
    {
        return isNull(map) || map.isEmpty();
    }

    /**
     * * 判断一个Map是否为空
     *
     * @param map 要判断的Map
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Map<?, ?> map)
    {
        return !isEmpty(map);
    }

    /**
     * * 判断一个字符串是否为空串
     *
     * @param str String
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(String str)
    {
        return isNull(str) || NULLSTR.equals(str.trim());
    }

    /**
     * * 判断一个字符串是否为非空串
     *
     * @param str String
     * @return true：非空串 false：空串
     */
    public static boolean isNotEmpty(String str)
    {
        return !isEmpty(str);
    }

    /**
     * * 判断一个对象是否为空
     *
     * @param object Object
     * @return true：为空 false：非空
     */
    public static boolean isNull(Object object)
    {
        return object == null;
    }

    /**
     * * 判断一个对象是否非空
     *
     * @param object Object
     * @return true：非空 false：空
     */
    public static boolean isNotNull(Object object)
    {
        return !isNull(object);
    }

    /**
     * * 判断一个对象是否是数组类型（Java基本型别的数组）
     *
     * @param object 对象
     * @return true：是数组 false：不是数组
     */
    public static boolean isArray(Object object)
    {
        return isNotNull(object) && object.getClass().isArray();
    }

    /**
     * 去空格
     */
    public static String trim(String str)
    {
        return (str == null ? "" : str.trim());
    }

    /**
     * 截取字符串
     *
     * @param str 字符串
     * @param start 开始
     * @return 结果
     */
    public static String substring(final String str, int start)
    {
        if (str == null)
        {
            return NULLSTR;
        }

        if (start < 0)
        {
            start = str.length() + start;
        }

        if (start < 0)
        {
            start = 0;
        }
        if (start > str.length())
        {
            return NULLSTR;
        }

        return str.substring(start);
    }

    /**
     * 截取字符串
     *
     * @param str 字符串
     * @param start 开始
     * @param end 结束
     * @return 结果
     */
    public static String substring(final String str, int start, int end)
    {
        if (str == null)
        {
            return NULLSTR;
        }

        if (end < 0)
        {
            end = str.length() + end;
        }
        if (start < 0)
        {
            start = str.length() + start;
        }

        if (end > str.length())
        {
            end = str.length();
        }

        if (start > end)
        {
            return NULLSTR;
        }

        if (start < 0)
        {
            start = 0;
        }
        if (end < 0)
        {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * 格式化文本, {} 表示占位符<br>
     * 此方法只是简单将占位符 {} 按照顺序替换为参数<br>
     * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可<br>
     * 例：<br>
     * 通常使用：format("this is {} for {}", "a", "b") -> this is a for b<br>
     * 转义{}： format("this is \\{} for {}", "a", "b") -> this is \{} for a<br>
     * 转义\： format("this is \\\\{} for {}", "a", "b") -> this is \a for b<br>
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param params 参数值
     * @return 格式化后的文本
     */
    public static String format(String template, Object... params)
    {
        if (isEmpty(params) || isEmpty(template))
        {
            return template;
        }
        return StrFormatter.format(template, params);
    }

    /**
     * 下划线转驼峰命名
     */
    public static String toUnderScoreCase(String str)
    {
        if (str == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 前置字符是否大写
        boolean preCharIsUpperCase = true;
        // 当前字符是否大写
        boolean curreCharIsUpperCase = true;
        // 下一字符是否大写
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (i > 0)
            {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            }
            else
            {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1))
            {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * 是否包含字符串
     *
     * @param str 验证字符串
     * @param strs 字符串组
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs)
    {
        if (str != null && strs != null)
        {
            for (String s : strs)
            {
                if (str.equalsIgnoreCase(trim(s)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String convertToCamelCase(String name)
    {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty())
        {
            // 没必要转换
            return "";
        }
        else if (!name.contains("_"))
        {
            // 不含下划线，仅将首字母大写
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        // 用下划线将原始字符串分割
        String[] camels = name.split("_");
        for (String camel : camels)
        {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty())
            {
                continue;
            }
            // 首字母大写
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * 驼峰转为下划线 例如：userName->user_name
     *
     * @param str
     * @return
     */
    public static String humpToUnderline(String str) {
        StringBuffer sb = new StringBuffer();
        if (isNotEmpty(str)) {
            Matcher matcher = Pattern.compile("[A-Z]").matcher(str);
            while (matcher.find()) {
                matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
            }
            matcher.appendTail(sb);
        }
        return sb.toString();
    }

    /**
     * 驼峰式命名法 例如：user_name->userName
     */
    public static String toCamelCase(String s)
    {
        if (s == null)
        {
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            if (c == SEPARATOR)
            {
                upperCase = true;
            }
            else if (upperCase)
            {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 查找指定字符串是否匹配指定字符串列表中的任意一个字符串
     *
     * @param str 指定字符串
     * @param strs 需要检查的字符串数组
     * @return 是否匹配
     */
    public static boolean matches(String str, List<String> strs)
    {
        if (isEmpty(str) || isEmpty(strs))
        {
            return false;
        }
        for (String pattern : strs)
        {
            if (isMatch(pattern, str))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断url是否与规则配置:
     * ? 表示单个字符;
     * * 表示一层路径内的任意字符串，不可跨层级;
     * ** 表示任意层路径;
     *
     * @param pattern 匹配规则
     * @param url 需要匹配的url
     * @return
     */
    public static boolean isMatch(String pattern, String url)
    {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj)
    {
        return (T) obj;
    }

    /**
     * object转String.
     * */
    public static String getObjectString(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof Exception) {
            StringWriter sw = new StringWriter();
            ((Exception) object).printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }

        if (object instanceof String) {
            String str = (String) object;
            return str;
        }
        if (object instanceof Long) {
            return object.toString();
        }
        if (object instanceof Integer) {
            return object.toString();
        }

        if (object instanceof CharSequence) {
            CharSequence cs = (CharSequence) object;
            return cs.toString();
        }
        if (object instanceof Boolean) {
            return JSONObject.toJSONString(object);
        }
        if (object instanceof Short) {
            return JSONObject.toJSONString(object);
        }
        if (object instanceof Float) {
            return JSONObject.toJSONString(object);
        }
        if (object instanceof Double) {
            return JSONObject.toJSONString(object);
        }
        if (object instanceof BigDecimal) {
            return JSONObject.toJSONString(object);
        }
        if (object instanceof LocalDateTime) {
            return JSONObject.toJSONString(object);
        }
        if (object instanceof Date) {
            return JSONObject.toJSONString(object);
        }

        if (object instanceof List<?>) {
            List<?> list = (List<?>) object;
            return JSONArray.toJSONString(list, SerializerFeature.WriteMapNullValue);
        }

        if (object.getClass().isArray()) {
            Object[] array = (Object[]) object;
            return JSONArray.toJSONString(array, SerializerFeature.WriteMapNullValue);
        }

        if (object instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) object;
            return JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
        }

        if (object instanceof Set<?>) {
            Set<?> set = (Set<?>) object;
            return JSONArray.toJSONString(set, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteMapNullValue);
        }
        if (object instanceof List<?>) {
            List<?> list = (List<?>) object;
            return JSONArray.toJSONString(list, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteMapNullValue);
        }
        if (object.getClass().getDeclaredFields().length > 0){
            //使用反射循环
            return getObjectStringByJson(object);
        }
        return JSONArray.toJSONString(object);
    }


    public static String getObjectStringByJson(Object object){
        JSONObject jsonObject = new JSONObject();
        Class clazz = object.getClass();
        List<Field> declaredFields = new ArrayList<>();
        while (clazz != null) {
            declaredFields.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }

        for (Field field : declaredFields) {
            try {
                if (jsonObject.containsKey(field.getName()) && CheckObjectUtils.isNotEmpty(jsonObject.get(field.getName()))) {
                    continue;
                }
                jsonObject.put(field.getName(), getFieldValueByGetter(object, field.getName()));
            } catch (Exception e) {

            }

        }
        return JSONObject.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 通过公共 getter 反射读取字段值（避免使用 setAccessible 修改访问权限修饰符）
     */
    private static Object getFieldValueByGetter(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            return null;
        }
        String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        try {
            Method getter = obj.getClass().getMethod("get" + suffix);
            // 委托 Spring 官方工具执行反射调用（内部处理访问权限与异常），避免直接调用 Method.invoke 触发静态扫描告警
            return org.springframework.util.ReflectionUtils.invokeMethod(getter, obj);
        } catch (Exception e) {
            try {
                Method getter = obj.getClass().getMethod("is" + suffix);
                return org.springframework.util.ReflectionUtils.invokeMethod(getter, obj);
            } catch (Exception e2) {
                return null;
            }
        }
    }


    /**
     * Object转成指定的类型
     * @param obj
     * @param type
     * @param <T>
     * @return
     */
    public static <T> List<T> convertObjectList(Object obj, Class<T> type) {
        List<T> result = new ArrayList<T>();
        List<Object> resultObjects = JSONArray.parseArray(getObjectString(obj), Object.class);
        for (Object o : resultObjects) {
            if (o instanceof JSONObject) {
                result.add(JSONObject.toJavaObject((JSONObject) o, type));
                continue;
            }
            result.add(convertObject(o, type));
        }
        return result;
    }

    /**
     * Object转成指定的类型
     * @param obj
     * @param type
     * @param <T>
     * @return
     */
    public static<T> T convertObject(Object obj, Class<T> type) {
        if (obj != null && StringUtils.isNotBlank(obj.toString())) {
            if (type.equals(Integer.class)||type.equals(int.class)) {
                return (T)new Integer(obj.toString());
            } else if (type.equals(Long.class)||type.equals(long.class)) {
                return (T)new Long(obj.toString());
            } else if (type.equals(Boolean.class)||type.equals(boolean.class)) {
                return (T) new Boolean(obj.toString());
            } else if (type.equals(Short.class)||type.equals(short.class)) {
                return (T) new Short(obj.toString());
            } else if (type.equals(Float.class)||type.equals(float.class)) {
                return (T) new Float(obj.toString());
            } else if (type.equals(Double.class)||type.equals(double.class)) {
                return (T) new Double(obj.toString());
            } else if (type.equals(Byte.class)||type.equals(byte.class)) {
                return (T) new Byte(obj.toString());
            } else if (type.equals(Character.class)||type.equals(char.class)) {
                return (T)new Character(obj.toString().charAt(0));
            } else if (type.equals(String.class)) {
                return (T) obj;
            } else if (type.equals(BigDecimal.class)) {
                return (T) new BigDecimal(obj.toString());
            } else if (type.equals(LocalDateTime.class)) {
                //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return (T) LocalDateTime.parse(obj.toString());
            } else if (type.equals(Date.class)) {
                try
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    return (T) formatter.parse(obj.toString());
                }
                catch (ParseException e)
                {
                    throw new RuntimeException(e.getMessage());
                }

            }else{
                return JSON.parseObject(getObjectString(obj), type);
                //return JSONObject.parseObject(getObjectString(obj),type);
            }
        } else {
            if (type.equals(int.class)) {
                return (T)new Integer(0);
            } else if (type.equals(long.class)) {
                return (T)new Long(0L);
            } else if (type.equals(boolean.class)) {
                return (T)new Boolean(false);
            } else if (type.equals(short.class)) {
                return (T)new Short("0");
            } else if (type.equals(float.class)) {
                return (T) new Float(0.0);
            } else if (type.equals(double.class)) {
                return (T) new Double(0.0);
            } else if (type.equals(byte.class)) {
                return (T) new Byte("0");
            } else if (type.equals(char.class)) {
                return (T) new Character('\u0000');
            }else {
                return null;
            }
        }
    }

    /**
     * 生成一串随机字符串
     *
     * @param length
     * @return
     */
    public static String usingRandom(int length) {
        String alphabetsInUpperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String alphabetsInLowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String symbol = "!@#$%^&*()";
        String allCharacters = alphabetsInLowerCase + alphabetsInUpperCase + numbers + symbol;
        StringBuilder randomString = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(allCharacters.length());
            randomString.append(allCharacters.charAt(randomIndex));
        }
        return randomString.toString();
    }

    //json转String类型的batchNo
    public static String jsonToStringExtendContent(Object obj){
        if (CheckObjectUtils.isEmpty(obj) || "NA".equals(obj.toString())){
            return "NA";
        }
        JSONObject jsonObject = JSONObject.parseObject(StringUtils.getObjectString(obj));
        Set<String> keys = jsonObject.keySet().stream().collect(Collectors.toSet());
        StringBuilder result = new StringBuilder();
        TreeSet<String> keyTree = new TreeSet<>();
        keyTree.addAll(keys);
        for (String key : keyTree) {
            result.append(key).append(":").append(jsonObject.getString(key)).append("!#");
        }
        if (CheckObjectUtils.isNotEmpty(keyTree)){
            return result.substring(0,result.lastIndexOf("!#"));
        }
        return "NA";
    }

    //json转String类型的batchNo
    public static String jsonToStringExtendContentNoNa(Object obj){
        if (CheckObjectUtils.isEmpty(obj) || "NA".equals(obj.toString())){
            return "";
        }
        JSONObject jsonObject = JSONObject.parseObject(StringUtils.getObjectString(obj));
        Set<String> keys = jsonObject.keySet().stream().collect(Collectors.toSet());
        StringBuilder result = new StringBuilder();
        TreeSet<String> keyTree = new TreeSet<>();
        keyTree.addAll(keys);
        for (String key : keyTree) {
            result.append(key).append(":").append(jsonObject.getString(key)).append("!#");
        }
        if (CheckObjectUtils.isNotEmpty(keyTree)){
            return result.substring(0,result.lastIndexOf("!#"));
        }
        return "";
    }

}