package com.zx.repository.util;

/**
 * @author: zhaoxu
 * @date: 2021/4/30 23:42
 */

import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.cglib.beans.BeanGenerator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 动态vo类
 * @author : zhaoxu
 */
@Data
@NoArgsConstructor
public class DynmicVO {

    Object dynamicBean;

    Class clazz;

    ReflectUtil reflectUtil = new ReflectUtil();

    public DynmicVO(Map dynAttrMap) {
        this.dynamicBean = generateBean(dynAttrMap);
        clazz = dynamicBean.getClass();
    }

    public DynmicVO(Object object) throws IllegalAccessException, NoSuchFieldException {
        dynamicBean = generateBean(reflectUtil.getFields(object));
        Map<String, Object> values = reflectUtil.getValues(object);
        Iterator<String> iterator = values.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = values.get(key);
            put(key, value);
        }
        clazz = dynamicBean.getClass();
    }

    /**
     * 获取所有属性值
     *
     * @return map
     * @throws IllegalAccessException 错误
     */
    public Map<String, Object> getValues() throws IllegalAccessException {
        Map<String, Object> fieldValuesMap = new HashMap(16);
        if (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object fieldValue = field.get(dynamicBean);
                fieldValuesMap.put(field.getName().split("\\$cglib_prop_")[1], fieldValue);
            }
            return fieldValuesMap;
        }
        return fieldValuesMap;
    }


    /**
     * 设置属性值，不存在就添加
     *
     * @param property 设置的属性
     * @param value 值
     * @throws NoSuchFieldException 没有字段
     * @throws IllegalAccessException 出错
     */
    public void put(String property, Object value) throws IllegalAccessException, NoSuchFieldException {
        Field declaredField;
        try {
            declaredField = clazz.getDeclaredField("$cglib_prop_" + property);
        } catch (Exception e) {
            Map<String, Class<?>> fields = getFields();
            fields.put(property, Object.class);

            Map<String, Object> values = getValues();

            this.dynamicBean = generateBean(fields);
            this.clazz = dynamicBean.getClass();

            values.put(property, value);
            Iterator<String> iterator = values.keySet().iterator();
            while (iterator.hasNext()) {
                String putKey = iterator.next();
                Object putValue = values.get(putKey);
                Field field = clazz.getDeclaredField("$cglib_prop_" + putKey);
                field.setAccessible(true);
                field.set(dynamicBean, putValue);
            }
            return;
        }
        declaredField.setAccessible(true);
        declaredField.set(dynamicBean, value);
    }

    /**
     * 在已有的实体上添加属性
     *
     * @param object 对象
     * @throws NoSuchFieldException 没有字段
     * @throws IllegalAccessException 反射出错
     */
    public void putAll(Object object) throws IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = object.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        Map<String, Object> fieldValuesMap = new HashMap(16);
        Map<String, Object> fieldTypeMap = new HashMap(16);
        for (Field field : declaredFields) {
            field.setAccessible(true);
            Object fieldValue = field.get(object);
            fieldValuesMap.put(field.getName(), fieldValue);
            fieldTypeMap.put(field.getName(), field.getType());
        }
        //获取当前的属性及属性值
        fieldTypeMap.putAll(getFields());
        fieldValuesMap.putAll(getValues());
        this.dynamicBean = generateBean(fieldTypeMap);
        this.clazz = dynamicBean.getClass();
        Iterator<String> iterator = fieldValuesMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = fieldValuesMap.get(key);
            put(key, value);
        }
    }

    public Map<String, Class<?>> getFields() throws IllegalAccessException {
        Map<String, Class<?>> attrMap = new HashMap<>(16);
        if (clazz != null) {
            Iterator<String> iterator = getValues().keySet().iterator();

            while (iterator.hasNext()) {
                attrMap.put(iterator.next(), Object.class);
            }
        }
        return attrMap;
    }

    /**
     * 获取属性值
     *
     * @param property 设置的字段
     * @return 对象
     * @throws NoSuchFieldException 没有字段
     * @throws IllegalAccessException 反射错误
     */
    public Object get(String property) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = clazz.getDeclaredField("$cglib_prop_" + property);
        declaredField.setAccessible(true);
        Object value = declaredField.get(dynamicBean);
        return value;
    }

    public Object getEntity() {
        return this.dynamicBean;
    }

    private Object generateBean(Map dynAttrMap) {
        BeanGenerator generator = new BeanGenerator();
        if (dynAttrMap != null) {
            Iterator iterator = dynAttrMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                generator.addProperty(key, (Class) dynAttrMap.get(key));
            }
        }
        return generator.create();
    }
}
