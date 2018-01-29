/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.cfal.account;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SftpLoginAT extends AbstractLoginAT {

    private static final String LOGIN_TYPE = "SFTP";
    private static final String GROUP = "SFTP_LOGIN";
    private static final String COMPONENT = "WEBDAV";

    @BeforeClass(groups = GROUP)
    public void successfulSftpLogin() throws Exception {
        sftpHelper.login(props.getPass());
    }

    @Test(groups = GROUP)
    public void testSftpLoginUserApi() {
        doTestUserApi(eventCheck(true, COMPONENT, LOGIN_TYPE), MESSAGE_TYPE);
    }

    @Test(groups = GROUP)
    public void testSftpLoginAdminApi() {
        doTestAdminApi(eventCheck(true, COMPONENT, LOGIN_TYPE), MESSAGE_TYPE);
    }
}
