/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.msf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gooddata.cfal.AbstractAT;
import com.gooddata.auditevent.AuditEvent;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.dataload.processes.ScheduleExecutionException;
import com.gooddata.project.Project;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.function.Predicate;

public class ETLScheduleManualExecutionAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "ETL_SCHEDULE_MANUAL_EXECUTE";

    @Test(groups = MESSAGE_TYPE)
    public void executeSchedule() throws URISyntaxException {
        final Project project = projectHelper.getOrCreateProject();
        final DataloadProcess process = processHelper.createRubyProcess(project);
        final Schedule schedule = processHelper.createSchedule(project, process);
        processHelper.executeSchedule(schedule);
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "executeSchedule", expectedExceptions = ScheduleExecutionException.class)
    public void badExecuteSchedule() {
        final Schedule mock = mock(Schedule.class);
        final String nonExistentScheduleUri = Schedule.TEMPLATE.expand(projectHelper.getOrCreateProject().getId(), "fail").toString();
        when(mock.getExecutionsUri()).thenReturn(nonExistentScheduleUri + "/executions");
        gd.getProcessService().executeSchedule(mock);
    }

    @Test(dependsOnMethods = "executeSchedule", groups = MESSAGE_TYPE)
    public void testScheduleManualExecutionMessageUserApi() {
        doTestUserApi(eventCheck(true), MESSAGE_TYPE);
    }

    @Test(dependsOnMethods = "executeSchedule", groups = MESSAGE_TYPE)
    public void testScheduleManualExecutionMessageAdminApi() {
        doTestAdminApi(eventCheck(true), MESSAGE_TYPE);
    }

    @Test(dependsOnMethods = "badExecuteSchedule", groups = MESSAGE_TYPE)
    public void testScheduleManualExecutionMessageErrorUserApi() {
        doTestUserApi(eventCheck(false), MESSAGE_TYPE);
    }

    @Test(dependsOnMethods = "badExecuteSchedule", groups = MESSAGE_TYPE)
    public void testScheduleManualExecutionMessageErrorAdminApi() {
        doTestAdminApi(eventCheck(false), MESSAGE_TYPE);
    }

    @AfterGroups(groups = MESSAGE_TYPE)
    public void tearDown() {
        processHelper.clearAllSchedules();
    }

    private Predicate<AuditEvent> eventCheck(final boolean isSuccess) {
        return (e -> e.getUserLogin().equals(getAccount().getLogin()) && e.getType().equals(MESSAGE_TYPE) && e.isSuccess() == isSuccess);
    }
}
