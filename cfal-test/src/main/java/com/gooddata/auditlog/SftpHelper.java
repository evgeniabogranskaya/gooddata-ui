/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.auditlog;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
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
        final Session session = new JSch().getSession(props.getSftpLogin(), props.getSftpHost());
        session.setUserInfo(new SftpUserInfo(password));

        try {
            session.connect();
            logger.info("SFTP logged in to the host={}", props.getSftpHost());
        } finally {
            session.disconnect();
            logger.info("SFTP logged out from the host={}", props.getSftpHost());
        }
    }

    private static class SftpUserInfo implements UserInfo {

        private final String password;

        private SftpUserInfo(final String password) {
            this.password = password;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean promptPassword(final String message) {
            return true;
        }

        @Override
        public boolean promptPassphrase(final String message) {
            return true;
        }

        @Override
        public boolean promptYesNo(final String message) {
            return true;
        }

        @Override
        public void showMessage(String message) {
        }
    }
}
