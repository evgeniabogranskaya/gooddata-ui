/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.msf;

import com.gooddata.cfal.AbstractAT;
import com.gooddata.auditevent.AuditEvent;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.project.Project;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.function.Predicate;

public class ETLScheduleChangeAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "ETL_SCHEDULE_CHANGE";

    @BeforeClass(groups = MESSAGE_TYPE)
    public void setUp() throws URISyntaxException {
        final Project project = projectHelper.getOrCreateProject();
        final DataloadProcess process = processHelper.createProcess(project);
        final Schedule schedule = processHelper.createSchedule(project, process);
        schedule.setCron("1 0 * * *");
        gd.getProcessService().updateSchedule(schedule);
    }

    @AfterGroups(groups = MESSAGE_TYPE)
    public void tearDown() {
        processHelper.clearAllSchedules();
    }

    @Test(groups = MESSAGE_TYPE)
    public void testChangeETLScheduleUserApi() {
        doTestUserApi(eventCheck(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testChangeETLScheduleAdminApi() {
        doTestAdminApi(eventCheck(), MESSAGE_TYPE);
    }

    private Predicate<AuditEvent> eventCheck() {
        return (e -> e.getUserLogin().equals(getAccount().getLogin()) && e.getType().equals(MESSAGE_TYPE));
    }

}
