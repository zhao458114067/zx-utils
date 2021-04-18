package com.zx.repository.util;

import org.dozer.DozerBeanMapperBuilder;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author: zhaoxu
 * @date: 2021/4/18 21:29
 */
@Component
public class MyBaseConverter {
    private static final Logger log = LoggerFactory.getLogger(MyBaseConverter.class);
    Mapper beanMapper = DozerBeanMapperBuilder.buildDefault();

    public MyBaseConverter() {
    }

    public <S, D> D convertSingleObject(S source, Class<D> clazz) {
        if (source == null) {
            return null;
        } else {
            D dest = null;

            try {
                dest = beanMapper.map(source, clazz);
            } catch (Exception var5) {
                log.error("初始化{}对象失败。", clazz, var5);
            }

            return dest;
        }
    }

    public <S, D> List<D> convertMultiObjectToList(List<S> sourceList, Class<D> destClass) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return null;
        } else {
            List<D> toList = new ArrayList();
            Iterator var4 = sourceList.iterator();

            while(var4.hasNext()) {
                Object src = var4.next();
                toList.add(this.convertSingleObject(src, destClass));
            }

            return toList;
        }
    }

    public <S, D> Map<String, Object> convertMultiObjectToMap(Page<S> srcPages, Class<D> destClass) {
        Map<String, Object> destMap = new HashMap(4);
        List<D> destList = new ArrayList();
        if (srcPages != null && srcPages.getContent() != null) {
            Iterator var6 = srcPages.getContent().iterator();

            while(var6.hasNext()) {
                Object src = var6.next();
                destList.add(this.convertSingleObject(src, destClass));
            }
        }

        destMap.put("total",srcPages.getTotalElements());
        destMap.put("data", destList);
        destMap.put("pageSize", srcPages.getSize());
        destMap.put("current", srcPages.getNumber() + 1);
        return destMap;
    }
}
