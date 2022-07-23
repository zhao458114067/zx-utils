package com.zx.utils.mvc;

import com.zx.utils.annotation.ModelMapping;
import com.zx.utils.constant.Constants;
import lombok.SneakyThrows;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author : zhaoxu
 */
public class BaseRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();

    public BaseRequestMappingHandlerMapping() {
    }

    @SneakyThrows
    @Override
    public void afterPropertiesSet() {
        Field config = this.getClass().getSuperclass().getDeclaredField("config");
        config.setAccessible(true);
        this.config = (RequestMappingInfo.BuilderConfiguration) config.get(this);
        super.afterPropertiesSet();
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        // 读取方法上的RequestMapping注解信息
        RequestMapping methodAnnotation = method.getDeclaredAnnotation(RequestMapping.class);
        if (method.isAnnotationPresent(ModelMapping.class)) {
            RequestCondition methodCondition = getCustomMethodCondition(method);
            info = createRequestMappingInfo(methodAnnotation, methodCondition);

            // 读取类上的RequestMapping注解信息
            RequestMapping typeAnnotation = AnnotationUtils.findAnnotation(handlerType, RequestMapping.class);

            // 生成类上的匹配条件,并合并方法上的
            List<String> strings = Arrays.asList(handlerType.getName().split("\\."));
            if (CollectionUtils.isEmpty(strings)) {
                return null;
            }
            String controllerName = strings.get(strings.size() - 1);
            String serviceApi = Character.toLowerCase(controllerName.charAt(0)) + controllerName.split("Controller")[0].substring(1);
            serviceApi = Constants.URL_PREFIX + serviceApi;
            RequestCondition typeCondition = getCustomTypeCondition(handlerType);
            if (typeAnnotation != null) {
                // 生成类上的匹配条件,并合并方法上的
                info = createRequestMappingInfo(typeAnnotation, typeCondition).combine(RequestMappingInfo.paths(serviceApi).build().combine(info));
            } else {
                info = RequestMappingInfo.paths(serviceApi).build().combine(info);
            }
        }
        return info;
    }

    @Override
    protected RequestMappingInfo createRequestMappingInfo(RequestMapping requestMapping, RequestCondition customCondition) {
        return RequestMappingInfo
                .paths(resolveEmbeddedValuesInPatterns(requestMapping.path()))
                .methods(requestMapping.method())
                .params(requestMapping.params())
                .headers(requestMapping.headers())
                .consumes(requestMapping.consumes())
                .produces(requestMapping.produces())
                .mappingName(requestMapping.name())
                .customCondition(customCondition)
                .options(this.config)
                .build();
    }
}