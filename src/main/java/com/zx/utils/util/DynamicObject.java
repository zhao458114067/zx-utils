package com.zx.utils.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.cglib.beans.BeanGenerator;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author: zhaoxu
 * @description:
 */
@Data
@NoArgsConstructor
public class DynamicObject {

    Object dynamicBean;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    Class clazz;

    public DynamicObject(Map dynAttrMap) {
        this.dynamicBean = generateBean(dynAttrMap);
        clazz = dynamicBean.getClass();
    }

    public DynamicObject(Object object) throws IllegalAccessException, NoSuchFieldException {
        dynamicBean = generateBean(getFields(object));
        Map<String, Object> values = getValues(object);
        Iterator<String> iterator = values.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = values.get(key);
            put(key, value);
        }
        clazz = dynamicBean.getClass();
    }

    public static DynamicObject parseMap(Map<String, Object> targetMap) throws NoSuchFieldException, IllegalAccessException {
        DynamicObject dynmicVO = new DynamicObject();
        for (Map.Entry<String, Object> entry : targetMap.entrySet()) {
            dynmicVO.put(entry.getKey(), entry.getValue());
        }
        return dynmicVO;
    }

    public static DynamicObject parseString(String jsonString) throws NoSuchFieldException, IllegalAccessException {
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        return parseMap(jsonObject);
    }

    /**
     * 获取所有属性值
     *
     * @return
     * @throws IllegalAccessException
     */
    public Map<String, Object> getValues() throws IllegalAccessException {
        Map<String, Object> fieldValuesMap = new HashMap<>(16);
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
     * 设置属性值，不存在就添加
     *
     * @param property
     * @param value
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
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
     * @param object
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public void putAll(Object object) throws IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = object.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        Map<String, Object> fieldValuesMap = new HashMap<>(16);
        Map<String, Object> fieldTypeMap = new HashMap<>(16);
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
     * 获取属性值
     *
     * @param property 设置的字段
     * @return JsonNode对象
     */
    public JsonNode get(String property) {
        JsonNode jsonNode = OBJECT_MAPPER.valueToTree(dynamicBean);
        return jsonNode.get(property);
    }

    /**
     * 获取属性值
     *
     * @param property 设置的字段
     * @return 属性对应的对象
     * @throws NoSuchFieldException   没有字段
     * @throws IllegalAccessException 反射错误
     */
    @SuppressWarnings("unchecked")
    public <E extends Object> E getToObject(String property) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = clazz.getDeclaredField("$cglib_prop_" + property);
        declaredField.setAccessible(true);
        Class<?> type = declaredField.getType();
        return (E) declaredField.get(dynamicBean);
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


    /**
     * 转换请求返回的data数据
     *
     * @param object
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T convertDataToJavaBean(Object object, Class<T> tClass) {
        try {
            String jsonString = OBJECT_MAPPER.writeValueAsString(object);
            JSONObject jsonObject = JSON.parseObject(jsonString);
            // 实体
            return JSON.toJavaObject(jsonObject, tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 转换请求返回的data数据
     *
     * @param object
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> List<T> convertDataToJavaBeanList(Object object, Class<T> tClass) {
        List<T> list = new ArrayList<>();
        try {
            String jsonString = OBJECT_MAPPER.writeValueAsString(object);
            JSONArray jsonArray = JSONArray.parseArray(jsonString);
            for (Object arrayObj : jsonArray) {
                T t = convertDataToJavaBean(arrayObj, tClass);
                list.add(t);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
