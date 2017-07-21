/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import com.gooddata.cfal.AbstractAT;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Predicate;

public class LogoutAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "STANDARD_LOGOUT";

    @BeforeClass(groups = MESSAGE_TYPE)
    public void setUp() {
        gd.getAccountService().logout();
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLogoutMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLogoutMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(), MESSAGE_TYPE);
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate() {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE));
    }

}
