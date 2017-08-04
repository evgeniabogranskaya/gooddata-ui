/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.msf;

import static com.gooddata.dataload.processes.ProcessType.DATALOAD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import com.gooddata.FutureResult;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.dataload.OutputStage;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.dataload.processes.ScheduleExecution;
import com.gooddata.model.ModelDiff;
import com.gooddata.project.Project;
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

public class ManualADDAT extends AbstractAT {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String MESSAGE_TYPE = "ETL_ADD_MANUAL_EXECUTION";

    private Warehouse warehouse;
    private JdbcTemplate jdbcTemplate;

    @BeforeClass(groups = MESSAGE_TYPE)
    public void updateProjectModel() throws Exception {
        final Project project = projectService.getOrCreateProject();
        final ModelDiff projectModelDiff = gd.getModelService().getProjectModelDiff(project,
                new InputStreamReader(getClass().getResourceAsStream("/model.json"))).get();
        if (!projectModelDiff.getUpdateMaql().isEmpty()) {
            gd.getModelService().updateProjectModel(project, projectModelDiff).get();
        }
        logger.info("updated model of project_id={}", project.getId());
    }

    @BeforeClass(groups = MESSAGE_TYPE, dependsOnMethods = "updateProjectModel")
    public void createWarehouse() {
        warehouse = adsService.getOrCreateWarehouse();
    }

    @BeforeClass(groups = MESSAGE_TYPE, dependsOnMethods = "createWarehouse")
    public void setOutputStage() {
        final Project project = projectService.getOrCreateProject();
        final OutputStage outputStage = gd.getOutputStageService().getOutputStage(project);
        final WarehouseSchema schema = gd.getWarehouseService().getDefaultWarehouseSchema(warehouse);

        outputStage.setSchemaUri(schema.getUri());

        gd.getOutputStageService().updateOutputStage(outputStage);
        logger.info("output stage of project_id={} is now set to warehouse_id={}", project.getId(), warehouse.getId());

    }

    @BeforeClass(groups = MESSAGE_TYPE, dependsOnMethods = "createWarehouse")
    public void createTemplate() {
        jdbcTemplate = adsService.createJdbcTemplate(warehouse);
    }

    @BeforeClass(groups = MESSAGE_TYPE, dependsOnMethods = "createTemplate")
    public void executeSql() throws Exception {
        final File city = new File(getClass().getClassLoader().getResource("city.sql").toURI());
        final File person = new File(getClass().getClassLoader().getResource("person.sql").toURI());

        final String citySql = FileUtils.readFileToString(city);
        final String personSql = FileUtils.readFileToString(person);

        jdbcTemplate.execute(citySql);
        jdbcTemplate.execute(personSql);

        logger.info("executed sql scripts on warehouse_id={}", warehouse.getId());
    }

    @BeforeClass(groups = MESSAGE_TYPE, dependsOnMethods = {"setOutputStage", "executeSql"})
    public void executeDataloadProcess() {
        final DataloadProcess dataloadProcess = gd.getProcessService()
                .listProcesses(projectService.getOrCreateProject())
                .stream()
                .filter(e -> e.getType().equals(DATALOAD.name()))
                .findFirst()
                .get();

        createAndExecuteSchedule(dataloadProcess);
    }

    @Test(groups = MESSAGE_TYPE)
    public void tesADDManualExecutionMessageUserAPI() throws Exception {
        doTestUserApi(pageCheckPredicate(true), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void tesADDManualExecutionMessageAdminAPI() throws Exception {
        doTestAdminApi(pageCheckPredicate(true), MESSAGE_TYPE);
    }

    @AfterClass(groups = MESSAGE_TYPE)
    public void clearOutputStage() {
        final OutputStage outputStage = gd.getOutputStageService().getOutputStage(projectService.getOrCreateProject());

        outputStage.setSchemaUri(null);

        gd.getOutputStageService().updateOutputStage(outputStage);
    }

    private void createAndExecuteSchedule(final DataloadProcess dataloadProcess) {
        final Schedule schedule = new Schedule(dataloadProcess, null, "0 0 * * *");
        schedule.addParam("GDC_DE_SYNCHRONIZE_ALL", "true");

        Schedule createdSchedule = gd.getProcessService().createSchedule(projectService.getOrCreateProject(), schedule);

        FutureResult<ScheduleExecution> futureResult = gd.getProcessService().executeSchedule(createdSchedule);
        final ScheduleExecution scheduleExecution = futureResult.get();

        assertThat(scheduleExecution.getStatus(), is(not("ERROR")));

        logger.info("executed schedule_id={} of dataload process={}", createdSchedule.getId(), dataloadProcess.getId());
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final boolean isSuccess) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(getAccount().getLogin()) && e.getType().equals(MESSAGE_TYPE) && e.isSuccess() == isSuccess);
    }

}
