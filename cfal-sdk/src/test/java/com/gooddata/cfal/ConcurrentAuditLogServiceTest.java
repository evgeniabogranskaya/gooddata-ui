/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.gooddata.cfal.CfalMonitoringMetricConstants.LOG_CALL_COUNT;
import static com.gooddata.cfal.CfalMonitoringMetricConstants.QUEUE_REJECTED_COUNT;
import static com.gooddata.cfal.CfalMonitoringMetricConstants.QUEUE_SIZE;
import static com.gooddata.cfal.ConcurrentAuditLogService.POISON_PILL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ConcurrentAuditLogServiceTest {

    private static final AuditLogEvent EVENT = new AuditLogEvent("FOO", "bear@gooddata.com", "1.2.3.4", "default");

    private ConcurrentAuditLogService service;
    private AuditLogEventFileWriter writer;

    @Before
    public void setUp() throws Exception {
        writer = mock(AuditLogEventFileWriter.class);
        service = new ConcurrentAuditLogService("test", writer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithEmptyComponent() throws Exception {
        new ConcurrentAuditLogService("", writer);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullArgument() throws Exception {
        service.logEvent(null);
    }

    @Test
    public void shouldShutdownEmptyService() throws Exception {
        service.destroy();
        verify(writer).close();
    }

    @Test
    public void shouldLogEventAndShutdown() throws Exception {
        service.logEvent(EVENT);

        service.destroy();

        verify(writer).logEvent(EVENT);
        verify(writer).close();
    }

    @Test
    public void shouldCallRejectionHandler() throws Exception {
        final RejectionHandler rejectionHandler = mock(RejectionHandler.class);

        final CountDownLatch lock = new CountDownLatch(1);

        service = new ConcurrentAuditLogService("foo", event -> {
            try {
                lock.await();
            } catch (InterruptedException ignore) {
            }
            return 0;
        }, 1, rejectionHandler);

        service.logEvent(EVENT);
        service.logEvent(EVENT);
        service.logEvent(EVENT); //log three events, because first event may already be consumed by consumer thread

        verify(rejectionHandler, atLeastOnce()).handle(EVENT);

        lock.countDown();

        assertThat(service.getEnqueueErrors(), is(not(0L)));

        service.destroy();
    }

    @Test
    public void testGetQueueSize() throws Exception {
        assertThat(service.getQueueSize(), is(0L));
    }

    @Test
    public void testGetEnueueErrors() throws Exception {
        assertThat(service.getQueueSize(), is(0L));
    }

    @Test
    public void testPoisonPillIsNotWrittenByWriter() throws Exception {
        AuditLogEventWriter auditLogEventWriter = mock(AuditLogEventWriter.class);

        service = new ConcurrentAuditLogService("foo", auditLogEventWriter);

        service.destroy();

        verify(auditLogEventWriter, times(0)).logEvent(POISON_PILL);
    }

    @Test
    public void testGetMetrics() throws Exception {
        final Map<String, Metric> metrics = service.getMetrics();

        assertThat(metrics, hasEntry(equalTo(QUEUE_SIZE), instanceOf(Gauge.class)));
        assertThat(metrics, hasEntry(equalTo(QUEUE_REJECTED_COUNT), instanceOf(Gauge.class)));
        assertThat(metrics, hasEntry(equalTo(LOG_CALL_COUNT), instanceOf(Gauge.class)));
    }
}