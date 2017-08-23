/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

import com.codahale.metrics.MetricRegistry;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import com.gooddata.cfal.restapi.service.AuditEventService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

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
    }
}
