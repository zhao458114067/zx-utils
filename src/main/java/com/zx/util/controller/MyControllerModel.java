package com.zx.util.controller;

import com.zx.util.annotation.ModelMapping;
import com.zx.util.service.MyRepository;
import com.zx.util.util.MyBaseConverter;
import com.zx.util.util.SpringManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
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
    public void save(@RequestBody S entityVO) {
        myRepository.save(entityVO);
    }

    @RequestMapping(path = "", method = RequestMethod.PUT)
    @ModelMapping
    public void update(@RequestBody S entityVO) {
        myRepository.save(entityVO);
    }

    @RequestMapping(path = "/{ids}", method = RequestMethod.DELETE)
    @ModelMapping
    public void deleteValid(@PathVariable String ids) {
        myRepository.deleteValid(ids);
    }

    @RequestMapping(path = "/{attr}/{condition}", method = RequestMethod.GET)
    @ModelMapping
    public S findByAttr(@PathVariable String attr, @PathVariable String condition) {
        return (S) myRepository.findOneByAttr(attr, condition);
    }

    @RequestMapping(path = "/findAll", method = RequestMethod.GET)
    @ModelMapping
    public List findAllByConditions(@RequestParam Map tableMap) {
        return myRepository.findByConditions(tableMap);
    }

    @RequestMapping(path = "/findByPage", method = RequestMethod.GET)
    @ModelMapping
    public Map<String, Object> findByPage(@RequestParam Map tableMap) {
        Page byPage = myRepository.findByPage(tableMap);
        return myBaseConverter.convertMultiObjectToMap(byPage, (Class<?>) actualTypeArguments[0]);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Class<? extends MyControllerModel> aClass = this.getClass();
        Class<?> superclass = this.getClass().getSuperclass();
        List<String> strings = Arrays.asList(aClass.getName().split("\\."));
        String controllerName = strings.get(strings.size() - 1);
        String serviceApi = Character.toLowerCase(controllerName.charAt(0)) + controllerName.split("Controller")[0].substring(1);
        this.myRepository = (MyRepository) SpringManager.getBean(serviceApi + "Repository");
    }
}