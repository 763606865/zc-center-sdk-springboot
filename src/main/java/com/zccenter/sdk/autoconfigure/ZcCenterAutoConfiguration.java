package com.zccenter.sdk.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zccenter.sdk.ZcCenterClient;
import com.zccenter.sdk.ZcCenterProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot 自动配置：注册 {@link ZcCenterClient}。
 */
@AutoConfiguration
@ConditionalOnClass(ZcCenterClient.class)
@EnableConfigurationProperties(ZcCenterProperties.class)
@ConditionalOnProperty(prefix = "zc-center", name = "base-url")
public class ZcCenterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ZcCenterClient zcCenterClient(
            ZcCenterProperties properties,
            ObjectProvider<ObjectMapper> objectMapperProvider
    ) {
        return new ZcCenterClient(properties, null, objectMapperProvider.getIfAvailable());
    }
}
