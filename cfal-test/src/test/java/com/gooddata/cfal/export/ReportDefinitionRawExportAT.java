/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.export;

import com.gooddata.export.NoDataExportException;
import com.gooddata.md.report.ReportDefinition;
import org.apache.commons.io.output.NullOutputStream;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ReportDefinitionRawExportAT extends AbstractExportAT {

    private static final String CSV_CONTENT_TYPE = "text/csv";

    @BeforeClass
    public void export() throws Exception {
        final ReportDefinition definition = metadata.getOrCreateReportDefinition();
        try {
            gd.getExportService().exportCsv(definition, new NullOutputStream()).get();
        } catch (NoDataExportException ignored) {
            // we were lazy and we haven't loaded any data into the project, so there are no data in export
        }
    }

    @Test(groups = MESSAGE_TYPE)
    public void testDefinitionMessageUserApi() throws Exception {
        doTestUserApi(pageCheckPredicate(CSV_CONTENT_TYPE), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testDefinitionMessageAdminApi() throws Exception {
        doTestAdminApi(pageCheckPredicate(CSV_CONTENT_TYPE), MESSAGE_TYPE);
    }

}
