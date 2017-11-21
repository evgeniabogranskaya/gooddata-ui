/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;

/**
 * Configuration not related to web or mongo
 */
@Configuration
public class ApplicationConfig {

    /**
     * Register additional property placeholder configurer which can override properties already set by spring boot default property configurer.
     * Use this approach, because default spring boot configurer cannot be configured to override properties set by application properties by properties
     * set in external file.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer gdcPropertySourcesPlaceholderConfigurer() {
        return createPropertySourcesPlaceholderConfigurer(new FileSystemResource("/etc/gdc/cfal-restapi.properties"));
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }

    static PropertySourcesPlaceholderConfigurer createPropertySourcesPlaceholderConfigurer(final AbstractResource... resources) {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocalOverride(true);
        propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(true);
        propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        propertySourcesPlaceholderConfigurer.setOrder(Ordered.LOWEST_PRECEDENCE);
        propertySourcesPlaceholderConfigurer.setLocations(resources);
        return propertySourcesPlaceholderConfigurer;
    }
}
