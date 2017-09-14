/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.msf;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.fail;

import com.gooddata.FutureResult;
import com.gooddata.GoodDataException;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.auditevent.AuditEvent;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecution;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.dataload.processes.ProcessType;
import com.gooddata.project.Project;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.function.Predicate;

/**
 * Tests ETL process messages
 */
public class ETLProcessAT extends AbstractAT {

    private static final String EXECUTION_MESSAGE_TYPE = "ETL_PROCESS_MANUAL_EXECUTE";
    private static final String CREATE_MESSAGE_TYPE = "ETL_PROCESS_CREATE";
    private static final String DELETE_MESSAGE_TYPE = "ETL_PROCESS_DELETE";
    private static final String UPDATE_MESSAGE_TYPE = "ETL_PROCESS_UPDATE";
    private static final String SCRIPT_NAME = "test.rb";

    private DataloadProcess process;
    private DataloadProcess processAppstore;

    @BeforeClass(groups = {EXECUTION_MESSAGE_TYPE, CREATE_MESSAGE_TYPE, DELETE_MESSAGE_TYPE, UPDATE_MESSAGE_TYPE})
    public void setUp() throws Exception {
        createProcessFromAppstore();
        createProcess();
        badCreateProcess();
        updateProcess();
        badUpdateProcess();
        executeProcess();
        badExecuteProcess();
        removeProcess();
        badRemoveProcess();
    }

    @Test(groups = EXECUTION_MESSAGE_TYPE)
    public void testProcessManualExecutionMessageUserApi() {
        doTestUserApi(eventCheck(EXECUTION_MESSAGE_TYPE, true), EXECUTION_MESSAGE_TYPE);
    }

    @Test(groups = EXECUTION_MESSAGE_TYPE)
    public void testProcessManualExecutionMessageAdminApi() {
        doTestAdminApi(eventCheck(EXECUTION_MESSAGE_TYPE, true), EXECUTION_MESSAGE_TYPE);
    }

    @Test(groups = EXECUTION_MESSAGE_TYPE)
    public void testProcessManualExecutionMessageErrorUserApi() {
        doTestUserApi(eventCheck(EXECUTION_MESSAGE_TYPE, false), EXECUTION_MESSAGE_TYPE);
    }

    @Test(groups = EXECUTION_MESSAGE_TYPE)
    public void testProcessManualExecutionMessageErrorAdminApi() {
        doTestAdminApi(eventCheck(EXECUTION_MESSAGE_TYPE, false), EXECUTION_MESSAGE_TYPE);
    }

    @Test(groups = CREATE_MESSAGE_TYPE)
    public void testProcessCreateMessageUserApi() {
        doTestUserApi(eventCheck(CREATE_MESSAGE_TYPE, true), CREATE_MESSAGE_TYPE);
    }

    @Test(groups = CREATE_MESSAGE_TYPE)
    public void testProcessCreateMessageAdminApi() {
        doTestAdminApi(eventCheck(CREATE_MESSAGE_TYPE, true), CREATE_MESSAGE_TYPE);
    }

    @Test(groups = CREATE_MESSAGE_TYPE)
    public void testProcessCreateMessageErrorUserApi() {
        doTestUserApi(eventCheck(CREATE_MESSAGE_TYPE, false), CREATE_MESSAGE_TYPE);
    }

    @Test(groups = CREATE_MESSAGE_TYPE)
    public void testProcessCreateMessageErrorAdminApi() {
        doTestAdminApi(eventCheck(CREATE_MESSAGE_TYPE, false), CREATE_MESSAGE_TYPE);
    }

    @Test(groups = UPDATE_MESSAGE_TYPE)
    public void testProcessUpdateMessageUserApi() {
        doTestUserApi(eventCheck(UPDATE_MESSAGE_TYPE, true), UPDATE_MESSAGE_TYPE);
    }

    @Test(groups = UPDATE_MESSAGE_TYPE)
    public void testProcessUpdateMessageAdminApi() {
        doTestAdminApi(eventCheck(UPDATE_MESSAGE_TYPE, true), UPDATE_MESSAGE_TYPE);
    }

    @Test(groups = UPDATE_MESSAGE_TYPE)
    public void testProcessUpdateErrorMessageUserApi() {
        doTestUserApi(eventCheck(UPDATE_MESSAGE_TYPE, false), UPDATE_MESSAGE_TYPE);
    }

    @Test(groups = UPDATE_MESSAGE_TYPE)
    public void testProcessUpdateErrorMessageAdminApi() {
        doTestAdminApi(eventCheck(UPDATE_MESSAGE_TYPE, false), UPDATE_MESSAGE_TYPE);
    }

    @Test(groups = DELETE_MESSAGE_TYPE)
    public void testProcessDeleteMessageUserApi() {
        doTestUserApi(eventCheck(DELETE_MESSAGE_TYPE, true), DELETE_MESSAGE_TYPE);
    }

    @Test(groups = DELETE_MESSAGE_TYPE)
    public void testProcessDeleteMessageAdminApi() {
        doTestAdminApi(eventCheck(DELETE_MESSAGE_TYPE, true), DELETE_MESSAGE_TYPE);
    }

