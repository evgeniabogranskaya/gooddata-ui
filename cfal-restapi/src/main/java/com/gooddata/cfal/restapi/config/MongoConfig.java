/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.config;

import com.gooddata.cfal.restapi.util.StringToUTCDateTimeConverter;
import com.mongodb.MongoClientOptions;
import com.mongodb.management.JMXConnectionPoolListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import java.util.Collections;

@Configuration
public class MongoConfig {


    /**
     * Register custom converters for mongo template.
     * Registers {@link StringToUTCDateTimeConverter} to be able to convert String to DateTime,
     * because fluentd is not able to save date to mongo as date, but only as String
     */
    @Bean
    public CustomConversions customConversions() {
        return new CustomConversions(Collections.singletonList(new StringToUTCDateTimeConverter()));
    }

    @Bean
    public MongoClientOptions mongoOptions() {
        return MongoClientOptions.builder().addConnectionPoolListener(new JMXConnectionPoolListener()).build();
    }
}
