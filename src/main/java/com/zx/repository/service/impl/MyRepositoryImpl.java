package com.zx.repository.service.impl;

import com.zx.repository.constant.Constants;
import com.zx.repository.service.MyRepository;
import com.zx.repository.util.ReflectUtil;
import com.zx.repository.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author: zhaoxu
 * @description: JPA通用功能扩展
 */
public class MyRepositoryImpl<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> implements MyRepository<T, ID> {

    private ReflectUtil reflectUtil = new ReflectUtil();

    private Utils utils = new Utils();

    private EntityManager entityManager;

    private Class<T> clazz;

    @Autowired(required = false)
    public MyRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.clazz = entityInformation.getJavaType();
        this.entityManager = entityManager;
    }

    @Override
    public Page<T> findByPage(Map<String, String> tableMap, List<String> excludeAttr, Map joinField, String sortAttr) throws NullPointerException {
        int current = Integer.valueOf(tableMap.get(Constants.CURRENT));
        int pageSize = Integer.valueOf(tableMap.get(Constants.PAGE_SIZE));

        Pageable pageable;
        if (!StringUtils.isEmpty(sortAttr)) {
            pageable = PageRequest.of(current - 1, pageSize, utils.sortAttr(tableMap, sortAttr));
        } else {
            pageable = PageRequest.of(current - 1, pageSize);
        }

        Specification<T> specification = reflectUtil.createSpecification(tableMap, clazz, excludeAttr, joinField);
        return this.findAll(specification, pageable);
    }

    @Override
    public List<T> findByConditions(Map<String, String> tableMap, List<String> excludeAttr, Map joinField, String sortAttr) {
        Specification<T> specification = reflectUtil.createSpecification(tableMap, clazz, excludeAttr, joinField);

        if (!StringUtils.isEmpty(sortAttr)) {
            return this.findAll(specification, utils.sortAttr(tableMap, sortAttr));
        } else {
            return this.findAll(specification);
        }

    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteValid(String ids) {
        List<String> strings = Arrays.asList(ids.split(","));
        if (!CollectionUtils.isEmpty(strings)) {
            //获取主键
            List<Field> idAnnoation = utils.getTargetAnnoation(clazz, Id.class);
            if (!CollectionUtils.isEmpty(idAnnoation)) {
                Field field = idAnnoation.get(0);

                strings.stream().forEach(id -> {
                    T object = this.findByAttr(field.getName(), id);
                    if (object != null) {
                        reflectUtil.setValue(object, "valid", 0);
                        reflectUtil.setValue(object, "gmtModified", utils.getNowDate());
                        this.save(object);
                    }
                });
            }
        }
    }

    @Override
    public T findByAttr(String attr, String condition) {
        Specification<T> specification = reflectUtil.createOneSpecification(attr, condition);
        Optional<T> result = this.findOne(specification);

        if (result.isPresent()) {
            return result.get();
        } else {
            return null;
        }
    }

    @Override
    public List<T> findByAttrs(String attr, String conditions) {
        List<T> results = new ArrayList<>();
        if(!StringUtils.isEmpty(conditions)){
            List<String> cons = Arrays.asList(conditions.split(","));
            cons.stream().forEach(condition -> {
                T byAttr = findByAttr(attr, condition);
                if (byAttr != null) {
                    results.add(byAttr);
                }
            });
        }
        return results;
    }
}
