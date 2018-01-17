/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata;

import com.gooddata.export.ExportService;
import com.gooddata.md.ProjectDashboard;
import org.apache.commons.io.output.NullOutputStream;
import org.springframework.web.client.RestTemplate;

import java.io.OutputStream;

/**
 * ExportService extended with functionality needed in acceptance tests
 */
public class ExtendedExportService extends ExportService {

    public ExtendedExportService(final RestTemplate restTemplate, final GoodDataEndpoint endpoint) {
        super(restTemplate, endpoint);
    }

    /**
     * Runs dashboard export for the first tab of given dashboard and saves exported data to {@link NullOutputStream}.
     * This calls PDF export (see {@link ExportService#exportPdf(ProjectDashboard, ProjectDashboard.Tab, OutputStream)})
     *
     * @param dashboard project dashboard to export
     */
    public void runExportDashboard(final ProjectDashboard dashboard) {
        exportPdf(dashboard, dashboard.getTabs().iterator().next(), new NullOutputStream()).get();
    }
}
