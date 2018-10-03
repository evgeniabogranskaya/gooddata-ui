/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import com.gooddata.auditlog.LoginHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SSOLoginAT extends AbstractLoginAT {

    private static final String SSO = "SSO";


    private ResponseEntity<String> ssoLoginResult;

    @BeforeClass(groups = MESSAGE_TYPE)
    public void setUp() throws Exception {
        ssoLoginResult = loginHelper.ssoLogin(getAccount());
    }

    @Test(groups = MESSAGE_TYPE)
    public void shouldLoginUserWithSSO() {
        assertThat(ssoLoginResult.getHeaders().getLocation().getPath(), is(LoginHelper.SSO_LOGIN_TARGET_URL));
        assertThat(ssoLoginResult.getStatusCode(), is(HttpStatus.FOUND));
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "shouldLoginUserWithSSO")
    public void testLoginMessageUserApi() {
        doTestUserApi(eventCheck(true, WEBAPP, SSO), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "shouldLoginUserWithSSO")
    public void testLoginMessageAdminApi() {
        doTestAdminApi(eventCheck(true, WEBAPP, SSO), MESSAGE_TYPE);
    }
}