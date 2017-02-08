/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.config;

import static com.gooddata.cfal.restapi.config.ApplicationConfig.createPropertySourcesPlaceholderConfigurer;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationConfigTest.TestApplicationConfig.class)
public class ApplicationConfigTest {

    @Value("${gdc.cfal.mongo.collection.prefix}")
    private String prefix;

    @Test
    public void shouldOverrideProperty() {
        assertThat(prefix, is("test"));
    }

    @Configuration
    static class TestApplicationConfig {
        @Bean
        public static PropertySourcesPlaceholderConfigurer testPropertySourcesPlaceholderConfigurer() {
            return createPropertySourcesPlaceholderConfigurer(new ClassPathResource("/com/gooddata/cfal/restapi/config/applicationTestConfig.properties"));
        }
    }
}
