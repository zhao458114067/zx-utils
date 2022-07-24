package com.zx.utils.util;

import com.zx.utils.controller.vo.PageVO;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author : zhaoxu
 */
@Component
public class BaseConverter {
    private static final Logger log = LoggerFactory.getLogger(BaseConverter.class);
    Mapper beanMapper = new DozerBeanMapper();

    public BaseConverter() {
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

            while (var4.hasNext()) {
                Object src = var4.next();
                toList.add(this.convertSingleObject(src, destClass));
            }

            return toList;
        }
    }

    public <S, D> PageVO<D> convertMultiObjectToMap(Page<S> srcPages, Class<D> destClass) {
        PageVO<D> pageVO = new PageVO<>();
        List<D> destList = new ArrayList<>();
        if (srcPages != null && srcPages.getContent() != null) {
            Iterator var6 = srcPages.getContent().iterator();

            while (var6.hasNext()) {
                Object src = var6.next();
                destList.add(this.convertSingleObject(src, destClass));
            }
        }

        pageVO.setTotal(srcPages.getTotalElements());
        pageVO.setData(destList);
        pageVO.setPageSize(srcPages.getSize());
        pageVO.setCurrent(srcPages.getNumber() + 1);
        return pageVO;
    }
}
