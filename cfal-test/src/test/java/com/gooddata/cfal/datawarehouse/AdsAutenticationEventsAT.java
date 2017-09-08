/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.datawarehouse;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.function.Predicate;

/**
 * Acceptance test for ADS login and logout
 */
public class AdsAutenticationEventsAT extends AbstractAdsAT {

    private static final String COMPONENT = "component";
    private static final String DATAWAREHOUSE = "DATAWAREHOUSE";
    private static final String USER_NAME_PASSWORD = "USERNAME_PASSWORD";
    private static final String LOGIN_TYPE = "loginType";

    private static final String TEST_QUERY = "SELECT 1";
    private static final String MESSAGE_TYPE_LOGIN = "LOGIN";
    private static final String MESSAGE_TYPE_CONNECTION = "CONNECTION";
    private static final String MESSAGE_TYPE_LOGOUT = "DATAWAREHOUSE_LOGOUT";

    @BeforeClass(groups = {MESSAGE_TYPE_LOGIN, MESSAGE_TYPE_LOGOUT, MESSAGE_TYPE_CONNECTION})
    public void setUp() {
        getJdbcTemplate().execute(TEST_QUERY);
    }

    @Test(groups = MESSAGE_TYPE_LOGIN)
    public void testUsernamePasswordLoginUserApi() {
        doTestUserApi(eventCheck(), MESSAGE_TYPE_LOGIN);
    }

    @Test(groups = MESSAGE_TYPE_LOGIN)
    public void testUsernamePasswordLoginAdminApi() {
        doTestAdminApi(eventCheck(), MESSAGE_TYPE_LOGIN);
    }

    @Test(groups = MESSAGE_TYPE_CONNECTION)
    public void testADSConnectionUserApi() {
        doTestUserApi(eventCheck(MESSAGE_TYPE_CONNECTION), MESSAGE_TYPE_CONNECTION);
    }

    @Test(groups = MESSAGE_TYPE_CONNECTION)
    public void testADSConnectionAdminApi() {
        doTestAdminApi(eventCheck(MESSAGE_TYPE_CONNECTION), MESSAGE_TYPE_CONNECTION);
    }

    @Test(groups = MESSAGE_TYPE_LOGOUT, enabled = false)
    public void testDatawarehouseLogoutUserApi() throws Exception {
        doTestUserApi(eventCheck(MESSAGE_TYPE_LOGOUT), MESSAGE_TYPE_LOGOUT);
    }

    @Test(groups = MESSAGE_TYPE_LOGOUT, enabled = false)
    public void testDatawarehouseLogoutAdminApi() throws Exception {
        doTestAdminApi(eventCheck(MESSAGE_TYPE_LOGOUT), MESSAGE_TYPE_LOGOUT);
    }

    protected Predicate<AuditEventDTO> eventCheck() {
        return (e ->
                e.getUserLogin().equals(getAccount().getLogin()) &&
                        e.getType().equals(MESSAGE_TYPE_LOGIN) &&
                        USER_NAME_PASSWORD.equals(e.getParams().get(LOGIN_TYPE)) &&
                        DATAWAREHOUSE.equals(e.getParams().get(COMPONENT)));
    }
}
