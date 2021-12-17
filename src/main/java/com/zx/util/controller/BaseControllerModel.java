package com.zx.util.controller;

import com.alibaba.fastjson.JSONObject;
import com.zx.util.annotation.ModelMapping;
import com.zx.util.constant.Constants;
import com.zx.util.service.BaseRepository;
import com.zx.util.util.MyBaseConverter;
import com.zx.util.util.ReflectUtil;
import com.zx.util.util.SpringManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author: zhaoxu
 * 公用controller
 */
public class BaseControllerModel<S, E> implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(BaseControllerModel.class);

    public BaseRepository myRepository;

    private Type[] actualTypeArguments;

    private MyBaseConverter myBaseConverter = new MyBaseConverter();

    private ReflectUtil reflectUtil = new ReflectUtil();

    @RequestMapping(path = "", method = RequestMethod.POST)
    @ModelMapping
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public synchronized void add(@RequestBody S entityVO) {
        Object entity = myBaseConverter.convertSingleObject(entityVO, (Class<?>) actualTypeArguments[1]);
        try {
            reflectUtil.setValue(entity, "valid", 1);
        } catch (Exception e) {
            logger.warn("没有找到属性：valid");
        }

        myRepository.save(entity);
    }

    @RequestMapping(path = "", method = RequestMethod.PUT)
    @ModelMapping
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void update(@RequestBody S entityVO) {
        Object entity = myBaseConverter.convertSingleObject(entityVO, (Class<?>) actualTypeArguments[1]);
        try {
            reflectUtil.setValue(entity, "valid", 1);
        } catch (Exception e) {
            logger.warn("没有找到属性：valid");
        }
        myRepository.save(entity);
    }

    @RequestMapping(path = "/{ids}", method = RequestMethod.DELETE)
    @ModelMapping
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteValid(@PathVariable String ids) {
        myRepository.deleteValid(ids);
    }

    @RequestMapping(path = "/{attr}/{condition}", method = RequestMethod.GET)
    @ModelMapping
    public Object findByAttr(@PathVariable String attr, @PathVariable String condition) {
        return myBaseConverter.convertSingleObject(myRepository.findOneByAttr(attr, condition), (Class<?>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/list/{attr}/{condition}", method = RequestMethod.GET)
    @ModelMapping
    public List findByAttrs(@PathVariable String attr,
                            @PathVariable String condition,
                            @RequestParam(name = "conditionType", required = false) String conditionType) {
        if (!StringUtils.isEmpty(conditionType) && Constants.LIST_TYPE.equals(conditionType)) {
            return myBaseConverter.convertMultiObjectToList((List<? extends Object>) myRepository.findByAttrs(attr, condition), (Class<?>) actualTypeArguments[0]);
        }
        return myBaseConverter.convertMultiObjectToList((List<? extends Object>)myRepository.findByAttr(attr, condition), (Class<?>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/findAll", method = RequestMethod.GET)
    @ModelMapping
    public List findAllByConditions(@RequestParam Map<String, Object> tableMap,
                                    @RequestParam(name = "excludeAttr", required = false) String excludeAttr) {
        String sortAttr;
        List<String> excludeAttrs;
        //取消模糊查询不为空并且排序不为空
        if (!StringUtils.isEmpty(excludeAttr) & (tableMap.get(Constants.SORTER) != null && !Constants.EMPTY_SORTER.equals(tableMap.get(Constants.SORTER)))) {
            excludeAttrs = Arrays.asList(excludeAttr.split(","));
            JSONObject sorter = JSONObject.parseObject(tableMap.get(Constants.SORTER).toString());
            Iterator<String> iterator = sorter.keySet().iterator();
            sortAttr = iterator.next();
            return myBaseConverter.convertMultiObjectToList((List<? extends Object>) myRepository.findByConditions(tableMap, excludeAttrs, sortAttr), (Class<?>) actualTypeArguments[0]);

        } else if (!StringUtils.isEmpty(excludeAttr)) {
            //取消模糊查询不为空
            excludeAttrs = Arrays.asList(excludeAttr.split(","));
            return myBaseConverter.convertMultiObjectToList((List<? extends Object>)myRepository.findByConditions(tableMap, excludeAttrs), (Class<?>) actualTypeArguments[0]);

        } else if ((tableMap.get(Constants.SORTER) != null && !Constants.EMPTY_SORTER.equals(tableMap.get(Constants.SORTER)))) {
            //排序不为空
            JSONObject sorter = JSONObject.parseObject(tableMap.get(Constants.SORTER).toString());
            Iterator<String> iterator = sorter.keySet().iterator();
            sortAttr = iterator.next();
            return myBaseConverter.convertMultiObjectToList((List<? extends Object>)myRepository.findByConditions(tableMap, null, sortAttr), (Class<?>) actualTypeArguments[0]);

        }

        return myBaseConverter.convertMultiObjectToList((List<? extends Object>)myRepository.findByConditions(tableMap), (Class<?>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/findByPage", method = RequestMethod.GET)
    @ModelMapping
    public Map findByPage(@RequestParam Map<String, Object> tableMap,
                          @RequestParam(name = "excludeAttr", required = false) String excludeAttr) {
        String sortAttr;
        List<String> excludeAttrs;
        //取消模糊查询不为空并且排序不为空
        if (!StringUtils.isEmpty(excludeAttr) & (tableMap.get(Constants.SORTER) != null && !Constants.EMPTY_SORTER.equals(tableMap.get(Constants.SORTER)))) {
            excludeAttrs = Arrays.asList(excludeAttr.split(","));
            JSONObject sorter = JSONObject.parseObject(tableMap.get(Constants.SORTER).toString());
            Iterator<String> iterator = sorter.keySet().iterator();
            sortAttr = iterator.next();
            Page byPage = myRepository.findByPage(tableMap, excludeAttrs, sortAttr);

            return myBaseConverter.convertMultiObjectToMap(byPage, (Class<?>) actualTypeArguments[0]);
        } else if (!StringUtils.isEmpty(excludeAttr)) {
            //取消模糊查询不为空
            excludeAttrs = Arrays.asList(excludeAttr.split(","));
            Page byPage = myRepository.findByPage(tableMap, excludeAttrs);
            return myBaseConverter.convertMultiObjectToMap(byPage, (Class<?>) actualTypeArguments[0]);

        } else if (tableMap.get(Constants.SORTER) != null && !Constants.EMPTY_SORTER.equals(tableMap.get(Constants.SORTER))) {
            //排序不为空
            JSONObject sorter = JSONObject.parseObject(tableMap.get(Constants.SORTER).toString());
            Iterator<String> iterator = sorter.keySet().iterator();
            sortAttr = iterator.next();
            Page byPage = myRepository.findByPage(tableMap, null, sortAttr);
            return myBaseConverter.convertMultiObjectToMap(byPage, (Class<?>) actualTypeArguments[0]);

        }

        Page<E> byPage = myRepository.findByPage(tableMap);
        return myBaseConverter.convertMultiObjectToMap(byPage, (Class<?>) actualTypeArguments[0]);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Class<? extends BaseControllerModel> aClass = this.getClass();
        //获取泛型类型
        Type genericSuperclass = aClass.getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        this.actualTypeArguments = actualTypeArguments;
        //查找Repository
        List<String> strings = Arrays.asList(aClass.getName().split("\\."));
        String controllerName = strings.get(strings.size() - 1);
        String serviceApi = Character.toLowerCase(controllerName.charAt(0)) + controllerName.split("Controller")[0].substring(1);
        this.myRepository = (BaseRepository) SpringManager.getBean(serviceApi + "Repository");
    }
}