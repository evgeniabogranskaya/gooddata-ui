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

public class AuditLogAutoConfigurationTest {
    private static final String COMPONENT = "meh";
    private AuditLogAutoConfiguration instance;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        this.instance = new AuditLogAutoConfiguration();
    }

    @Test
    public void testEnabled() throws Exception {
        assertThat(this.instance.cfalAuditLogService(props(false, NOOP)).isLoggingEnabled(), is(false));
        assertThat(this.instance.cfalAuditLogService(props(false, SIMPLE)).isLoggingEnabled(), is(false));
        assertThat(this.instance.cfalAuditLogService(props(false, CONCURRENT)).isLoggingEnabled(), is(false));

        assertThat(this.instance.cfalAuditLogService(props(true, NOOP)).isLoggingEnabled(), is(true));
        assertThat(this.instance.cfalAuditLogService(props(true, SIMPLE)).isLoggingEnabled(), is(true));
        assertThat(this.instance.cfalAuditLogService(props(true, CONCURRENT)).isLoggingEnabled(), is(true));
    }

    @Test
    public void testLoggerType() throws Exception {
        assertThat(this.instance.cfalAuditLogService(props(false, NOOP)), instanceOf(NoopAuditEventService.class));
        assertThat(this.instance.cfalAuditLogService(props(false, SIMPLE)), instanceOf(SimpleAuditLogService.class));
        assertThat(this.instance.cfalAuditLogService(props(false, CONCURRENT)), instanceOf(ConcurrentAuditLogService.class));
    }

    @Test
    public void testComponent() throws Exception {
        assertThat(this.instance.cfalAuditLogService(props(false, NOOP)).getComponent(), is(COMPONENT));
        assertThat(this.instance.cfalAuditLogService(props(false, SIMPLE)).getComponent(), is(COMPONENT));
        assertThat(this.instance.cfalAuditLogService(props(false, CONCURRENT)).getComponent(), is(COMPONENT));
    }

    private CfalProperties props(boolean enabled, CfalProperties.CfalServiceType service) {
        final CfalProperties cfalProperties = new CfalProperties(COMPONENT, enabled, service);
        cfalProperties.setCfalDir(temporaryFolder.getRoot().getAbsolutePath());

        return cfalProperties;
    }
}
