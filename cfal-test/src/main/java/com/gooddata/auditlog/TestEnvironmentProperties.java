/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import static java.lang.System.getProperty;

import java.util.concurrent.TimeUnit;

/**
 * This class encapsulates environment properties for test
 */
public class TestEnvironmentProperties {

    private static final TimeUnit POLL_TIMEOUT_UNIT = TimeUnit.MINUTES;

    private final String host;
    private final String user;
    private final String pass;
    private final String domain;
    private final String projectToken;
    private final String projectId;
    private final String datawarehouseToken;
    private final Boolean keepProject;
    private final Integer pollTimeoutMinutes;

    public TestEnvironmentProperties() {
        host = getProperty("host", "localhost");
        user = getProperty("user", "bear@gooddata.com");
        pass = getProperty("pass", "jindrisska");
        domain = getProperty("domain", "default");
        projectToken = getProperty("projectToken");
        projectId = getProperty("projectId");
        datawarehouseToken = getProperty("datawarehouseToken", "vertica");
        keepProject = Boolean.getBoolean("keepProject");
        pollTimeoutMinutes = Integer.getInteger("pollTimeoutMinutes", 5);
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getDomain() {
        return domain;
    }

    public String getProjectToken() {
        return projectToken;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getDatawarehouseToken() {
        return datawarehouseToken;
    }

    public Boolean getKeepProject() {
        return keepProject;
    }

    public Integer getPollTimeoutMinutes() {
        return pollTimeoutMinutes;
    }

    public TimeUnit getPollTimeoutUnit() {
        return POLL_TIMEOUT_UNIT;
    }
}
