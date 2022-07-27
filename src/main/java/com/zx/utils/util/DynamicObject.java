package com.zx.utils.util;

import com.alibaba.fastjson.JSON;
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

    private Object dynamicBean;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    Class clazz;

    public DynamicObject(Object object) {
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

    public static DynamicObject parseMap(Map<String, Object> targetMap) {
        DynamicObject dynamicObject = new DynamicObject();
        for (Map.Entry<String, Object> entry : targetMap.entrySet()) {
            dynamicObject.put(entry.getKey(), entry.getValue());
        }
        return dynamicObject;
    }

    public static DynamicObject parseString(String jsonString) {
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        return parseMap(jsonObject);
    }

    /**
     * 获取所有属性值
     *
     * @return
     * @throws IllegalAccessException
     */
    public Map<String, Object> getValues() {
        Map<String, Object> fieldValuesMap = new HashMap<>(16);
        if (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(dynamicBean);
                    fieldValuesMap.put(field.getName().split("\\$cglib_prop_")[1], fieldValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
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
    public Map<String, Object> getValues(Object object) {
        Map<String, Object> fieldValuesMap = new HashMap<>(16);
        Class<?> clazz = object.getClass();
        if (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(object);
                    fieldValuesMap.put(field.getName(), fieldValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
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
    public void put(String property, Object value) {
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
                try {
                    Field field = clazz.getDeclaredField("$cglib_prop_" + putKey);
                    field.setAccessible(true);
                    field.set(dynamicBean, putValue);
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return;
        }
        declaredField.setAccessible(true);
        try {
            declaredField.set(dynamicBean, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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

    public Map<String, Class<?>> getFields() {
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
    public Map<String, Class<?>> getFields(Object object) {
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
     * 获取返回的data数据
     *
     * @param response
     * @return
     */
    public static JsonNode getResponseData(Map<String, Object> response) {
        return parseMap(response).get("result").get("data");
    }

    /**
     * 转换请求返回的data数据
     *
     * @param jsonNode
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T convertDataToJavaBean(JsonNode jsonNode, Class<T> tClass) {
        try {
            String jsonString = OBJECT_MAPPER.writeValueAsString(jsonNode);
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
     * @param jsonNode
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> List<T> convertDataToJavaBeanList(JsonNode jsonNode, Class<T> tClass) {
        List<T> list = new ArrayList<>();
        for (JsonNode node : jsonNode) {
            T t = convertDataToJavaBean(node, tClass);
            list.add(t);
        }
        return list;
    }
}
