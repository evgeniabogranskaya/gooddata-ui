/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import com.gooddata.cfal.AbstractAT;
import com.gooddata.auditevent.AuditEvent;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.function.Predicate;

public class LogoutAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "STANDARD_LOGOUT";

    @BeforeClass(groups = MESSAGE_TYPE)
    public void setUp() {
        gd.getAccountService().logout();
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLogoutMessageUserApi() {
        doTestUserApi(eventCheck(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLogoutMessageAdminApi() {
        doTestAdminApi(eventCheck(), MESSAGE_TYPE);
    }

    private Predicate<AuditEvent> eventCheck() {
        return (e -> e.getUserLogin().equals(getAccount().getLogin()) && e.getType().equals(MESSAGE_TYPE));
    }

}
