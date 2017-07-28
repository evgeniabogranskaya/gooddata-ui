/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.config;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.gooddata.commons.monitoring.jmx.TomcatMetricSet;
import com.gooddata.commons.monitoring.metrics.MetricRegistrator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MonitoringConfig {

    private static final String TOMCAT_METRIC = "tomcat";

    @Bean
    public MetricRegistrator tomcatMetricRegistrator(final MetricRegistry metricRegistry) {
        final Map<String, Metric> namedMetrics = new HashMap<>();
        namedMetrics.put(TOMCAT_METRIC, new TomcatMetricSet());
        return new MetricRegistrator(metricRegistry, namedMetrics);
    }

}
