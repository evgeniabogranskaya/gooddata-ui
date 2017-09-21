/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.access;

import com.gooddata.ReportExecuteService;
import com.gooddata.auditevent.AuditEvent;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.export.ExecuteReport;
import com.gooddata.export.ExecuteReportDefinition;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.project.Project;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.function.Predicate;

public class DataAccessAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "DATA_ACCESS";
    private static final String DATA_RESULT = "dataResult";

    private Project project;

    private final ReportExecuteService reportExecuteService = gd.getReportExecuteService();

    @BeforeClass(groups = MESSAGE_TYPE)
    public void setUp() throws Exception {
        project = projectHelper.getOrCreateProject();

        final Report report = metadataHelper.getOrCreateReport(project);
        final ReportDefinition reportDefinition = metadataHelper.getOrCreateReportDefinition(project);

        metadataHelper.ensureDataLoaded(project);

        final ExecuteReport reportRequest = new ExecuteReport(report);
        final ExecuteReportDefinition reportDefinitionRequest = new ExecuteReportDefinition(reportDefinition);

        reportExecuteService.executeUsingAppExecuteResource(project, reportRequest);
        reportExecuteService.executeUsingAppExecuteResource(project, reportDefinitionRequest);

        reportExecuteService.executeUsingExecuteResource(project, reportRequest);
        reportExecuteService.executeUsingExecuteResource(project, reportDefinitionRequest);

        reportExecuteService.executeUsingXtabExecutorResource(project, reportRequest);
        reportExecuteService.executeUsingXtabExecutorResource(project, reportDefinitionRequest);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testDataAccessMessageUserApi() throws Exception {
        doTestUserApi(eventCheck(), MESSAGE_TYPE, reportExecuteService.getTimesExecuted());
    }

    @Test(groups = MESSAGE_TYPE)
    public void testDataAccessMessageAdminApi() throws Exception {
        doTestAdminApi(eventCheck(), MESSAGE_TYPE, reportExecuteService.getTimesExecuted());
    }

    private Predicate<AuditEvent> eventCheck() {
        return e -> e.getUserLogin().equals(getAccount().getLogin())
                && e.getType().equals(MESSAGE_TYPE)
                && e.isSuccess()
                && DATA_RESULT.equals(e.getParams().get("type"))
                && project.getUri().equals(e.getLinks().get("project"));
    }
}
