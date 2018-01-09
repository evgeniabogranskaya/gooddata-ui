/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.test.ssh.Authentication;

import static com.gooddata.test.ssh.Authentication.pubKeyAuth;
import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * This singleton encapsulates environment properties for test.
 * Lazy initialized, not thread safe.
 */
public class TestEnvironmentProperties {

    private static TestEnvironmentProperties instance;

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
    private final Authentication sshAuth;
    private final int notificationWaitSeconds;
    private final int scheduledMailWaitSeconds;

    private TestEnvironmentProperties() {
        host = getProperty("host", "localhost");
        user = getProperty("user", "bear@gooddata.com");
        pass = getProperty("pass", "jindrisska");
        domain = getProperty("domain", "default");
        projectToken = getProperty("projectToken");
        projectId = getProperty("projectId");
        datawarehouseToken = getProperty("datawarehouseToken", "vertica");
        keepProject = Boolean.getBoolean("keepProject");
        pollTimeoutMinutes = Integer.getInteger("pollTimeoutMinutes", 5);
        notificationWaitSeconds = Integer.getInteger("notificationWaitSeconds", 30);
        scheduledMailWaitSeconds = Integer.getInteger("scheduledMailWaitSeconds",60);

        final String sshKey = getProperty("sshKey", null);
        final File sshKeyFile = isEmpty(sshKey) ? new File(getProperty("user.home"), ".ssh/id_rsa") : new File(sshKey);
        final String sshKeyPass = getProperty("sshKeyPass", null);
        final String sshUser = getProperty("sshUser", getProperty("user.name"));
        sshAuth = isEmpty(sshKeyPass) ? pubKeyAuth(sshUser, sshKeyFile) : pubKeyAuth(sshUser, sshKeyFile, sshKeyPass);
    }

    public static TestEnvironmentProperties getInstance() {
        if (instance == null) {
            instance = new TestEnvironmentProperties();
        }
        return instance;
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

    public Authentication getSshAuth() {
        return sshAuth;
    }

    public int getNotificationWaitSeconds() {
        return notificationWaitSeconds;
    }

    public int getScheduledMailWaitSeconds() {
        return scheduledMailWaitSeconds;
    }
}
