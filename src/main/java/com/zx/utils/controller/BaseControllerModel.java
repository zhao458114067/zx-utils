package com.zx.utils.controller;

import com.zx.utils.annotation.ModelMapping;
import com.zx.utils.constant.Constants;
import com.zx.utils.repository.BaseRepository;
import com.zx.utils.util.BaseConverter;
import com.zx.utils.util.ReflectUtil;
import com.zx.utils.util.SpringManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author: zhaoxu
 * 公用controller
 */
public class BaseControllerModel<S, E> implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(BaseControllerModel.class);

    public BaseRepository<E, Long> baseRepository;

    private Type[] actualTypeArguments;

    private final BaseConverter baseConverter = new BaseConverter();

    @RequestMapping(path = "", method = RequestMethod.POST)
    @ModelMapping
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public synchronized void add(@RequestBody S entityVO) {
        E entity = baseConverter.convertSingleObject(entityVO, (Class<E>) actualTypeArguments[1]);
        try {
            ReflectUtil.setValue(entity, "valid", 1);
        } catch (Exception e) {
            logger.warn("没有找到属性：valid");
        }

        baseRepository.save(entity);
    }

    @RequestMapping(path = "", method = RequestMethod.PUT)
    @ModelMapping
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void update(@RequestBody S entityVO) {
        E entity = baseConverter.convertSingleObject(entityVO, (Class<E>) actualTypeArguments[1]);
        try {
            ReflectUtil.setValue(entity, "valid", 1);
        } catch (Exception e) {
            logger.warn("没有找到属性：valid");
        }
        baseRepository.save(entity);
    }

    @RequestMapping(path = "/{ids}", method = RequestMethod.DELETE)
    @ModelMapping
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteValid(@PathVariable String ids) {
        baseRepository.deleteValid(ids);
    }

    @RequestMapping(path = "/{attr}/{condition}", method = RequestMethod.GET)
    @ModelMapping
    public S findByAttr(@PathVariable String attr, @PathVariable String condition) {
        return baseConverter.convertSingleObject(baseRepository.findOneByAttr(attr, condition), (Class<S>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/list/{attr}/{condition}", method = RequestMethod.GET)
    @ModelMapping
    public List<S> findByAttrs(@PathVariable String attr,
                               @PathVariable String condition) {
        return baseConverter.convertMultiObjectToList((List<?>) baseRepository.findByAttr(attr, condition), (Class<S>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/findAll", method = RequestMethod.GET)
    @ModelMapping
    public List<S> findAllByConditions(@RequestParam Map<String, String> objConditions,
                                       @RequestParam(name = Constants.SORTER, required = false) String sorter,
                                       @RequestParam(name = "excludeLikeAttr", defaultValue = "", required = false) String excludeLikeAttr) {
        List<String> excludeAttrs = Arrays.asList(excludeLikeAttr.split(","));
        List<E> byConditions = baseRepository.findByConditions(objConditions, excludeAttrs, sorter);
        return baseConverter.convertMultiObjectToList(byConditions, (Class<S>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/findByPage", method = RequestMethod.GET)
    @ModelMapping
    public Map<String, Object> findByPage(@RequestParam Map<String, String> objConditions,
                                          @RequestParam(name = Constants.SORTER, required = false) String sorter,
                                          @RequestParam(name = "excludeLikeAttr", defaultValue = "", required = false) String excludeLikeAttr,
                                          @RequestParam(name = Constants.CURRENT) Integer current,
                                          @RequestParam(name = Constants.PAGE_SIZE) Integer pageSize) {
        List<String> excludeAttrs = Arrays.asList(excludeLikeAttr.split(","));
        Page<E> byPage = baseRepository.findByPage(objConditions, current, pageSize, excludeAttrs, sorter);
        return baseConverter.convertMultiObjectToMap(byPage, (Class<?>) actualTypeArguments[0]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(ApplicationArguments args) {
        Class<?> aClass = this.getClass();
        //获取泛型类型
        Type genericSuperclass = aClass.getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        this.actualTypeArguments = actualTypeArguments;
        //查找Repository
        List<String> strings = Arrays.asList(aClass.getName().split("\\."));
        String controllerName = strings.get(strings.size() - 1);
        String serviceApi = Character.toLowerCase(controllerName.charAt(0)) + controllerName.split("Controller")[0].substring(1);
        this.baseRepository = (BaseRepository<E, Long>) SpringManager.getBean(serviceApi + "Repository");
    }
}