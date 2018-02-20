/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.auditlog;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton for SFTP related stuff.
 * Lazy initialized. Not thread safe.
 */
public class SftpHelper {

    private static final Logger logger = LoggerFactory.getLogger(SftpHelper.class);

    private static SftpHelper instance;

    private final TestEnvironmentProperties props;

    private SftpHelper(final TestEnvironmentProperties props) {
        this.props = props;
    }

    public static SftpHelper getInstance() {
        if (instance == null) {
            instance = new SftpHelper(TestEnvironmentProperties.getInstance());
        }
        return instance;
    }

    public void login(final String password) throws JSchException {
        logger.info("log in user={} to SFTP host={}", props.getSftpLogin(), props.getSftpHost());
        final Session session = new JSch().getSession(props.getSftpLogin(), props.getSftpHost());
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "password");
        session.setPassword(password);
        session.setTimeout(props.getSftpLoginTimeoutSeconds() * 1000);

        try {
            session.connect();
            logger.info("SFTP logged in to the host={}", props.getSftpHost());
        } finally {
            session.disconnect();
            logger.info("SFTP logged out from the host={}", props.getSftpHost());
        }
    }
}
