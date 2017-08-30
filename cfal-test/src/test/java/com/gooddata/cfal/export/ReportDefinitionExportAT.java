/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.export;

import com.gooddata.export.ExportFormat;
import com.gooddata.export.NoDataExportException;
import com.gooddata.md.report.ReportDefinition;
import org.apache.commons.io.output.NullOutputStream;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ReportDefinitionExportAT extends AbstractExportAT {

    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @BeforeClass
    public void export() throws Exception {
        final ReportDefinition definition = metadata.getOrCreateReportDefinition();
        try {
            gd.getExportService().export(definition, ExportFormat.XLSX, new NullOutputStream()).get();
        } catch (NoDataExportException ignored) {
            // we were lazy and we haven't loaded any data into the project, so there are no data in export
        }
    }

    @Test(groups = MESSAGE_TYPE)
    public void testDefinitionMessageUserApi() throws Exception {
        doTestUserApi(eventCheck(XLSX_CONTENT_TYPE), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testDefinitionMessageAdminApi() throws Exception {
        doTestAdminApi(eventCheck(XLSX_CONTENT_TYPE), MESSAGE_TYPE);
    }

}
