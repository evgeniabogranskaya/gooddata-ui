/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.export;

import com.gooddata.auditevent.AuditEvent;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.md.ProjectDashboard;
import com.gooddata.md.ProjectDashboard.Tab;
import com.gooddata.project.Project;
import org.apache.commons.io.output.NullOutputStream;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.function.Predicate;

public class DashboardExportAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "DATA_EXPORT";

    private ProjectDashboard dashboard;
    private Project project;

    @BeforeClass(groups = MESSAGE_TYPE)
    public void createAndExportDashboard() throws Exception {
        final ProjectDashboard dashboard = new ProjectDashboard("My dashboard", new Tab("My tab"));

        this.project = projectHelper.getOrCreateProject();
        this.dashboard = gd.getMetadataService().createObj(project, dashboard);

        gd.getExportService().exportPdf(this.dashboard, this.dashboard.getTabs().iterator().next(), new NullOutputStream()).get();
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLoginMessageUserApi() throws Exception {
        doTestUserApi(eventCheck(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLoginMessageAdminApi() throws Exception {
        doTestAdminApi(eventCheck(), MESSAGE_TYPE);
    }

    private Predicate<AuditEvent> eventCheck() {
        return (e ->
                e.getUserLogin().equals(getAccount().getLogin()) &&
                        e.getType().equals(MESSAGE_TYPE) &&
                        e.isSuccess() &&
                        dashboard.getUri().equals(e.getLinks().get("dashboard")) &&
                        project.getUri().equals(e.getLinks().get("project")) &&
                        "application/pdf".equals(e.getParams().get("format"))
        );
    }

}
