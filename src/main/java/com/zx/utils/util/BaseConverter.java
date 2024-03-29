package com.zx.utils.util;

import com.zx.utils.controller.vo.PageVO;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: zhaoxu
 */
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
            } catch (Exception e) {
                log.error("初始化{}对象失败。", clazz, e);
            }

            return dest;
        }
    }

    public <S, D> List<D> convertMultiObjectToList(List<S> sourceList, Class<D> destClass) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return null;
        } else {
            List<D> toList = new ArrayList<>();
            for (S src : sourceList) {
                toList.add(this.convertSingleObject(src, destClass));
            }
            return toList;
        }
    }

    public <S, D> PageVO<D> convertMultiObjectToPage(Page<S> srcPages, Class<D> destClass) {
        PageVO<D> pageResponse = new PageVO<>();
        List<D> destList = new ArrayList<>();
        if (srcPages != null && srcPages.getContent() != null) {
            for (S srcPage : srcPages) {
                destList.add(this.convertSingleObject(srcPage, destClass));
            }
        }
        pageResponse.setTotal(srcPages.getTotalElements());
        pageResponse.setData(destList);
        pageResponse.setPageSize(srcPages.getSize());
        pageResponse.setCurrent(srcPages.getNumber() + 1);
        return pageResponse;
    }
}
