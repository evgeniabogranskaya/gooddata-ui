/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.msf;

import static com.gooddata.dataload.processes.ProcessType.DATALOAD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import com.gooddata.FutureResult;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.AbstractProjectAT;
import com.gooddata.dataload.OutputStage;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.dataload.processes.ScheduleExecution;
import com.gooddata.model.ModelDiff;
import com.gooddata.warehouse.Warehouse;
import com.gooddata.warehouse.WarehouseSchema;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Predicate;

public class ManualADDAT extends AbstractProjectAT {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String MESSAGE_TYPE = "ETL_ADD_MANUAL_EXECUTION";

    private Warehouse warehouse;
    private JdbcTemplate jdbcTemplate;

    @BeforeClass
    public void updateProjectModel() throws Exception {
        final ModelDiff projectModelDiff = gd.getModelService().getProjectModelDiff(project,
                new InputStreamReader(getClass().getResourceAsStream("/model.json"))).get();
        if (!projectModelDiff.getUpdateMaql().isEmpty()) {
            gd.getModelService().updateProjectModel(project, projectModelDiff).get();
        }
        logger.info("updated model of project_id={}", project.getId());
    }

    @BeforeClass(dependsOnMethods = "updateProjectModel")
    public void createWarehouse() {
        warehouse = adsService.createWarehouse();
    }

    @BeforeClass(dependsOnMethods = "createWarehouse")
    public void setOutputStage() {
        final OutputStage outputStage = gd.getOutputStageService().getOutputStage(project);
        final WarehouseSchema schema = gd.getWarehouseService().getDefaultWarehouseSchema(warehouse);

        outputStage.setSchemaUri(schema.getUri());

        gd.getOutputStageService().updateOutputStage(outputStage);
        logger.info("output stage of project_id={} is now set to warehouse_id={}", project.getId(), warehouse.getId());

    }

    @BeforeClass(dependsOnMethods = "createWarehouse")
    public void createTemplate() {
        jdbcTemplate = adsService.createJdbcTemplate(warehouse);
    }

    @BeforeClass(dependsOnMethods = "createTemplate")
    public void executeSql() throws Exception {
        final File city = new File(getClass().getClassLoader().getResource("city.sql").toURI());
        final File person = new File(getClass().getClassLoader().getResource("person.sql").toURI());

        final String citySql = FileUtils.readFileToString(city);
        final String personSql = FileUtils.readFileToString(person);

        jdbcTemplate.execute(citySql);
        jdbcTemplate.execute(personSql);

        logger.info("executed sql scripts on warehouse_id={}", warehouse.getId());
    }

    @BeforeClass(dependsOnMethods = {"setOutputStage", "executeSql"})
    public void executeDataloadProcess() {
        final DataloadProcess dataloadProcess = gd.getProcessService()
                .listProcesses(project)
                .stream()
                .filter(e -> e.getType().equals(DATALOAD.name()))
                .findFirst()
                .get();

        createAndExecuteSchedule(dataloadProcess);
    }

    @Test
    public void tesADDManualExecutionMessageUserAPI() throws Exception {
        doTestUserApi(pageCheckPredicate(true), MESSAGE_TYPE);
    }

    @Test
    public void tesADDManualExecutionMessageAdminAPI() throws Exception {
        doTestAdminApi(pageCheckPredicate(true), MESSAGE_TYPE);
    }

    @AfterClass
    public void clearOutputStage() {
        final OutputStage outputStage = gd.getOutputStageService().getOutputStage(project);

        outputStage.setSchemaUri(null);

        gd.getOutputStageService().updateOutputStage(outputStage);
    }

    @AfterClass(dependsOnMethods = "clearOutputStage")
    public void removeWarehouse() {
        if (warehouse != null) {
            gd.getWarehouseService().removeWarehouse(warehouse);
        }
    }

    private void createAndExecuteSchedule(final DataloadProcess dataloadProcess) {
        final Schedule schedule = new Schedule(dataloadProcess, null, "0 0 * * *");
        schedule.addParam("GDC_DE_SYNCHRONIZE_ALL", "true");

        Schedule createdSchedule = gd.getProcessService().createSchedule(project, schedule);

        FutureResult<ScheduleExecution> futureResult = gd.getProcessService().executeSchedule(createdSchedule);
        final ScheduleExecution scheduleExecution = futureResult.get();

        assertThat(scheduleExecution.getStatus(), is(not("ERROR")));

        logger.info("executed schedule_id={} of dataload process={}", createdSchedule.getId(), dataloadProcess.getId());
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final boolean isSuccess) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE) && e.isSuccess() == isSuccess);
    }

}
