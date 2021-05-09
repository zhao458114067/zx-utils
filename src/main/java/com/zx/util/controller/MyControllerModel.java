package com.zx.util.controller;

import com.zx.util.annotation.ModelMapping;
import com.zx.util.service.MyRepository;
import com.zx.util.util.MyBaseConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @author : zhaoxu
 */
@Component
public class MyControllerModel<S> {
    @Autowired
    MyRepository myRepository;

    @Autowired
    MyBaseConverter myBaseConverter;

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
        ParameterizedType paramType = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] actualTypeArguments = paramType.getActualTypeArguments();
        return myBaseConverter.convertMultiObjectToMap(byPage, (Class<?>) actualTypeArguments[0]);
    }
}
