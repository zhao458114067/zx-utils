package com.zx.utils.config;

import com.zx.utils.factory.BaseJpaRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author : zhaoxu
 * 重写requestMapping
 */
@Configuration
@EnableJpaRepositories(basePackages = {"**.repository"}, repositoryFactoryBeanClass = BaseJpaRepositoryFactoryBean.class)
@EntityScan(basePackages = {"**.entity"})
public class BaseJpaRepositoryConfig {

}
