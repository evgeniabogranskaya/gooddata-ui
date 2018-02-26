/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.sst;

import com.gooddata.auditevent.AuditEvent;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.md.ProjectDashboard;
import com.gooddata.project.Project;
import com.gooddata.warehouse.Warehouse;
import org.testng.annotations.*;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Tests only positive (success=true) SST create events as the negative creation would be hard to simulate and is not
 * necessary.
 */
public class SstEventsAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "SST_CREATE";

    /**
     * The expected number of SST_CREATE events when all components in setUp method did their work.
     */
    private int expectedEventsCount = 0;

    @BeforeClass(groups = MESSAGE_TYPE)
    public void tryLogins() throws Exception {
        // WebApp components (+2 events)
        loginHelper.usernamePasswordLogin();
        loginHelper.ssoLogin(getAccount());
        expectedEventsCount += 2;
    }

    @BeforeClass(groups = MESSAGE_TYPE)
    public void rubyProcessExecution() throws Exception {
        // create new RUBY process from ZIP archive - check of "enableScriptBundlerExecution" FF on REST API (+1 event)
        final DataloadProcess rubyProcess = processHelper.createRubyProcess(projectHelper.getOrCreateProject());
        // execute RUBY process (+1 event)
        processHelper.executeProcess(rubyProcess);
        expectedEventsCount += 2;
    }

    @BeforeClass(groups = MESSAGE_TYPE)
    public void cloverProcessExecution() throws Exception {
        // create new CLOVER process
        final DataloadProcess cloverProcess = processHelper.createCloverProcess(projectHelper.getOrCreateProject());
        // execute CLOVER process (+1 event)
        processHelper.executeProcess(cloverProcess);
        expectedEventsCount++;
    }

    @BeforeClass(groups = MESSAGE_TYPE)
    public void csvUploaderExecution() throws Exception {
        // execute CSV Upload (+1 event)
        final Project project = projectHelper.getOrCreateProject();
        final String datasetId = csvUploadHelper.uploadCsv(project);
        // delete created CSV dataset (+1 event)
        csvUploadHelper.deleteCsvDataset(project, datasetId);
        expectedEventsCount += 2;
    }

    @BeforeClass(groups = MESSAGE_TYPE)
    public void addExecution() throws Exception {
        final Project addProject = projectHelper.createProject();
        // call project model worker DIFF (+1 event)
        projectHelper.setupDefaultModel(addProject);

        final Warehouse addWarehouse = adsHelper.createWarehouse();
        adsHelper.setupDefaultModel(addWarehouse);

        // validation of existing OS schema (+1 events)
        projectHelper.setupOutputStage(addProject, addWarehouse);

        // execution of ADD process (+1 event)
        final Schedule addSchedule = processHelper.createADDSchedule(addProject);
        processHelper.executeSchedule(addSchedule);
        expectedEventsCount += 3;
    }

    // registration test can be only done on PI where the captchaString property is enabled
    @BeforeClass(groups = {MESSAGE_TYPE, SSH_GROUP})
    public void registerUser() {
        // registration of new user generates SST for current user session (+1 event)
        // calls Registration.pm
        accountHelper.registerAndDeleteUser();
        expectedEventsCount++;
    }

    @BeforeClass(groups = MESSAGE_TYPE)
    public void runScheduledEmail() throws Exception {
        final Project project = projectHelper.getOrCreateProject();
        // running of scheduled email should create own SST (+1 event)
        scheduledMailHelper.runScheduledMail(project, metadataHelper.getOrCreateReport(project));
        expectedEventsCount++;
    }

    @BeforeClass(groups = MESSAGE_TYPE)
    public void exportDashboard() throws Exception {
        final Project project = projectHelper.getOrCreateProject();
        final ProjectDashboard dashboard = metadataHelper.getOrCreateDashboard(project);

        // runs export dashboard (+1 event)
        gd.getExportService().runExportDashboard(dashboard);
        expectedEventsCount++;
    }

    @AfterClass(groups = MESSAGE_TYPE)
    public void tearDown() {
        processHelper.clearAllSchedules();
        scheduledMailHelper.clearScheduledMails();
    }

    @Test(groups = MESSAGE_TYPE)
    public void testSstCreateUserApi() {
        doTestUserApi(eventCheck(), MESSAGE_TYPE, expectedEventsCount);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testSstCreateAdminApi() {
        doTestAdminApi(eventCheck(), MESSAGE_TYPE, expectedEventsCount);
    }

    private Predicate<AuditEvent> eventCheck() {
        return (e -> Objects.equals(getAccount().getLogin(), e.getUserLogin()) &&
                MESSAGE_TYPE.equals(e.getType()) &&
                e.isSuccess()
        );
    }
}
