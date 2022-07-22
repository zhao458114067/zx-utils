package com.zx.utils.config;

import com.zx.utils.mvc.BaseRequestMappingHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author ZhaoXu
 * @date 2022/7/22 14:24
 */
@Configuration
public class RequestMappingConfig extends WebMvcConfigurationSupport {
    @Override
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new BaseRequestMappingHandlerMapping();
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("static/**")
//                .addResourceLocations("classpath:/META-INF/resources/static/");
//    }
}
