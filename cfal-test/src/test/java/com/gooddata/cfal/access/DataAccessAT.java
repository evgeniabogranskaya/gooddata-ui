/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.access;

import static com.gooddata.md.Restriction.identifier;

import com.gooddata.ReportExecuteService;
import com.gooddata.auditevent.AuditEvent;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.export.ExecuteReport;
import com.gooddata.export.ExecuteReportDefinition;
import com.gooddata.md.Attribute;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.project.Project;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.function.Predicate;

public class DataAccessAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "DATA_ACCESS";
    private static final String DATA_RESULT = "dataResult";
    private static final String VALID_ELEMENTS = "validElements";
    private static final String ELEMENTS = "elements";

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

        final Attribute attr = gd.getMetadataService().getObj(project, Attribute.class, identifier("attr.star.name"));

        gd.getMetadataService().getAttributeValidElements(attr);

        gd.getMetadataService().getAttributeElements(attr);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testDataAccessMessageUserApi() throws Exception {
        doTestUserApi(eventCheck(), MESSAGE_TYPE, reportExecuteService.getTimesExecuted());
    }

    @Test(groups = MESSAGE_TYPE)
    public void testDataAccessMessageAdminApi() throws Exception {
        doTestAdminApi(eventCheck(), MESSAGE_TYPE, reportExecuteService.getTimesExecuted());
    }

    @Test(groups = MESSAGE_TYPE)
    public void testValidElementsAccessMessageUserApi() throws Exception {
        doTestUserApi(validElementsEventCheck(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testValidElementsAccessMessageAdminApi() throws Exception {
        doTestAdminApi(validElementsEventCheck(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testElementsAccessMessageUserApi() throws Exception {
        doTestUserApi(elementsEventCheck(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testElementsAccessMessageAdminApi() throws Exception {
        doTestAdminApi(elementsEventCheck(), MESSAGE_TYPE);
    }

    private Predicate<AuditEvent> eventCheck() {
        return e -> e.getUserLogin().equals(getAccount().getLogin())
                && e.getType().equals(MESSAGE_TYPE)
                && e.isSuccess()
                && DATA_RESULT.equals(e.getParams().get("type"))
                && project.getUri().equals(e.getLinks().get("project"));
    }

    private Predicate<AuditEvent> validElementsEventCheck() {
        return e -> e.getUserLogin().equals(getAccount().getLogin())
                && e.getType().equals(MESSAGE_TYPE)
                && e.isSuccess()
                && VALID_ELEMENTS.equals(e.getParams().get("type"))
                && project.getUri().equals(e.getLinks().get("project"));
    }

    private Predicate<AuditEvent> elementsEventCheck() {
        return e -> e.getUserLogin().equals(getAccount().getLogin())
                && e.getType().equals(MESSAGE_TYPE)
                && e.isSuccess()
                && ELEMENTS.equals(e.getParams().get("type"))
                && project.getUri().equals(e.getLinks().get("project"));
    }
}
