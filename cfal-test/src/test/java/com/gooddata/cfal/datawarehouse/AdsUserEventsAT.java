/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.datawarehouse;

import com.gooddata.account.Account;
import com.gooddata.warehouse.WarehouseUser;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.warehouse.WarehouseUserRole.DATA_ADMIN;

/**
 * Test ADS that it logs event about adding the user to ADS correctly
 */
public class AdsUserEventsAT extends AbstractAdsAT {
    private static final String MESSAGE_TYPE_ADD = "DATAWAREHOUSE_USER_ADD";
    private static final String MESSAGE_TYPE_REMOVE = "DATAWAREHOUSE_USER_REMOVE";

    @BeforeClass(groups = {MESSAGE_TYPE_ADD, MESSAGE_TYPE_REMOVE})
    public void addUserToAds() throws Exception {
        final Account anotherAccount = accountHelper.getOrCreateUser();

        final WarehouseUser tmpUser = WarehouseUser.createWithlogin(anotherAccount.getLogin(), DATA_ADMIN);

        final WarehouseUser warehouseUser = gd.getWarehouseService().addUserToWarehouse(getWarehouse(), tmpUser).get();
        gd.getWarehouseService().removeUserFromWarehouse(warehouseUser).get();
    }

    @Test(groups = MESSAGE_TYPE_ADD)
    public void testAddUserEventUserApi() {
        doTestUserApi(eventCheck(MESSAGE_TYPE_ADD), MESSAGE_TYPE_ADD);
    }

    @Test(groups = MESSAGE_TYPE_ADD)
    public void testAddUserEventAdminApi() {
        doTestAdminApi(eventCheck(MESSAGE_TYPE_ADD), MESSAGE_TYPE_ADD);
    }

    @Test(groups = MESSAGE_TYPE_REMOVE)
    public void testRemoveUserEventUserApi() {
        doTestUserApi(eventCheck(MESSAGE_TYPE_REMOVE), MESSAGE_TYPE_REMOVE);
    }

    @Test(groups = MESSAGE_TYPE_REMOVE)
    public void testRemoveUserEventAdminApi() {
        doTestAdminApi(eventCheck(MESSAGE_TYPE_REMOVE), MESSAGE_TYPE_REMOVE);
    }
}
