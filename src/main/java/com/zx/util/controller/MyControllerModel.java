package com.zx.util.controller;

import com.zx.util.annotation.ModelMapping;
import com.zx.util.constant.Constants;
import com.zx.util.service.MyRepository;
import com.zx.util.util.MyBaseConverter;
import com.zx.util.util.SpringManager;
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
import java.util.List;
import java.util.Map;

/**
 * @author: zhaoxu
 */
public class MyControllerModel<S> implements ApplicationRunner {
    public MyRepository myRepository;

    Type[] actualTypeArguments;

    MyBaseConverter myBaseConverter = new MyBaseConverter();

    @RequestMapping(path = "", method = RequestMethod.POST)
    @ModelMapping
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public synchronized void add(@RequestBody S entityVO) {
        myRepository.save(entityVO);
    }

    @RequestMapping(path = "", method = RequestMethod.PUT)
    @ModelMapping
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void update(@RequestBody S entityVO) {
        myRepository.save(entityVO);
    }

    @RequestMapping(path = "/{ids}", method = RequestMethod.DELETE)
    @ModelMapping
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteValid(@PathVariable String ids) {
        myRepository.deleteValid(ids);
    }

    @RequestMapping(path = "/{attr}/{condition}", method = RequestMethod.GET)
    @ModelMapping
    public S findByAttr(@PathVariable String attr, @PathVariable String condition) {
        return (S) myBaseConverter.convertSingleObject(myRepository.findOneByAttr(attr, condition), (Class<?>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/list/{attr}/{condition}", method = RequestMethod.GET)
    @ModelMapping
    public List findByAttrs(@PathVariable String attr,
                            @PathVariable String condition,
                            @RequestParam(name = "conditionType", required = false) String conditionType) {
        if (!StringUtils.isEmpty(conditionType) && Constants.LIST_TYPE.equals(conditionType)) {
            return myBaseConverter.convertMultiObjectToList(myRepository.findByAttrs(attr, condition), (Class<?>) actualTypeArguments[0]);
        }
        return myBaseConverter.convertMultiObjectToList(myRepository.findByAttr(attr, condition), (Class<?>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/findAll", method = RequestMethod.GET)
    @ModelMapping
    public List findAllByConditions(@RequestParam Map<String, Object> tableMap,
                                    @RequestParam(name = "excludeAttr", required = false) String excludeAttr,
                                    @RequestParam(name = "sortAttr", required = false) String sortAttr) {
        if (!StringUtils.isEmpty(excludeAttr) & !StringUtils.isEmpty(sortAttr)) {
            List<String> excludeAttrs = Arrays.asList(excludeAttr.split(","));
            return myBaseConverter.convertMultiObjectToList(myRepository.findByConditions(tableMap, excludeAttrs, sortAttr), (Class<?>) actualTypeArguments[0]);
        } else if (!StringUtils.isEmpty(excludeAttr)) {
            List<String> excludeAttrs = Arrays.asList(excludeAttr.split(","));
            return myBaseConverter.convertMultiObjectToList(myRepository.findByConditions(tableMap, excludeAttrs), (Class<?>) actualTypeArguments[0]);
        } else if (!StringUtils.isEmpty(sortAttr)) {
            return myBaseConverter.convertMultiObjectToList(myRepository.findByConditions(tableMap, null, sortAttr), (Class<?>) actualTypeArguments[0]);
        }
        return myBaseConverter.convertMultiObjectToList(myRepository.findByConditions(tableMap), (Class<?>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/findByPage", method = RequestMethod.GET)
    @ModelMapping
    public Map<String, Object> findByPage(@RequestParam Map<String, Object> tableMap,
                                          @RequestParam(name = "excludeAttr", required = false) String excludeAttr,
                                          @RequestParam(name = "sortAttr", required = false) String sortAttr) {
        if (!StringUtils.isEmpty(excludeAttr) & !StringUtils.isEmpty(sortAttr)) {
            List<String> excludeAttrs = Arrays.asList(excludeAttr.split(","));
            Page byPage = myRepository.findByPage(tableMap, excludeAttrs, sortAttr);
            return myBaseConverter.convertMultiObjectToMap(byPage, (Class<?>) actualTypeArguments[0]);
        } else if (!StringUtils.isEmpty(excludeAttr)) {
            List<String> excludeAttrs = Arrays.asList(excludeAttr.split(","));
            Page byPage = myRepository.findByPage(tableMap, excludeAttrs);
            return myBaseConverter.convertMultiObjectToMap(byPage, (Class<?>) actualTypeArguments[0]);
        } else if (!StringUtils.isEmpty(sortAttr)) {
            Page byPage = myRepository.findByPage(tableMap, null, sortAttr);
            return myBaseConverter.convertMultiObjectToMap(byPage, (Class<?>) actualTypeArguments[0]);
        }
        Page byPage = myRepository.findByPage(tableMap);
        return myBaseConverter.convertMultiObjectToMap(byPage, (Class<?>) actualTypeArguments[0]);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Class<? extends MyControllerModel> aClass = this.getClass();
        //获取泛型类型
        Type genericSuperclass = aClass.getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        this.actualTypeArguments = actualTypeArguments;
        //查找Repository
        List<String> strings = Arrays.asList(aClass.getName().split("\\."));
        String controllerName = strings.get(strings.size() - 1);
        String serviceApi = Character.toLowerCase(controllerName.charAt(0)) + controllerName.split("Controller")[0].substring(1);
        this.myRepository = (MyRepository) SpringManager.getBean(serviceApi + "Repository");
    }
}