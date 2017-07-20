/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import com.gooddata.cfal.AbstractAT;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Predicate;

public class LoginAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "STANDARD_LOGIN";

    @BeforeMethod
    public void setUp() {
        gd.getAccountService().logout();

        gd.getAccountService().getCurrent(); // do log
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLoginMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLoginMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(), MESSAGE_TYPE);
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate() {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE));
    }

}
