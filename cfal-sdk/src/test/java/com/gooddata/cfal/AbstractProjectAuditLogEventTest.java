/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.gooddata.context.ThreadContextUtils;
import com.gooddata.context.TransferableGdcCallContext;
import org.junit.After;
import org.junit.Before;

abstract class AbstractProjectAuditLogEventTest {

    protected static final String USER_LOGIN = "bear@gooddata.com";
    protected static final String USER_IP = "127.0.0.1";
    protected static final String DOMAIN_ID = "default";
    protected static final String PROJECT_ID = "FoodMartDemo";

    @Before
    public void setUp() {
        final TransferableGdcCallContext context = new TransferableGdcCallContext();
        context.setUserLogin(USER_LOGIN);
        context.setClientIp(USER_IP);
        context.setDomainId(DOMAIN_ID);
        context.setProjectId(PROJECT_ID);
        ThreadContextUtils.setUpGdcCallContext(context);
    }

    @After
    public void tearDown() {
        ThreadContextUtils.clearGdcCallContext();
    }
}
