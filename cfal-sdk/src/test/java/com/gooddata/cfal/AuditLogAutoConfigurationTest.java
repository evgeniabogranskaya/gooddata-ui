/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static com.gooddata.cfal.CfalProperties.CfalServiceType.CONCURRENT;
import static com.gooddata.cfal.CfalProperties.CfalServiceType.NOOP;
import static com.gooddata.cfal.CfalProperties.CfalServiceType.SIMPLE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class AuditLogAutoConfigurationTest {
    private static final String COMPONENT = "meh";
    private static final AuditLogEventWriter MOCK = mock(AuditLogEventWriter.class);
    private AuditLogAutoConfiguration instance;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        this.instance = new AuditLogAutoConfiguration();
    }

    @Test
    public void testEnabled() throws Exception {
        assertThat(this.instance.cfalAuditLogService(props(false, NOOP), MOCK).isLoggingEnabled(), is(false));
        assertThat(this.instance.cfalAuditLogService(props(false, SIMPLE), MOCK).isLoggingEnabled(), is(false));
        assertThat(this.instance.cfalAuditLogService(props(false, CONCURRENT), MOCK).isLoggingEnabled(), is(false));

        assertThat(this.instance.cfalAuditLogService(props(true, NOOP), MOCK).isLoggingEnabled(), is(true));
        assertThat(this.instance.cfalAuditLogService(props(true, SIMPLE), MOCK).isLoggingEnabled(), is(true));
        assertThat(this.instance.cfalAuditLogService(props(true, CONCURRENT), MOCK).isLoggingEnabled(), is(true));
    }

    @Test
    public void testLoggerType() throws Exception {
        assertThat(this.instance.cfalAuditLogService(props(false, NOOP), MOCK), instanceOf(SimpleAuditLogService.class));
        assertThat(this.instance.cfalAuditLogService(props(false, SIMPLE), MOCK), instanceOf(SimpleAuditLogService.class));
        assertThat(this.instance.cfalAuditLogService(props(false, CONCURRENT), MOCK), instanceOf(ConcurrentAuditLogService.class));
    }

    @Test
    public void testComponent() throws Exception {
        assertThat(this.instance.cfalAuditLogService(props(false, NOOP), MOCK).getComponent(), is(COMPONENT));
        assertThat(this.instance.cfalAuditLogService(props(false, SIMPLE), MOCK).getComponent(), is(COMPONENT));
        assertThat(this.instance.cfalAuditLogService(props(false, CONCURRENT), MOCK).getComponent(), is(COMPONENT));
    }

    @Test
    public void testAuditLogEventWriter() throws Exception {
        assertThat(instance.auditLogEventWriter(props(true, NOOP)), instanceOf(Slf4jAuditLogEventWriter.class));
        assertThat(instance.auditLogEventWriter(props(true, SIMPLE)), instanceOf(AuditLogEventFileWriter.class));
        assertThat(instance.auditLogEventWriter(props(true, CONCURRENT)), instanceOf(AuditLogEventFileWriter.class));
    }

    private CfalProperties props(boolean enabled, CfalProperties.CfalServiceType service) {
        final CfalProperties cfalProperties = new CfalProperties(COMPONENT, enabled, service);
        cfalProperties.setCfalDir(temporaryFolder.getRoot().getAbsolutePath());

        return cfalProperties;
    }
}
