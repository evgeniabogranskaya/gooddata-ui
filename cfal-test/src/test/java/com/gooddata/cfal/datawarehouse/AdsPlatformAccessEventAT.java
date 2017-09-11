/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.datawarehouse;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import org.testng.annotations.Test;

import java.util.function.Predicate;

/**
 * Acceptance test for ADS platform access event
 */
public class AdsPlatformAccessEventAT extends AbstractAdsAT {

    private static final String TEST_QUERY = "SELECT 1";
    private static final String MESSAGE_TYPE_ACCESS = "DATA_ACCESS";

    @Test(groups = MESSAGE_TYPE_ACCESS)
    public void testSelectUserApi() {
        getJdbcTemplate().execute(TEST_QUERY);
        doTestUserApi(eventCheck(MESSAGE_TYPE_ACCESS), MESSAGE_TYPE_ACCESS);
    }

    @Test(groups = MESSAGE_TYPE_ACCESS)
    public void testSelectAdminApi() {
        getJdbcTemplate().execute(TEST_QUERY);
        doTestAdminApi(eventCheck(MESSAGE_TYPE_ACCESS), MESSAGE_TYPE_ACCESS);
    }

    @Override
    protected Predicate<AuditEventDTO> eventCheck(final String eventType) {
        return (e -> e.getUserLogin().equals(getAccount().getLogin()) &&
                e.getType().equals(eventType) &&
                e.getParams() != null &&
                "datawarehouse".equals(e.getParams().get("type")) &&
                e.getLinks() != null &&
                getWarehouse().getUri().equals(e.getLinks().get("datawarehouse")));
    }

}
