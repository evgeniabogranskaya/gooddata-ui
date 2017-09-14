/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.export;

import com.gooddata.auditevent.AuditEvent;
import com.gooddata.auditlog.MetadataHelper;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.export.ExportFormat;
import com.gooddata.export.NoDataExportException;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.project.Project;
import org.apache.commons.io.output.NullOutputStream;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.function.Predicate;

public class ExportAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "DATA_EXPORT";

    private static final String RAW_CSV_FORMAT = "raw";
    private static final String XLS_FORMAT = "application/vnd.ms-excel";
    private static final String XLSX_FORMAT = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private MetadataHelper metadata;

    private Project project;
    private Report report;
    private ReportDefinition definition;

    @BeforeClass
    public void createMetadata() throws Exception {
        project = projectHelper.getOrCreateProject();

        metadata = MetadataHelper.getInstance(gd, project);
        report = metadata.getOrCreateReport();
        definition = metadata.getOrCreateReportDefinition();
        export();
    }

    private void export() throws Exception {
        try {
            gd.getExportService().export(report, ExportFormat.XLS, new NullOutputStream()).get();
        } catch (NoDataExportException ignored) {
            // we were lazy and we haven't loaded any data into the project, so there are no data in export
        }
        try {
            gd.getExportService().export(definition, ExportFormat.XLSX, new NullOutputStream()).get();
        } catch (NoDataExportException ignored) {
            // we were lazy and we haven't loaded any data into the project, so there are no data in export
        }
    }

    private void rawExport() {
        gd.getExportService().exportCsv(definition, new NullOutputStream()).get();
    }

    @Test(groups = MESSAGE_TYPE)
    public void emptyExport() throws Exception {

        doTestUserApi(eventCheck(XLS_FORMAT, false), MESSAGE_TYPE);
        doTestUserApi(eventCheck(XLSX_FORMAT, false), MESSAGE_TYPE);

        doTestAdminApi(eventCheck(XLS_FORMAT, false), MESSAGE_TYPE);
        doTestAdminApi(eventCheck(XLSX_FORMAT, false), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "emptyExport")
    public void dataExport() throws Exception {
        metadata.ensureDataLoaded();
        export();
        rawExport();

        doTestUserApi(eventCheck(XLS_FORMAT, true), MESSAGE_TYPE);
        doTestUserApi(eventCheck(XLSX_FORMAT, true), MESSAGE_TYPE);
        doTestUserApi(eventCheck(RAW_CSV_FORMAT, true), MESSAGE_TYPE);

        doTestAdminApi(eventCheck(XLS_FORMAT, true), MESSAGE_TYPE);
        doTestAdminApi(eventCheck(XLSX_FORMAT, true), MESSAGE_TYPE);
        doTestAdminApi(eventCheck(RAW_CSV_FORMAT, true), MESSAGE_TYPE);
    }

    protected Predicate<AuditEvent> eventCheck(final String format, final boolean success) {
        return (e ->
                e.getUserLogin().equals(getAccount().getLogin()) &&
                        e.getType().equals(MESSAGE_TYPE) &&
                        e.isSuccess() == success &&
                        project.getUri().equals(e.getLinks().get("project")) &&
                        format.equals(e.getParams().get("format"))
        );
    }
}
