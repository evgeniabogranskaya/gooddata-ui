/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.msf;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.fail;

import com.gooddata.FutureResult;
import com.gooddata.GoodDataException;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.test.AbstractProjectAT;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecution;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.dataload.processes.ProcessType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Predicate;

/**
 * Tests ETL process messages
 */
@Test
public class ETLProcessAT extends AbstractProjectAT {

    private static final String EXECUTION_MESSAGE_TYPE = "ETL_PROCESS_MANUAL_EXECUTION";
    private static final String CREATE_MESSAGE_TYPE = "ETL_PROCESS_CREATION";
    private static final String DELETE_MESSAGE_TYPE = "ETL_PROCESS_DELETION";
    private static final String UPDATE_MESSAGE_TYPE = "ETL_PROCESS_UPDATE";
    private static final String SCRIPT_NAME = "test.rb";

    private DataloadProcess process;

    @BeforeClass
    public void createProcess() throws URISyntaxException {
        final File file = new File(getClass().getClassLoader().getResource(SCRIPT_NAME).toURI());
        process = gd.getProcessService().createProcess(project, new DataloadProcess(getClass().getSimpleName(), ProcessType.RUBY), file);
    }

    @BeforeClass
    public void badCreateProcess() throws URISyntaxException {
        try {
            final File file = new File(getClass().getClassLoader().getResource(SCRIPT_NAME).toURI());
            gd.getProcessService().createProcess(project, new DataloadProcess(getClass().getSimpleName(), ProcessType.GRAPH), file);
            fail("should throw exception");
        } catch (GoodDataException ignored) {
        }
    }

    @BeforeClass(dependsOnMethods = "createProcess")
    public void updateProcess() throws URISyntaxException {
        final File file = new File(getClass().getClassLoader().getResource(SCRIPT_NAME).toURI());

        gd.getProcessService().updateProcess(process, file);
    }

    @BeforeClass(dependsOnMethods = "createProcess")
    public void badUpdateProcess() throws IOException {
        try {
            final File tempFile = File.createTempFile("test", "test");

            gd.getProcessService().updateProcess(process, tempFile);
            fail("should throw exception");
        } catch (GoodDataException ignored) {
        }
    }

    @BeforeClass(dependsOnMethods = "createProcess")
    public void executeProcess() {
        final ProcessExecution execution = new ProcessExecution(process, SCRIPT_NAME);
        final FutureResult<ProcessExecutionDetail> result = gd.getProcessService().executeProcess(execution);
        logger.info("Process execution uri={}", result.getPollingUri());
        result.get(props.getPollTimeoutMinutes(), props.getPollTimeoutUnit());
    }

    /**
     * Executes process with bad executable so it fails on creation of execution
     */
    @BeforeClass(dependsOnMethods = "createProcess")
    public void badExecuteProcess() throws NoSuchFieldException, IllegalAccessException {
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

    @BeforeClass(dependsOnMethods = {"createProcess", "updateProcess", "badUpdateProcess", "executeProcess", "badExecuteProcess"})
    public void removeProcess() {
        gd.getProcessService().removeProcess(process);
    }

    @BeforeClass
    public void badRemoveProcess() {
        try {
            final DataloadProcess badProcess = mock(DataloadProcess.class);
            doReturn("/gdc/projects/" + project.getId() + "/dataload/processes/aaa").when(badProcess).getUri();
            gd.getProcessService().removeProcess(badProcess);
            fail("should throw exception");
        } catch (GoodDataException ignored) {
        }
    }

    @Test(groups = EXECUTION_MESSAGE_TYPE)
    public void testProcessManualExecutionMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(EXECUTION_MESSAGE_TYPE, true), EXECUTION_MESSAGE_TYPE);
    }

    @Test(groups = EXECUTION_MESSAGE_TYPE)
    public void testProcessManualExecutionMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(EXECUTION_MESSAGE_TYPE, true), EXECUTION_MESSAGE_TYPE);
    }

    @Test(groups = EXECUTION_MESSAGE_TYPE)
    public void testProcessManualExecutionMessageErrorUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(EXECUTION_MESSAGE_TYPE, false), EXECUTION_MESSAGE_TYPE);
    }

    @Test(groups = EXECUTION_MESSAGE_TYPE)
    public void testProcessManualExecutionMessageErrorAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(EXECUTION_MESSAGE_TYPE, false), EXECUTION_MESSAGE_TYPE);
    }

    @Test(groups = CREATE_MESSAGE_TYPE)
    public void testProcessCreateMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(CREATE_MESSAGE_TYPE, true), CREATE_MESSAGE_TYPE);
    }

    @Test(groups = CREATE_MESSAGE_TYPE)
    public void testProcessCreateMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(CREATE_MESSAGE_TYPE, true), CREATE_MESSAGE_TYPE);
    }

    @Test(groups = CREATE_MESSAGE_TYPE)
    public void testProcessCreateMessageErrorUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(CREATE_MESSAGE_TYPE, false), CREATE_MESSAGE_TYPE);
    }

    @Test(groups = CREATE_MESSAGE_TYPE)
    public void testProcessCreateMessageErrorAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(CREATE_MESSAGE_TYPE, false), CREATE_MESSAGE_TYPE);
    }

    @Test(groups = UPDATE_MESSAGE_TYPE)
    public void testProcessUpdateMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(UPDATE_MESSAGE_TYPE, true), UPDATE_MESSAGE_TYPE);
    }

    @Test(groups = UPDATE_MESSAGE_TYPE)
    public void testProcessUpdateMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(UPDATE_MESSAGE_TYPE, true), UPDATE_MESSAGE_TYPE);
    }

    @Test(groups = UPDATE_MESSAGE_TYPE)
    public void testProcessUpdateErrorMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(UPDATE_MESSAGE_TYPE, false), UPDATE_MESSAGE_TYPE);
    }

    @Test(groups = UPDATE_MESSAGE_TYPE)
    public void testProcessUpdateErrorMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(UPDATE_MESSAGE_TYPE, false), UPDATE_MESSAGE_TYPE);
    }

    @Test(groups = DELETE_MESSAGE_TYPE)
    public void testProcessDeleteMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(DELETE_MESSAGE_TYPE, true), DELETE_MESSAGE_TYPE);
    }

    @Test(groups = DELETE_MESSAGE_TYPE)
    public void testProcessDeleteMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(DELETE_MESSAGE_TYPE, true), DELETE_MESSAGE_TYPE);
    }

    @Test(groups = DELETE_MESSAGE_TYPE)
    public void testProcessDeleteMessageErrorUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(DELETE_MESSAGE_TYPE, false), DELETE_MESSAGE_TYPE);
    }

    @Test(groups = DELETE_MESSAGE_TYPE)
    public void testProcessDeleteMessageErrorAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(DELETE_MESSAGE_TYPE, false), DELETE_MESSAGE_TYPE);
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final String messageType, final boolean isSuccess) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(messageType) && e.isSuccess() == isSuccess);
    }
}
