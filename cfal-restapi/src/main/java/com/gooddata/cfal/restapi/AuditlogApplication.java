/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi;

import com.gooddata.boot.autoconfigure.GdcBootApplication;
import com.gooddata.c4.boot.EnableC4Client;
import com.gooddata.commons.monitoring.metrics.boot.EnableMonitoring;
import com.gooddata.spring.systemd.EnableSDNotify;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableMonitoring
@EnableC4Client
@EnableSDNotify
@EnableScheduling
@EnableDiscoveryClient
@GdcBootApplication
public class AuditlogApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AuditlogApplication.class, args);
    }
}
