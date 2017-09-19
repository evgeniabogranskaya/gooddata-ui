/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;

/**
 * Class representing state of project metadata handled by {@link MetadataHelper}
 */
public class ProjectMetadataState {

    private Report report;

    private ReportDefinition reportDefinition;

    private boolean needSynchronize;

    private boolean isDataLoaded;

    public Report getReport() {
        return report;
    }

    public ReportDefinition getReportDefinition() {
        return reportDefinition;
    }

    public boolean isNeedSynchronize() {
        return needSynchronize;
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    public void setReport(final Report report) {
        this.report = report;
    }

    public void setReportDefinition(final ReportDefinition reportDefinition) {
        this.reportDefinition = reportDefinition;
    }

    public void setNeedSynchronize(final boolean needSynchronize) {
        this.needSynchronize = needSynchronize;
    }

    public void setDataLoaded(final boolean dataLoaded) {
        isDataLoaded = dataLoaded;
    }
}