    @Test(groups = DELETE_MESSAGE_TYPE)
    public void testProcessDeleteMessageErrorUserApi() {
        doTestUserApi(eventCheck(DELETE_MESSAGE_TYPE, false), DELETE_MESSAGE_TYPE);
    }

    @Test(groups = DELETE_MESSAGE_TYPE)
    public void testProcessDeleteMessageErrorAdminApi() {
        doTestAdminApi(eventCheck(DELETE_MESSAGE_TYPE, false), DELETE_MESSAGE_TYPE);
    }

    @Test(groups = CREATE_MESSAGE_TYPE)
    public void testProcessCreateFromGitMessageUserApi() {
        doTestUserApi(eventCheckCreateFromAppstore(CREATE_MESSAGE_TYPE), CREATE_MESSAGE_TYPE);
    }

    @Test(groups = CREATE_MESSAGE_TYPE)
    public void testProcessCreateFromGitMessageAdminApi() {
        doTestAdminApi(eventCheckCreateFromAppstore(CREATE_MESSAGE_TYPE), CREATE_MESSAGE_TYPE);
    }

    private Predicate<AuditEvent> eventCheck(final String messageType, final boolean isSuccess) {
        return (e -> e.getUserLogin().equals(getAccount().getLogin()) && e.getType().equals(messageType) && e.isSuccess() == isSuccess);
    }

    private Predicate<AuditEvent> eventCheckCreateFromAppstore(final String messageType) {
        return (e -> e.getUserLogin().equals(getAccount().getLogin()) && e.getType().equals(messageType) &&
                e.isSuccess() == true && processAppstore.getUri().equals(e.getLinks().get("process")));
    }


    private void createProcessFromAppstore() throws Exception {
        processAppstore = gd.getProcessService().createProcessFromAppstore(projectHelper.getOrCreateProject(),
                new DataloadProcess(getClass().getSimpleName() + "Appstore", ProcessType.RUBY.toString(),
                        "${PUBLIC_APPSTORE}:branch/demo:/test/HelloApp")).get();

        logger.info("deployed process_id={} from appstore", processAppstore.getId());
    }

    private void createProcess() throws URISyntaxException {
        final File file = new File(getClass().getClassLoader().getResource(SCRIPT_NAME).toURI());
        process = gd.getProcessService().createProcess(projectHelper.getOrCreateProject(), new DataloadProcess(getClass().getSimpleName(), ProcessType.RUBY), file);
    }

    private void badCreateProcess() throws URISyntaxException {
        try {
            final File file = new File(getClass().getClassLoader().getResource(SCRIPT_NAME).toURI());
            final Project project = projectHelper.getOrCreateProject();
            gd.getProcessService().createProcess(project, new DataloadProcess(getClass().getSimpleName(), ProcessType.GRAPH), file);
            fail("should throw exception");
        } catch (GoodDataException ignored) {
        }
    }

    private void updateProcess() throws URISyntaxException {
        final File file = new File(getClass().getClassLoader().getResource(SCRIPT_NAME).toURI());

        gd.getProcessService().updateProcess(process, file);
    }

    private void badUpdateProcess() throws IOException {
        try {
            final File tempFile = File.createTempFile("test", "test");

            gd.getProcessService().updateProcess(process, tempFile);
            fail("should throw exception");
        } catch (GoodDataException ignored) {
        }
    }

    private void executeProcess() {
        final ProcessExecution execution = new ProcessExecution(process, SCRIPT_NAME);
        final FutureResult<ProcessExecutionDetail> result = gd.getProcessService().executeProcess(execution);
        logger.info("Process execution uri={}", result.getPollingUri());
        result.get(props.getPollTimeoutMinutes(), props.getPollTimeoutUnit());
    }

    /**
     * Executes process with bad executable so it fails on creation of execution
     */
    private void badExecuteProcess() throws NoSuchFieldException, IllegalAccessException {
        try {
            final Field executable = ProcessExecution.class.getDeclaredField("executable");
            executable.setAccessible(true);

            final ProcessExecution execution = new ProcessExecution(process, SCRIPT_NAME);
            executable.set(execution, "nonExistentExecutable");

            final FutureResult<ProcessExecutionDetail> result = gd.getProcessService().executeProcess(execution);
            result.get(props.getPollTimeoutMinutes(), props.getPollTimeoutUnit());
            fail("should throw exception");
        } catch (GoodDataException ignored) {
        }
    }

    private void removeProcess() {
        gd.getProcessService().removeProcess(process);
        gd.getProcessService().removeProcess(processAppstore);
    }

    private void badRemoveProcess() {
        try {
            final DataloadProcess badProcess = mock(DataloadProcess.class);
            doReturn("/gdc/projects/" + projectHelper.getOrCreateProject().getId() + "/dataload/processes/aaa").when(badProcess).getUri();
            gd.getProcessService().removeProcess(badProcess);
            fail("should throw exception");
        } catch (GoodDataException ignored) {
        }
    }
}
