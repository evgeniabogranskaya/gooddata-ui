/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.junit.Test;

import static com.gooddata.cfal.ETLDataloadProcessExecutionAuditLogEvent.ETL_SCHEDULE_CHANGE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ETLDataloadProcessExecutionAuditLogEventTest extends AbstractProjectAuditLogEventTest {

    private static final String PROCESS = "/gdc/projects/" + PROJECT_ID + "/dataload/processes/TestProcessId";
    private static final String EXECUTION = PROCESS + "/executions/ExecutionId";
    private static final String INSTANCE = "/gdc/datawarehouse/instances/InstanceId";

    @Test
    public void shouldSetValuesFromGdcCallContext() {
        final ETLDataloadProcessExecutionAuditLogEvent event = new ETLDataloadProcessExecutionAuditLogEvent(ETL_SCHEDULE_CHANGE, true, PROCESS, EXECUTION, INSTANCE);

        assertThat(event.getType(), is(ETL_SCHEDULE_CHANGE));
        assertThat(event.isSuccess(), is(true));
        assertThat(event.getUserLogin(), is(USER_LOGIN));
        assertThat(event.getUserIp(), is(USER_IP));
        assertThat(event.getDomainId(), is(DOMAIN_ID));
        assertThat(event.getProject(), is("/gdc/projects/" + PROJECT_ID));
        assertThat(event.getProcess(), is(PROCESS));
        assertThat(event.getExecution(), is(EXECUTION));
        assertThat(event.getInstance(), is(INSTANCE));
    }

}