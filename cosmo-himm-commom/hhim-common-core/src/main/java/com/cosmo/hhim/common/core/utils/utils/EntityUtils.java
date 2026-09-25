/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils.utils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityUtils {

	private static final Logger log = LoggerFactory.getLogger(EntityUtils.class);

	/**
	 * 根据字段名在继承链上查找字段（仅获取 Field 对象，不修改访问权限）
	 */
	private static Field findField(Class<?> clazz, String name) {
		while (clazz != null) {
			try {
				return clazz.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			}
		}
		return null;
	}

	/**
	 * 通过公共 getter 反射读取字段值（避免使用 setAccessible 修改访问权限修饰符）
	 */
	private static Object invokeGetter(Object obj, String fieldName) {
		String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		try {
			Method getter = obj.getClass().getMethod("get" + suffix);
			return getter.invoke(obj);
		} catch (NoSuchMethodException e) {
			// boolean 类型字段可能以 is 前缀命名
			try {
				Method getter = obj.getClass().getMethod("is" + suffix);
				return getter.invoke(obj);
			} catch (NoSuchMethodException e2) {
				return null;
			} catch (Exception e2) {
				throw new RuntimeException("获取数据出错");
			}
		} catch (Exception e) {
			throw new RuntimeException("获取数据出错");
		}
	}

	/**
	 * 通过公共 setter 反射写入字段值（避免使用 setAccessible 修改访问权限修饰符）
	 */
	private static boolean invokeSetter(Object obj, String fieldName, Object val) {
		Field field = findField(obj.getClass(), fieldName);
		if (field == null) {
			// 字段不存在：保持原行为（返回 true 不抛异常）
			return true;
		}
		String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		try {
			Method setter = obj.getClass().getMethod("set" + suffix, field.getType());
			setter.invoke(obj, val);
			return true;
		} catch (NoSuchMethodException e) {
			return true;
		} catch (Exception e) {
			throw new RuntimeException("获取数据出错");
		}
	}

	/**
	 * 实体类转Map
	 * @param object
	 * @return
	 */
	public static Map<String, Object> entityToMap(Object object) {
		Map<String, Object> map = new HashMap();
	    for (Field field : object.getClass().getDeclaredFields()){
	        try {
	        	Object o = invokeGetter(object, field.getName());
	            map.put(field.getName(), o);
	        } catch (Exception e) {

	        }
	    }
	    return map;
	}

	/**
	 * Map转实体类
	 * @param map 需要初始化的数据，key字段必须与实体类的成员名字一样，否则赋值为空
	 * @param entity  需要转化成的实体类
	 * @return
	 */
	public static <T> T mapToEntity(Map<String, Object> map, Class<T> entity) {
		T t = null;
		try {
			t = entity.newInstance();
			for(Field field : entity.getDeclaredFields()) {
				if (map.containsKey(field.getName())) {
		            Object object = map.get(field.getName());
		            if (object!= null && field.getType().isAssignableFrom(object.getClass())) {
		            	invokeSetter(t, field.getName(), object);
					}
				}
			}
			return t;
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
			log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
		}
		return t;
	}

	/**
	 * 通过反射判断是否包含该属性
	 * @param  param 要判断的对象
	 * @param  name 要判断的字段名
	 *
	 * */
    public static boolean isHaveField(Object param,String name) {
		Class clazz = param.getClass();
		List<Field> fields = new ArrayList<>();
		while (clazz != null) {
			fields.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
			clazz = clazz.getSuperclass();
		}
        List<String> names = fields.stream().map(Field::getName).collect(Collectors.toList());
        if (names.contains(name)){
            return true;
        }
        return false;
    }

	/**
	 * 通过反射获取该属性值
	 *  @param  param 要赋值的对象
	 *  @param  name 要获取值的字段名
	 * */
	public static Object getValFildName(Object param, String name) {
		try {
			return invokeGetter(param, name);
		} catch (Exception e) {
			throw new RuntimeException("获取数据出错");
		}
	}

	/**
	 * 通过反射赋值
	 * @param  param 要赋值的对象
	 * @param  name 要赋值的字段名
	 * @param  val 要赋值进去的数据
	 *
	 * */
	public static boolean setValFildName(Object param, String name, Object val) {
		return invokeSetter(param, name, val);
	}
}
