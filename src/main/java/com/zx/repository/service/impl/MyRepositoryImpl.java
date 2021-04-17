package com.zx.repository.service.impl;

import com.zx.repository.constant.Constants;
import com.zx.repository.constant.ErrorCodeEnum;
import com.zx.repository.exception.MyException;
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
import java.io.Serializable;
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
    public Page<T> findByPage(Map<String, String> tableMap, List<String> excludeAttr, Map joinField, String sortAttr) {
        int current = 0;
        int pageSize = 0;
        try {
            current = Integer.valueOf(tableMap.get(Constants.CURRENT));
            pageSize = Integer.valueOf(tableMap.get(Constants.PAGE_SIZE));
        } catch (Exception e) {
            throw new MyException(ErrorCodeEnum.PARAMS_ERROR.getErrorCode(), ErrorCodeEnum.PARAMS_ERROR.getErrorMessage());
        }

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
            strings.stream().forEach(id -> {
                Object object = this.findById((ID) Long.valueOf(id)).get();
                reflectUtil.setValue(clazz, object, "valid", 0);
                reflectUtil.setValue(clazz, object, "gmtModified", utils.getNowDate());
            });
        }
    }

    @Override
    public T findByAttr(String attr, String condition) {
        List<T> resultList = new ArrayList<>();
        List<String> excludeAttr = new ArrayList<>();

        Map<String, String> tableMap = new HashMap<>(4);
        tableMap.put(attr, condition);

        excludeAttr.add(attr);

        Specification<T> specification = reflectUtil.createSpecification(tableMap, clazz, excludeAttr, null);
        resultList.addAll(this.findAll(specification));

        if (!CollectionUtils.isEmpty(resultList)) {
            return resultList.get(0);
        } else {
            return null;
        }
    }
}
