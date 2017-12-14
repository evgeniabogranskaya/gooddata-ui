/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.msf;

import com.gooddata.cfal.AbstractAT;
import com.gooddata.auditevent.AuditEvent;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.dataload.processes.ScheduleExecution;
import com.gooddata.project.Project;
import com.gooddata.warehouse.Warehouse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.function.Predicate;

public class ManualADDAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "ETL_ADD_MANUAL_EXECUTE";
    private static final String ERROR_STATUS = "ERROR";

    private Warehouse warehouse;
    private Project project;

    @BeforeClass(groups = MESSAGE_TYPE)
    public void createProjectWithModel() {
        project = projectHelper.createProject();
        projectHelper.setupDefaultModel(project);
    }

    @BeforeClass(groups = MESSAGE_TYPE)
    public void createWarehouseWithModel() throws Exception {
        warehouse = adsHelper.getOrCreateWarehouse();
        adsHelper.setupDefaultModel(warehouse);
    }

    @BeforeClass(groups = MESSAGE_TYPE, dependsOnMethods = { "createProjectWithModel", "createWarehouseWithModel" })
    public void setupOutputStage() {
        projectHelper.setupOutputStage(project, warehouse);
    }

    @BeforeClass(groups = MESSAGE_TYPE, dependsOnMethods = "setupOutputStage")
    public void executeDataloadProcess() {
        final Schedule schedule = processHelper.createADDSchedule(project);
        final ScheduleExecution execution = processHelper.executeSchedule(schedule);

        if (ERROR_STATUS.equals(execution.getStatus())) {
            throw new IllegalStateException("Execution of dataload process schedule '" + schedule.getId()
                    + "' in project '" + project.getId() + "' should not fail.");
        }

        logger.info("executed schedule_id={} of DATALOAD process for project={}", schedule.getId(), project.getId());
    }

    @Test(groups = MESSAGE_TYPE)
    public void tesADDManualExecutionMessageUserAPI() {
        doTestUserApi(eventCheck(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void tesADDManualExecutionMessageAdminAPI() {
        doTestAdminApi(eventCheck(), MESSAGE_TYPE);
    }

    @AfterClass(groups = MESSAGE_TYPE)
    public void cleanUp() {
        processHelper.clearAllSchedules();
        projectHelper.clearOutputStage(project);
    }

    private Predicate<AuditEvent> eventCheck() {
        return (e -> e.getUserLogin().equals(getAccount().getLogin()) && e.getType().equals(MESSAGE_TYPE) && e.isSuccess());
    }

}
