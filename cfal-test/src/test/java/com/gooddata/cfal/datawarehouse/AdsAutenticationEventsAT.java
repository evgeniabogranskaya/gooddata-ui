/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.datawarehouse;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Acceptance test for ADS login and logout
 */
public class AdsAutenticationEventsAT extends AbstractAdsAT {

    private static final String TEST_QUERY = "SELECT 1";
    private static final String MESSAGE_TYPE_LOGIN = "DATAWAREHOUSE_USERNAME_PASSWORD_LOGIN";
    private static final String MESSAGE_TYPE_LOGOUT = "DATAWAREHOUSE_LOGOUT";

    @BeforeClass(groups = {MESSAGE_TYPE_LOGIN, MESSAGE_TYPE_LOGOUT})
    public void setUp() {
        getJdbcTemplate().execute(TEST_QUERY);
    }

    @Test(groups = MESSAGE_TYPE_LOGIN)
    public void testUsernamePasswordLoginUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(MESSAGE_TYPE_LOGIN), MESSAGE_TYPE_LOGIN);
    }

    @Test(groups = MESSAGE_TYPE_LOGIN)
    public void testUsernamePasswordLoginAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(MESSAGE_TYPE_LOGIN), MESSAGE_TYPE_LOGIN);
    }

    @Test(groups = MESSAGE_TYPE_LOGOUT)
    public void testDatawarehouseLogoutUserApi() throws Exception {
        doTestUserApi(pageCheckPredicate(MESSAGE_TYPE_LOGOUT), MESSAGE_TYPE_LOGOUT);
    }

    @Test(groups = MESSAGE_TYPE_LOGOUT)
    public void testDatawarehouseLogoutAdminApi() throws Exception {
        doTestAdminApi(pageCheckPredicate(MESSAGE_TYPE_LOGOUT), MESSAGE_TYPE_LOGOUT);
    }
}
