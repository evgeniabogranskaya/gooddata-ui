/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gooddata.context.GdcCallContext;

/**
 * Audit Log Event with Project Link.
 */
public class ProjectAuditLogEvent extends AuditLogEvent {

    private static final String PROJECT = "project";
    private static final String PROJECT_URI_PREFIX = "/gdc/projects/";

    public ProjectAuditLogEvent(final String type, final String userLogin, final String userIp,
                                final String domainId, final String projectUri, final boolean success) {
        super(type, userLogin, userIp, domainId, success);
        addProjectLink(projectUri);
    }

    public ProjectAuditLogEvent(final String type, final boolean success) {
        super(type, success);
        addProjectIdLink(GdcCallContext.getCurrentContext().getProjectId());
    }

    protected void addProjectIdLink(final String projectId) {
        addProjectLink(PROJECT_URI_PREFIX + projectId);
    }

    protected void addProjectLink(final String projectUri) {
        addLink(PROJECT, projectUri);
    }

    /**
     * @return project uri
     */
    @JsonIgnore
    public String getProject() {
        return getLink(PROJECT);
    }
}
