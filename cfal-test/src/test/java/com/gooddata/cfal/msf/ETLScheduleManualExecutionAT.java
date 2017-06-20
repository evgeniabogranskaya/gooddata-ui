/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.msf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.test.AbstractProjectAT;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessType;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.dataload.processes.ScheduleExecutionException;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Predicate;

public class ETLScheduleManualExecutionAT extends AbstractProjectAT {

    private static final String MESSAGE_TYPE = "ETL_SCHEDULE_MANUAL_EXECUTION";
    private static final String SCRIPT_NAME = "test.rb";

    private DataloadProcess process;
    private Schedule schedule;

    @Test(groups = MESSAGE_TYPE)
    public void createProcess() throws URISyntaxException {
        final File file = new File(getClass().getClassLoader().getResource(SCRIPT_NAME).toURI());
        process = gd.getProcessService().createProcess(project, new DataloadProcess(getClass().getSimpleName(), ProcessType.RUBY), file);
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "createProcess")
    public void createSchedule() throws Exception {
        schedule = gd.getProcessService().createSchedule(project, new Schedule(process, SCRIPT_NAME, "0 0 * * *"));
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "createSchedule")
    public void executeSchedule() throws InterruptedException {
        gd.getProcessService().executeSchedule(schedule);
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "executeSchedule", expectedExceptions = ScheduleExecutionException.class)
    public void badExecuteSchedule() {
        final Schedule mock = mock(Schedule.class);
        final String nonExistentScheduleUri = Schedule.TEMPLATE.expand(project.getId(), "fail").toString();
        when(mock.getExecutionsUri()).thenReturn(nonExistentScheduleUri + "/executions");
        gd.getProcessService().executeSchedule(mock);
    }

    @Test(dependsOnMethods = "executeSchedule", groups = MESSAGE_TYPE)
    public void testScheduleManualExecutionMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(true), MESSAGE_TYPE);
    }

    @Test(dependsOnMethods = "executeSchedule", groups = MESSAGE_TYPE)
    public void testScheduleManualExecutionMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(true), MESSAGE_TYPE);
    }

    @Test(dependsOnMethods = "badExecuteSchedule", groups = MESSAGE_TYPE)
    public void testScheduleManualExecutionMessageErrorUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(false), MESSAGE_TYPE);
    }

    @Test(dependsOnMethods = "badExecuteSchedule", groups = MESSAGE_TYPE)
    public void testScheduleManualExecutionMessageErrorAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(false), MESSAGE_TYPE);
    }

    @AfterGroups(groups = MESSAGE_TYPE)
    public void tearDown() {
        if (schedule != null) {
            gd.getProcessService().removeSchedule(schedule);
        }
        if (process != null) {
            gd.getProcessService().removeProcess(process);
        }
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final boolean isSuccess) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE) && e.isSuccess() == isSuccess);
    }
}
