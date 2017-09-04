/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.export;

import com.gooddata.export.ExportFormat;
import com.gooddata.export.NoDataExportException;
import com.gooddata.md.report.Report;
import org.apache.commons.io.output.NullOutputStream;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ReportExportAT extends AbstractExportAT {

    private static final String XLS_CONTENT_TYPE = "application/vnd.ms-excel";

    @BeforeClass
    public void export() throws Exception {
        final Report report = metadata.getOrCreateReport();
        try {
            gd.getExportService().export(report, ExportFormat.XLS, new NullOutputStream()).get();
        } catch (NoDataExportException ignored) {
            // we were lazy and we haven't loaded any data into the project, so there are no data in export
        }
    }

    @Test(groups = MESSAGE_TYPE)
    public void testReportMessageUserApi() throws Exception {
        doTestUserApi(pageCheckPredicate(XLS_CONTENT_TYPE), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testReportMessageAdminApi() throws Exception {
        doTestAdminApi(pageCheckPredicate(XLS_CONTENT_TYPE), MESSAGE_TYPE);
    }

}
