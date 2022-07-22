package com.zx.utils.service.impl;

import com.zx.utils.service.BaseRepository;
import com.zx.utils.util.SpecificationUtil;
import com.zx.utils.util.Utils;
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
 * JPA通用功能扩展
 *
 * @author : zhaoxu
 */
public class BaseRepositoryImpl<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    private EntityManager entityManager;

    private final Class<T> clazz;

    @Autowired(required = false)
    public BaseRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.clazz = entityInformation.getJavaType();
        this.entityManager = entityManager;
    }

    @Override
    public Page<T> findByPage(Map<String, String> objConditions, Integer current, Integer pageSize, List<String> excludeLikeAttr, String sortAttr) {
        Pageable pageable;
        if (!StringUtils.isEmpty(sortAttr)) {
            pageable = PageRequest.of(current - 1, pageSize, Utils.sortAttr(objConditions, sortAttr));
        } else {
            pageable = PageRequest.of(current - 1, pageSize);
        }

        Specification<T> specification = SpecificationUtil.createSpecification(objConditions, clazz, excludeLikeAttr);
        return this.findAll(specification, pageable);
    }

    /**
     * 省去不必要的关联map参数
     *
     * @param objConditions   查询条件
     * @param excludeLikeAttr 是字符串类型，但是不使用模糊查询的字段，可为空
     * @param sortAttr        排序，可为空
     * @return List
     */
    @Override
    public List<T> findByConditions(Map<String, String> objConditions, List<String> excludeLikeAttr, String sortAttr) {
        Specification<T> specification = SpecificationUtil.createSpecification(objConditions, clazz, excludeLikeAttr);

        if (!StringUtils.isEmpty(sortAttr)) {
            return this.findAll(specification, Utils.sortAttr(objConditions, sortAttr));
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
            List<Field> idAnnoation = SpecificationUtil.getTargetAnnoation(clazz, Id.class);
            if (!CollectionUtils.isEmpty(idAnnoation)) {
                Field field = idAnnoation.get(0);
                strings.forEach(id -> {
                    T object = this.findOneByAttr(field.getName(), id);
                    if (object != null) {
                        try {
                            SpecificationUtil.setValue(object, "valid", 0);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        this.save(object);
                    }
                });
            }
        }
    }

    @Override
    public T findOneByAttr(String attr, String condition) {
        Specification<T> specification = SpecificationUtil.createOneSpecification(attr, condition);
        Optional<T> result = this.findOne(specification);

        return result.orElse(null);
    }

    @Override
    public List<T> findByAttr(String attr, String condition) {
        Specification<T> specification = SpecificationUtil.createOneSpecification(attr, condition);
        return this.findAll(specification);
    }
}
