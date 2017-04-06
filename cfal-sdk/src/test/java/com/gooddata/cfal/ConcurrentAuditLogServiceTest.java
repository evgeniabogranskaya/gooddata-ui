/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

import static com.gooddata.cfal.ConcurrentAuditLogService.POISON_PILL;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ConcurrentAuditLogServiceTest {

    private static final AuditLogEvent EVENT = new AuditLogEvent(AuditLogEventType.STANDARD_LOGIN, "bear@gooddata.com", "1.2.3.4", "default");

    private ConcurrentAuditLogService service;
    private AuditLogEventFileWriter writer;

    @Before
    public void setUp() throws Exception {
        writer = mock(AuditLogEventFileWriter.class);
        service = new ConcurrentAuditLogService("test", writer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithEmptyComponent() throws Exception {
        new ConcurrentAuditLogService("");
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

        final ReentrantLock lock = new ReentrantLock();
        lock.lock();

        service = new ConcurrentAuditLogService("foo", event -> { lock.lock(); return 0; }, 1, rejectionHandler);

        service.logEvent(EVENT);
        service.logEvent(EVENT);

        verify(rejectionHandler).handle(EVENT);

        lock.unlock();
        service.destroy();
    }

    @Test
    public void testPoisonPillIsNotWrittenByWriter() throws Exception {
        AuditLogEventWriter auditLogEventWriter = mock(AuditLogEventWriter.class);

        service = new ConcurrentAuditLogService("foo", auditLogEventWriter);

        service.destroy();

        verify(auditLogEventWriter, times(0)).logEvent(POISON_PILL);
    }
}