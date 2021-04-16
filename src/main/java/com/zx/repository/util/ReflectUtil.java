package com.zx.repository.util;

import com.zx.repository.constant.Constants;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author: zhaoxu
 * @description: 反射封装工具类
 */
@Component
public class ReflectUtil {
    /**
     * 生成全属性条件查询通用Specification
     *
     * @param tableMap    属性参数
     * @param clazz       要查询的实体类或vo类
     * @param excludeAttr 不使用模糊搜索的字符串属性
     * @param map         外键关联查询
     * @param <S>
     * @return
     */
    public <S> Specification<S> createSpecification(Map<String, String> tableMap, Class clazz, List<String> excludeAttr, Map map) {
        Specification<S> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //未删除的数据
            try {
                clazz.getDeclaredField(Constants.VALID);
                predicates.add(cb.equal(root.get(Constants.VALID), 1));
            } catch (NoSuchFieldException e) {

            }

            Field[] declaredFields = clazz.getDeclaredFields();

            if (tableMap != null) {
                for (Field field : declaredFields) {
                    String fieldName = field.getName();
                    if (!StringUtils.isEmpty(tableMap.get(fieldName))) {
                        String typeName = field.getGenericType().getTypeName();
                        Class<?> aClass = null;
                        try {
                            aClass = Class.forName(typeName);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        //属性不包含特定的属性并且是字符串采用模糊搜索
                        if (aClass == String.class && (CollectionUtils.isEmpty(excludeAttr) || !excludeAttr.contains(fieldName))) {
                            String queryFieldName = "%" + tableMap.get(fieldName).replace("/", "\\/")
                                    .replaceAll("_", "\\\\_").replaceAll("%", "\\\\%") + "%";
                            predicates.add(cb.like(root.get(fieldName), queryFieldName));
                        } else {
                            predicates.add(cb.equal(root.get(fieldName), tableMap.get(fieldName)));
                        }
                    }
                }
                if (!CollectionUtils.isEmpty(map)) {
                    Iterator iterator = map.keySet().iterator();
                    while (iterator.hasNext()) {
                        String sourceKey = iterator.next().toString();
                        Map mapping = (Map) map.get(sourceKey);
                        Join join = root.join(sourceKey, JoinType.INNER);
                        Iterator mappingItr = mapping.keySet().iterator();
                        while (mappingItr.hasNext()) {
                            String joinKey = mappingItr.next().toString();
                            String joinAttr = mapping.get(joinKey).toString();
                            if (!StringUtils.isEmpty(tableMap.get(joinKey))) {
                                predicates.add(cb.equal(join.get(joinAttr), tableMap.get(joinKey)));
                            }
                        }
                    }
                }
            }
            Predicate[] pre = new Predicate[predicates.size()];
            Predicate preAnd = cb.and(predicates.toArray(pre));
            return query.where(preAnd).getRestriction();
        };
        return specification;
    }

    /**
     * 通过方法名动态执行某个方法
     *
     * @param clazz
     * @param o
     * @param methodName
     * @param parameters
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    public Object executeMethod(Class<?> clazz, Object o, String methodName, Object... parameters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ArrayList<Class<?>> paramTypeList = new ArrayList<>();
        for (Object paramType : parameters) {
            paramTypeList.add(paramType.getClass());
        }
        Class<?>[] classArray = new Class[paramTypeList.size()];
        Method method = clazz.getMethod(methodName, paramTypeList.toArray(classArray));
        Object invoke = method.invoke(o, parameters);
        return invoke;
    }

    /**
     * 获取所有属性值
     *
     * @return
     * @throws IllegalAccessException
     */
    public Map<String, Object> getFieldsValue(Class clazz, Object object) throws IllegalAccessException {
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
    public Boolean setValue(Class clazz, Object object, String property, Object value) {
        try {
            Field declaredField = clazz.getDeclaredField(property);
            declaredField.setAccessible(true);
            declaredField.set(object, value);
        } catch (NoSuchFieldException e) {
            return false;
        } catch (IllegalAccessException e) {
            return false;
        }
        return true;
    }

}