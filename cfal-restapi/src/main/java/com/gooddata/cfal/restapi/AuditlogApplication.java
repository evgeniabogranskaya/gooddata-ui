/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi;

import com.gooddata.c4.boot.EnableC4Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableC4Client
@PropertySource(value = "file:///etc/gdc/cfal-restapi.properties", ignoreResourceNotFound = true)
public class AuditlogApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AuditlogApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(AuditlogApplication.class, args);
    }
}
