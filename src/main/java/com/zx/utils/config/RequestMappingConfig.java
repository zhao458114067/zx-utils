package com.zx.utils.config;

import com.zx.utils.mvc.BaseRequestMappingHandlerMapping;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author ZhaoXu
 * @date 2022/7/22 14:24
 */
@Configuration
@Lazy
public class RequestMappingConfig implements WebMvcRegistrations {
    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new BaseRequestMappingHandlerMapping();
        handlerMapping.setOrder(0);
        return handlerMapping;
    }
}
