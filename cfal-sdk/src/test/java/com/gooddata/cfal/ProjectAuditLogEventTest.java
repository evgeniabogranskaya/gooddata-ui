/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.junit.Test;

import static com.gooddata.cfal.ETLDataloadProcessExecutionAuditLogEvent.ETL_SCHEDULE_CHANGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertThat;

public class ProjectAuditLogEventTest extends AbstractProjectAuditLogEventTest {

    @Test
    public void shouldSetProperties() throws Exception {
        final ProjectAuditLogEvent event = new ProjectAuditLogEvent("PROJECT", USER_LOGIN, USER_IP, DOMAIN_ID, "/gdc/projects/" + PROJECT_ID, true);

        assertThat(event.getType(), is("PROJECT"));
        assertThat(event.isSuccess(), is(true));
        assertThat(event.getUserLogin(), is(USER_LOGIN));
        assertThat(event.getUserIp(), is(USER_IP));
        assertThat(event.getDomainId(), is(DOMAIN_ID));
        assertThat(event.getProject(), is("/gdc/projects/" + PROJECT_ID));
    }

    @Test
    public void shouldSetPropertiesFromContext() throws Exception {
        final ProjectAuditLogEvent event = new ProjectAuditLogEvent("PROJECT", true);

        assertThat(event.getType(), is("PROJECT"));
        assertThat(event.isSuccess(), is(true));
        assertThat(event.getUserLogin(), is(USER_LOGIN));
        assertThat(event.getUserIp(), is(USER_IP));
        assertThat(event.getDomainId(), is(DOMAIN_ID));
        assertThat(event.getProject(), is("/gdc/projects/" + PROJECT_ID));
    }
}