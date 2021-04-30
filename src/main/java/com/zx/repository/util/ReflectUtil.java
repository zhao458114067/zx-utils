package com.zx.repository.util;

import com.zx.repository.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    static final Logger logger = LoggerFactory.getLogger(ReflectUtil.class);

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
                if (!StringUtils.isEmpty(tableMap.get(Constants.VALID))) {
                    predicates.add(cb.equal(root.get(Constants.VALID), Integer.valueOf(tableMap.get(Constants.VALID))));
                } else {
                    predicates.add(cb.equal(root.get(Constants.VALID), 1));
                }
            } catch (NoSuchFieldException e) {
                logger.warn("没有找到 {valid} 属性");
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
     * 指定条件查询
     *
     * @param attr
     * @param condition
     * @param <S>
     * @return
     */
    public <S> Specification<S> createOneSpecification(String attr, String condition) {
        Specification<S> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //未删除的数据
            try {
                predicates.add(cb.equal(root.get(Constants.VALID), 1));
            } catch (Exception e) {
                logger.warn("没有找到 {valid} 属性");
            }

            predicates.add(cb.equal(root.get(attr), condition));

            Predicate[] pre = new Predicate[predicates.size()];
            Predicate preAnd = cb.and(predicates.toArray(pre));
            return query.where(preAnd).getRestriction();
        };
        return specification;
    }

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
        Map<String, Object> fieldValuesMap = new HashMap(16);
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

}