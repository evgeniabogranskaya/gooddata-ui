/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.config;

import com.codahale.metrics.MetricRegistry;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;

import javax.management.MBeanServer;

public class MonitoringTestConfig {

    @Bean
    public MetricRegistry metricRegistry() {
        return Mockito.mock(MetricRegistry.class);
    }

    @Bean
    public MBeanServer mBeanServer() {
        return Mockito.mock(MBeanServer.class);
    }
}
