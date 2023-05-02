package com.zx.utils.controller;

import com.zx.utils.annotation.ModelMapping;
import com.zx.utils.constant.Constants;
import com.zx.utils.controller.vo.PageVO;
import com.zx.utils.repository.BaseRepository;
import com.zx.utils.util.BaseConverter;
import com.zx.utils.util.ReflectUtil;
import com.zx.utils.util.SpringManager;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
    @ApiOperation(value = "新增", notes = "")
    @Transactional(rollbackFor = Exception.class)
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
    @ApiOperation(value = "更新", notes = "必须传数据库id值")
    @Transactional(rollbackFor = Exception.class)
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
    @ApiOperation(value = "信息删除", notes = "多个用逗号隔开")
    @Transactional(rollbackFor = Exception.class)
    public void deleteValid(@PathVariable String ids) {
        baseRepository.deleteValid(ids);
    }

    @RequestMapping(path = "/{attr}/{condition}", method = RequestMethod.GET)
    @ModelMapping
    @ApiOperation(value = "单条件查询", notes = "对象的所有属性可作为条件进行查询，返回一个设备对象的实体信息。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attr", value = "对象属性例如：id", required = true),
            @ApiImplicitParam(name = "condition", value = "条件例如：1", required = true)
    })
    public S findOneByAttr(@PathVariable String attr, @PathVariable String condition) {
        return baseConverter.convertSingleObject(baseRepository.findOneByAttr(attr, condition), (Class<S>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/list/{attr}/{condition}", method = RequestMethod.GET)
    @ModelMapping
    @ApiOperation(value = "条件查询，精准查询，可作为条件的为对象的所有属性，返回一个列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attr", value = "对象属性例如：id", required = true),
            @ApiImplicitParam(name = "condition", value = "条件例如：1,2,3", required = true)
    })
    public List<S> findByAttrs(@PathVariable String attr,
                               @PathVariable String condition) {
        return baseConverter.convertMultiObjectToList((List<?>) baseRepository.findByAttr(attr, condition), (Class<S>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/findAll", method = RequestMethod.GET)
    @ModelMapping
    @ApiOperation(value = "多条件组合查询所有，字符串默认使用模糊搜索", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.SORTER, value = "排序条件：sorter={\"id\":\"descend\"}，ascend升序，descend降序"),
            @ApiImplicitParam(name = "excludeLikeAttr", value = "是字符串类型属性但不使用模糊查询的字段，逗号隔开")
    })
    public List<S> findAllByConditions(@RequestBody(required = false) S reqObj,
                                       @RequestParam(required = false) Map<String, String> reqReplaceMap,
                                       @RequestParam(name = Constants.SORTER, required = false) String sorter,
                                       @RequestParam(name = "excludeLikeAttr", defaultValue = "", required = false) String excludeLikeAttr) throws IllegalAccessException {
        List<String> excludeAttrs = Arrays.asList(excludeLikeAttr.split(","));
        List<E> byConditions = baseRepository.findByConditions(reqReplaceMap, excludeAttrs, sorter);
        return baseConverter.convertMultiObjectToList(byConditions, (Class<S>) actualTypeArguments[0]);
    }

    @RequestMapping(path = "/findByPage", method = RequestMethod.GET)
    @ModelMapping
    @ApiOperation(value = "多条件组合分页查询，字符串默认使用模糊搜索", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = Constants.SORTER, value = "排序条件：sorter={\"id\":\"descend\"}，ascend升序，descend降序"),
            @ApiImplicitParam(name = "excludeLikeAttr", value = "是字符串类型属性但不使用模糊查询的字段，逗号隔开"),
            @ApiImplicitParam(name = Constants.CURRENT, value = "当前页默认第 1 页"),
            @ApiImplicitParam(name = Constants.PAGE_SIZE, value = "每页数据条数默认 20 条")
    })
    public PageVO<S> findByPage(@RequestBody(required = false) S reqObj,
                                @RequestParam(required = false) Map<String, String> reqReplaceMap,
                                @RequestParam(name = Constants.SORTER, required = false) String sorter,
                                @RequestParam(name = "excludeLikeAttr", defaultValue = "", required = false) String excludeLikeAttr,
                                @RequestParam(name = Constants.CURRENT, required = false, defaultValue = "1") Integer current,
                                @RequestParam(name = Constants.PAGE_SIZE, required = false, defaultValue = "20") Integer pageSize) throws IllegalAccessException {
        List<String> excludeAttrs = Arrays.asList(excludeLikeAttr.split(","));
        Page<E> byPage = baseRepository.findByPage(reqReplaceMap, current, pageSize, excludeAttrs, sorter);
        return (PageVO<S>) baseConverter.convertMultiObjectToPage(byPage, (Class<?>) actualTypeArguments[0]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(ApplicationArguments args) {
        Class<?> aClass = this.getClass();
        // 获取泛型类型
        Type genericSuperclass = aClass.getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        this.actualTypeArguments = actualTypeArguments;
        // 查找Repository
        List<String> strings = Arrays.asList(aClass.getName().split("\\."));
        String controllerName = strings.get(strings.size() - 1);
        String serviceApi = Character.toLowerCase(controllerName.charAt(0)) + controllerName.split("Controller")[0].substring(1);
        this.baseRepository = (BaseRepository<E, Long>) SpringManager.getBean(serviceApi + "Repository");
    }
}