/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.gooddata.context.ThreadContextUtils;
import com.gooddata.context.TransferableGdcCallContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuditLogEventTest {

    private static final String USER_LOGIN = "bear@gooddata.com";
    private static final String USER_IP = "127.0.0.1";
    private static final String DOMAIN_ID = "default";

    @Before
    public void setUp() {
        final TransferableGdcCallContext context = new TransferableGdcCallContext();
        context.setUserLogin(USER_LOGIN);
        context.setClientIp(USER_IP);
        context.setDomainId(DOMAIN_ID);
        ThreadContextUtils.setUpGdcCallContext(context);
    }

    @After
    public void tearDown() {
        ThreadContextUtils.clearGdcCallContext();
    }

    @Test
    public void shouldSetValuesFromGdcCallContext() {
        final AuditLogEvent event = new AuditLogEvent("FOO", true);

        assertThat(event.getType(), is("FOO"));
        assertThat(event.isSuccess(), is(true));
        assertThat(event.getUserLogin(), is(USER_LOGIN));
        assertThat(event.getUserIp(), is(USER_IP));
        assertThat(event.getDomainId(), is(DOMAIN_ID));
    }

}