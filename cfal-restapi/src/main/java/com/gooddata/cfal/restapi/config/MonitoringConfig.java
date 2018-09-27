/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.config;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.Arrays.asList;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.gooddata.commons.monitoring.jmx.JmxAttributeGaugeSet;
import com.gooddata.commons.monitoring.jmx.TomcatMetricSet;
import com.gooddata.commons.monitoring.metrics.MetricRegistrator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.InvalidApplicationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class MonitoringConfig {

    private static final String TOMCAT_METRIC = "tomcat";
    private static final String MONGO_DRIVER_JMX_DOMAIN = "org.mongodb.driver";
    private static final String CONNECTION_POOL_JMX_TYPE = "ConnectionPool";
    private static final List<String> JMX_ATTRIBUTES = asList("Size", "WaitQueueSize", "MaxSize", "MinSize", "CheckedOutCount");

    @Bean
    public MetricRegistrator tomcatMetricRegistrator(final MetricRegistry metricRegistry) {
        final Map<String, Metric> namedMetrics = new HashMap<>();
        namedMetrics.put(TOMCAT_METRIC, new TomcatMetricSet());
        return new MetricRegistrator(metricRegistry, namedMetrics);
    }

    @Bean
    public MetricRegistrator mongoConnectionRegistrator(final MetricRegistry metricRegistry) throws MalformedObjectNameException {
        final Set<ObjectInstance> objectInstances = ManagementFactory.getPlatformMBeanServer().queryMBeans(null, new QueryExp() {
            @Override
            public boolean apply(final ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException {
                if (!name.getDomain().equals(MONGO_DRIVER_JMX_DOMAIN)) {
                    return false;
                }
                final String type = name.getKeyProperty("type");
                return CONNECTION_POOL_JMX_TYPE.equals(type);
            }

            @Override
            public void setMBeanServer(final MBeanServer s) {
            }
        });

        final Map<String, Metric> metrics = new HashMap<>();

        for (ObjectInstance objectInstance : objectInstances) {
            metrics.put(name("cfal", "mongo", "connection-pool", objectInstance.getObjectName().getKeyProperty("host")),
                    new JmxAttributeGaugeSet(objectInstance.getObjectName().toString(), JMX_ATTRIBUTES));
        }

        return new MetricRegistrator(metricRegistry, metrics);
    }

}
