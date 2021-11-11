package com.zx.util.util;

import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author: zhaoxu
 * @description:
 */
public class BeanUtil {
    /**
     * 通过方法名动态执行某个方法
     *
     * @param object
     * @param methodName
     * @param parameters
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    public Object executeMethod(Object object, String methodName, Object... parameters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> clazz = object.getClass();
        ArrayList<Class<?>> paramTypeList = new ArrayList<>();
        for (Object paramType : parameters) {
            paramTypeList.add(paramType.getClass());
        }
        Class<?>[] classArray = new Class[paramTypeList.size()];
        Method method = clazz.getMethod(methodName, paramTypeList.toArray(classArray));
        Object invoke = method.invoke(object, parameters);
        return invoke;
    }

    /**
     * 获取所有属性值
     *
     * @return
     * @throws IllegalAccessException
     */
    public Map<String, Object> getFieldsValue(Object object) throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        Map<String, Object> fieldValuesMap = new HashMap<>(16);
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Object fieldValue = field.get(object);
            fieldValuesMap.put(field.getName(), fieldValue);
        }
        return fieldValuesMap;
    }

    /**
     * 设置属性值
     *
     * @param property
     * @param value
     */
    public Boolean setValue(Object object, String property, Object value) {
        Class<?> clazz = object.getClass();
        try {
            Field declaredField = clazz.getDeclaredField(property);
            declaredField.setAccessible(true);
            declaredField.set(object, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 获取对象所有属性及对应的类别
     *
     * @param object
     * @return
     * @throws IllegalAccessException
     */
    public Map<String, Class<?>> getFields(Object object) throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        Map<String, Class<?>> attrMap = new HashMap<>(16);
        if (clazz != null) {
            Iterator<String> iterator = getValues(object).keySet().iterator();

            while (iterator.hasNext()) {
                attrMap.put(iterator.next(), Object.class);
            }
        }
        return attrMap;
    }

    /**
     * 获取所有属性值
     *
     * @return
     * @throws IllegalAccessException
     */
    public Map<String, Object> getValues(Object object) throws IllegalAccessException {
        Map<String, Object> fieldValuesMap = new HashMap<>(16);
        Class<?> clazz = object.getClass();
        if (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object fieldValue = field.get(object);
                fieldValuesMap.put(field.getName(), fieldValue);
            }
            return fieldValuesMap;
        }
        return fieldValuesMap;
    }

    /**
     * 获取拥有指定注解的字段
     *
     * @param objectClass
     * @param annoClass
     * @return
     */
    public List<Field> getTargetAnnoation(Class<?> objectClass, Class<? extends Annotation> annoClass) {
        List<Field> fields = new ArrayList<>();
        Field[] declaredFields = objectClass.getDeclaredFields();

        for (Field field : declaredFields) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(annoClass)) {
                continue;
            } else {
                fields.add(field);
            }
        }

        if (!CollectionUtils.isEmpty(fields)) {
            return fields;
        } else {
            return null;
        }
    }
}

