/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.datawarehouse;

import com.gooddata.warehouse.WarehouseUser;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test ADS that it logs event about adding the user to ADS correctly
 */
public class AdsAddUserEventsAT extends AbstractAdsAT {
    private static final String MESSAGE_TYPE = "DATAWAREHOUSE_ADD_USER";

    private WarehouseUser warehouseUser;

    @BeforeClass
    public void addUserToAds() throws Exception {
        warehouseUser = new WarehouseUser(WarehouseUser.DATA_ADMIN_ROLE, null, anotherAccount.getLogin());
        gd.getWarehouseService().addUserToWarehouse(getWarehouse(), warehouseUser);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testAddUserEventUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(MESSAGE_TYPE), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testAddUserEventAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(MESSAGE_TYPE), MESSAGE_TYPE);
    }
}
