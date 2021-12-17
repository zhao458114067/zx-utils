package com.zx.util.config;

import com.zx.util.factory.BaseJpaRepositoryFactoryBean;
import com.zx.util.mvc.BaseRequestMappingHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
/**
 * @author : zhaoxu
 * 这个是自定义repository工厂
 */
@EnableJpaRepositories(basePackages = {"com.zx.util"}, repositoryFactoryBeanClass = BaseJpaRepositoryFactoryBean.class)
public class BaseJpaRepositoryConfig extends WebMvcConfigurationSupport {
    @Override
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new BaseRequestMappingHandlerMapping();
    }
}