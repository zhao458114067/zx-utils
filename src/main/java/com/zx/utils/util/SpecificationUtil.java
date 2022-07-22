package com.zx.utils.util;

import com.zx.utils.constant.Constants;
import com.zx.utils.constant.ErrorCodeEnum;
import com.zx.utils.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 反射封装工具类
 *
 * @author : zhaoxu
 */
public class SpecificationUtil {
    static final Logger logger = LoggerFactory.getLogger(SpecificationUtil.class);

    /**
     * 生成全属性条件查询通用Specification
     *
     * @param objConditions   属性参数
     * @param clazz           要查询的实体类或vo类
     * @param excludeLikeAttr 不使用模糊搜索的字符串属性
     * @param <E>             泛型
     * @return Specification
     */
    public static <E> Specification<E> createSpecification(Map<String, String> objConditions, Class<E> clazz, List<String> excludeLikeAttr) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //未删除的数据
            try {
                clazz.getDeclaredField(Constants.VALID);
                if (!StringUtils.isEmpty(objConditions.get(Constants.VALID))) {
                    predicates.add(cb.equal(root.get(Constants.VALID), Integer.valueOf(objConditions.get(Constants.VALID))));
                } else {
                    predicates.add(cb.equal(root.get(Constants.VALID), 1));
                }
            } catch (NoSuchFieldException e) {
                logger.warn("没有找到属性：valid");
            }

            Field[] declaredFields = clazz.getDeclaredFields();

            for (Field field : declaredFields) {
                String fieldName = field.getName();
                String condition = objConditions.get(fieldName);
                if (!StringUtils.isEmpty(condition)) {
                    String typeName = field.getGenericType().getTypeName();
                    Class<?> aClass;
                    try {
                        aClass = Class.forName(typeName);
                    } catch (ClassNotFoundException e) {
                        throw new BaseException(ErrorCodeEnum.CANNOT_FIND_ATTR_ERROR.getErrorCode(), ErrorCodeEnum.CANNOT_FIND_ATTR_ERROR.getErrorMessage());
                    }
                    //属性不包含特定的属性并且是字符串采用模糊搜索
                    boolean isLike = aClass == String.class && (CollectionUtils.isEmpty(excludeLikeAttr) || !excludeLikeAttr.contains(fieldName));
                    if (isLike) {
                        String queryFieldName = "%" + condition.replace("/", "\\/")
                                .replaceAll("_", "\\\\_").replaceAll("%", "\\\\%") + "%";
                        predicates.add(cb.like(root.get(fieldName), queryFieldName));
                    } else {
                        List<String> conditionList = Arrays.asList(condition.split(","));
                        predicates.add(cb.and(root.get(fieldName).in(conditionList)));
                    }
                }
            }

            //外键关联查询，新，可以省去map参数
            if (!CollectionUtils.isEmpty(objConditions)) {
                for (Map.Entry<String, String> entry : objConditions.entrySet()) {
                    if (entry.getKey().contains(".")) {
                        //递归解析真实的path
                        List<String> conditionList = Arrays.asList(entry.getValue().split(","));
                        predicates.add(cb.and(getRootPath(root, null, entry.getKey()).in(conditionList)));
                    }
                }
            }
            Predicate[] pre = new Predicate[predicates.size()];
            Predicate preAnd = cb.and(predicates.toArray(pre));
            return query.where(preAnd).getRestriction();
        };
    }

    /**
     * 指定条件查询
     *
     * @param attr      查询的字段
     * @param condition 条件
     * @param <E>       泛型
     * @return Specification
     */
    public static <E> Specification<E> createOneSpecification(String attr, String condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //未删除的数据
            try {
                if (Constants.VALID.equals(attr)) {
                    predicates.add(cb.equal(root.get(Constants.VALID), condition));
                } else {
                    predicates.add(cb.equal(root.get(Constants.VALID), 1));
                }
            } catch (Exception e) {
                logger.warn("没有找到属性：valid");
            }

            //外键关联查询
            if (attr.contains(Constants.POINT)) {
                //递归解析真实的path
                predicates.add(cb.equal(getRootPath(root, null, attr), condition));
            } else {
                List<String> conditionList = Arrays.asList(condition.split(","));
                predicates.add(cb.and(root.get(attr).in(conditionList)));
            }

            Predicate[] pre = new Predicate[predicates.size()];
            Predicate preAnd = cb.and(predicates.toArray(pre));
            return query.where(preAnd).getRestriction();
        };
    }

    /**
     * 获取关联查询真实path
     *
     * @param root    root
     * @param path    path
     * @param allPath allPath
     * @param <E>     E
     * @return Path
     */
    public static <E> Path<E> getRootPath(Root<E> root, Path<E> path, String allPath) {
        List<String> pathList = Arrays.asList(allPath.split("\\."));
        //下一个解析的path
        StringBuilder restPath = new StringBuilder();
        Path<E> nowPath = null;
        if (!CollectionUtils.isEmpty(pathList)) {
            if (root != null) {
                nowPath = root.get(pathList.get(0));
                //拥有下一个解析点
                if (pathList.size() > 1) {
                    for (int i = 1; i < pathList.size(); i++) {
                        restPath.append(pathList.get(i));
                        if (i + 1 < pathList.size()) {
                            restPath.append(".");
                        }
                    }
                    //递归
                    nowPath = getRootPath(null, nowPath, restPath.toString());
                }
            } else {
                nowPath = path.get(pathList.get(0));
                //拥有下一个解析点
                if (pathList.size() > 1) {
                    for (int i = 1; i < pathList.size(); i++) {
                        restPath.append(pathList.get(i));
                        if (i + 1 < pathList.size()) {
                            restPath.append(".");
                        }
                    }
                    //递归
                    nowPath = getRootPath(root, nowPath, restPath.toString());
                }
            }
        }
        return nowPath;
    }

    /**
     * 通过方法名动态执行某个方法
     *
     * @param object     object
     * @param methodName 方法名
     * @param parameters 参数
     * @return Object
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException    IllegalAccessException
     * @throws NoSuchMethodException     NoSuchMethodException
     */
    public static Object executeMethod(Object object, String methodName, Object... parameters) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
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
     * @param object object
     * @return Map
     * @throws IllegalAccessException IllegalAccessException
     */
    public static Map<String, Object> getFieldsValue(Object object) throws IllegalAccessException {
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
     * @param property 设置的字段
     * @param value    值
     * @param object   object
     * @return Boolean
     */
    public static Boolean setValue(Object object, String property, Object value) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field declaredField = clazz.getDeclaredField(property);
        declaredField.setAccessible(true);
        declaredField.set(object, value);
        return true;
    }

    /**
     * 获取对象所有属性及对应的类别
     *
     * @param object object
     * @return Map
     * @throws IllegalAccessException IllegalAccessException
     */
    public static Map<String, Class<?>> getFields(Object object) throws IllegalAccessException {
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
     * @param object object
     * @return Map
     * @throws IllegalAccessException IllegalAccessException
     */
    public static Map<String, Object> getValues(Object object) throws IllegalAccessException {
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

    /**
     * 获取拥有指定注解的字段
     *
     * @param objectClass 对象
     * @param annoClass   查询的注解
     * @return List
     */
    public static List<Field> getTargetAnnoation(Class<?> objectClass, Class<? extends Annotation> annoClass) {
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