/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.msf;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.AbstractProjectAT;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessType;
import com.gooddata.dataload.processes.Schedule;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Predicate;

public class ETLScheduleChangeAT extends AbstractProjectAT {

    private static final String MESSAGE_TYPE = "ETL_SCHEDULE_CHANGE";

    private DataloadProcess process;
    private Schedule schedule;

    @BeforeClass(groups = MESSAGE_TYPE)
    public void createProcessAndSchedule() throws URISyntaxException {
        final File file = new File(getClass().getClassLoader().getResource("test.rb").toURI());
        process = gd.getProcessService().createProcess(project, new DataloadProcess(getClass().getSimpleName(), ProcessType.RUBY), file);
        schedule = gd.getProcessService().createSchedule(project, new Schedule(process, "test.rb", "0 0 * * *"));
    }

    @BeforeClass(groups = MESSAGE_TYPE)
    public void setUp() {
        schedule.setCron("1 0 * * *");
        gd.getProcessService().updateSchedule(schedule);
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

    @Test(groups = MESSAGE_TYPE)
    public void testChangeETLScheduleUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testChangeETLScheduleAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(), MESSAGE_TYPE);
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate() {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE));
    }

}
