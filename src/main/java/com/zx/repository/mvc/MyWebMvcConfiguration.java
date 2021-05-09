package com.zx.repository.mvc;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author : zhaoxu
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MyWebMvcConfiguration implements WebMvcRegistrations {
    public MyWebMvcConfiguration() {
        System.out.println("VersionControlWebMvcConfiguration");
    }

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new MyRequestMappingHandlerMapping();
    }
}