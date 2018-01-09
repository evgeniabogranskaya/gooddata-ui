/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LoginAT extends AbstractLoginAT {

    private static final String USERNAME_PASSWORD = "USERNAME_PASSWORD";

    @BeforeClass(groups = MESSAGE_TYPE)
    public void setUp() {
        loginHelper.usernamePasswordLogin();
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLoginMessageUserApi() {
        doTestUserApi(eventCheck(true, WEBAPP, USERNAME_PASSWORD), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLoginMessageAdminApi() {
        doTestAdminApi(eventCheck(true, WEBAPP, USERNAME_PASSWORD), MESSAGE_TYPE);
    }
}
