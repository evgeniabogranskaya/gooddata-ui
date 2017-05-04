/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.datawarehouse;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

/**
 * Acceptance test for ADS login and logout
 */
public class AdsAutenticationEventsAT extends AbstractAdsAT {

    private static final String TEST_QUERY = "SELECT 1";
    private static final String MESSAGE_TYPE_LOGIN = "DATAWAREHOUSE_USERNAME_PASSWORD_LOGIN";
    private static final String MESSAGE_TYPE_LOGOUT = "DATAWAREHOUSE_LOGOUT";

    @Test(groups = MESSAGE_TYPE_LOGIN)
    public void testUsernamePasswordAuthUserApi() throws InterruptedException {
        getJdbcTemplate().execute(TEST_QUERY);
        doTestUserApi(pageCheckPredicate(MESSAGE_TYPE_LOGIN));
        doTestUserApi(pageCheckPredicate(MESSAGE_TYPE_LOGOUT));
    }

    @Test(groups = MESSAGE_TYPE_LOGIN)
    public void testUsernamePasswordAuthAdminApi() throws InterruptedException {
        getJdbcTemplate().execute(TEST_QUERY);
        doTestAdminApi(pageCheckPredicate(MESSAGE_TYPE_LOGIN));
        doTestAdminApi(pageCheckPredicate(MESSAGE_TYPE_LOGOUT));
    }

    @AfterGroups(groups = MESSAGE_TYPE_LOGIN)
    public void tearDown() {
        safelyDeleteAds();
    }
}
