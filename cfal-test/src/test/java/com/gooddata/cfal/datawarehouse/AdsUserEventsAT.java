/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.datawarehouse;

import com.gooddata.warehouse.WarehouseUser;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.warehouse.WarehouseUserRole.DATA_ADMIN;

/**
 * Test ADS that it logs event about adding the user to ADS correctly
 */
public class AdsUserEventsAT extends AbstractAdsAT {
    private static final String MESSAGE_TYPE_ADD = "DATAWAREHOUSE_ADD_USER";
    private static final String MESSAGE_TYPE_REMOVE = "DATAWAREHOUSE_REMOVE_USER";

    private WarehouseUser warehouseUser;

    @BeforeClass
    public void addUserToAds() throws Exception {
        warehouseUser = WarehouseUser.createWithlogin(anotherAccount.getLogin(),DATA_ADMIN);
        gd.getWarehouseService().addUserToWarehouse(getWarehouse(), warehouseUser);
        gd.getWarehouseService().removeUserFromWarehouse(warehouseUser);
    }

    @Test(groups = MESSAGE_TYPE_ADD)
    public void testAddUserEventUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(MESSAGE_TYPE_ADD), MESSAGE_TYPE_ADD);
    }

    @Test(groups = MESSAGE_TYPE_ADD)
    public void testAddUserEventAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(MESSAGE_TYPE_ADD), MESSAGE_TYPE_ADD);
    }

    @Test(groups = MESSAGE_TYPE_REMOVE)
    public void testRemoveUserEventUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(MESSAGE_TYPE_REMOVE), MESSAGE_TYPE_REMOVE);
    }

    @Test(groups = MESSAGE_TYPE_REMOVE)
    public void testRemoveUserEventAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(MESSAGE_TYPE_REMOVE), MESSAGE_TYPE_REMOVE);
    }
}
