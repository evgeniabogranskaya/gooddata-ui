/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.JmxAttributeGauge;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import com.gooddata.cfal.restapi.service.AuditEventService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuditLogApplicationIT {

    @Autowired
    private MetricRegistry metricRegistry;

    @Autowired
    private AuditLogEventRepository auditLogEventRepository;

    @Autowired
    private AuditEventService auditEventService;

    @Test
    public void testMetricsRegistered() throws Exception {
        assertThat(metricRegistry.getMetrics(), hasEntry(equalTo("cfal.AuditLogEventRepository.find.by.domain.time"), equalTo(auditLogEventRepository.getFindByDomainTimer())));
        assertThat(metricRegistry.getMetrics(), hasEntry(equalTo("cfal.AuditLogEventRepository.find.by.user.time"), equalTo(auditLogEventRepository.getFindByUserTimer())));
        assertThat(metricRegistry.getMetrics(), hasEntry(equalTo("cfal.AuditEventService.find.by.domain.time"), equalTo(auditEventService.getFindByDomainTimer())));
        assertThat(metricRegistry.getMetrics(), hasEntry(equalTo("cfal.AuditEventService.find.by.user.time"), equalTo(auditEventService.getFindByUserTimer())));
        assertThat(metricRegistry.getMetrics(), hasEntry(equalTo("cfal.mongo.connection-pool.localhost.Size"), instanceOf(JmxAttributeGauge.class)));
        assertThat(metricRegistry.getMetrics(), hasEntry(equalTo("cfal.mongo.connection-pool.localhost.WaitQueueSize"), instanceOf(JmxAttributeGauge.class)));
        assertThat(metricRegistry.getMetrics(), hasEntry(equalTo("cfal.mongo.connection-pool.localhost.MaxSize"), instanceOf(JmxAttributeGauge.class)));
        assertThat(metricRegistry.getMetrics(), hasEntry(equalTo("cfal.mongo.connection-pool.localhost.MinSize"), instanceOf(JmxAttributeGauge.class)));
        assertThat(metricRegistry.getMetrics(), hasEntry(equalTo("cfal.mongo.connection-pool.localhost.CheckedOutCount"), instanceOf(JmxAttributeGauge.class)));
    }
}
