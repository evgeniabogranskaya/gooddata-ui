/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.msf;

import com.gooddata.FutureResult;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.test.AbstractAT;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecution;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.dataload.processes.ProcessExecutionException;
import com.gooddata.dataload.processes.ProcessType;
import com.gooddata.project.Project;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Predicate;

/**
 * Tests ETL process manual execution
 */
public class ETLProcessManualExecutionAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "ETL_PROCESS_MANUAL_EXECUTION";
    private static final String SCRIPT_NAME = "test.rb";

    private DataloadProcess process;

    @Test(groups = MESSAGE_TYPE)
    public void createProcess() throws URISyntaxException {
        process = createProcessFromResource(SCRIPT_NAME);
    }

    @Test(dependsOnMethods = "createProcess", groups = MESSAGE_TYPE)
    public void executeProcess() {
        final FutureResult<ProcessExecutionDetail> result = gd.getProcessService().executeProcess(new ProcessExecution(process, SCRIPT_NAME));
        result.get();
    }

    /**
     * Executes process with bad executable so it fails on creation of execution
     */
    @Test(dependsOnMethods = "createProcess", groups = MESSAGE_TYPE,  expectedExceptions = ProcessExecutionException.class)
    public void badExecuteProcess() throws NoSuchFieldException, IllegalAccessException {
        final Field executable = ProcessExecution.class.getDeclaredField("executable");
        executable.setAccessible(true);

        final ProcessExecution execution = new ProcessExecution(process, SCRIPT_NAME);
        executable.set(execution, "nonExistentExecutable");

        final FutureResult<ProcessExecutionDetail> result = gd.getProcessService().executeProcess(execution);
        result.get();
    }

    @Test(dependsOnMethods = "executeProcess", groups = MESSAGE_TYPE)
    public void testProcessManualExecutionMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(true));
    }

    @Test(dependsOnMethods = "executeProcess", groups = MESSAGE_TYPE)
    public void testProcessManualExecutionMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(true));
    }

    @Test(dependsOnMethods = "badExecuteProcess", groups = MESSAGE_TYPE)
    public void testProcessManualExecutionMessageErrorUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(false));
    }

    @Test(dependsOnMethods = "badExecuteProcess", groups = MESSAGE_TYPE)
    public void testProcessManualExecutionMessageErrorAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(false));
    }

    @AfterGroups(groups = MESSAGE_TYPE)
    public void removeProcess() {
        if(process != null) {
            gd.getProcessService().removeProcess(process);
        }
    }

    private DataloadProcess createProcessFromResource(final String script) throws URISyntaxException {
        final File file = new File(getClass().getClassLoader().getResource(script).toURI());
        final Project project = gd.getProjectService().getProjectById(projectId);
        return gd.getProcessService().createProcess(project, new DataloadProcess(getClass().getSimpleName(), ProcessType.RUBY), file);
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final boolean isSuccess) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE) && e.isSuccess() == isSuccess);
    }
}
