/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.datawarehouse;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

/**
 * Acceptance test for ADS platform access event
 */
public class AdsPlatformAccessEventAT extends AbstractAdsAT {

    private static final String TEST_QUERY = "SELECT 1";
    private static final String MESSAGE_TYPE_ACCESS = "DATAWAREHOUSE_DATA_ACCESS";

    @Test(groups = MESSAGE_TYPE_ACCESS)
    public void testSelectUserApi() throws InterruptedException {
        getJdbcTemplate().execute(TEST_QUERY);
        doTestUserApi(pageCheckPredicate(MESSAGE_TYPE_ACCESS));
    }

    @Test(groups = MESSAGE_TYPE_ACCESS)
    public void testSelectAdminApi() throws InterruptedException {
        getJdbcTemplate().execute(TEST_QUERY);
        doTestAdminApi(pageCheckPredicate(MESSAGE_TYPE_ACCESS));
    }

    @AfterGroups(groups = MESSAGE_TYPE_ACCESS)
    public void tearDown() {
        safelyDeleteAds();
    }

}
