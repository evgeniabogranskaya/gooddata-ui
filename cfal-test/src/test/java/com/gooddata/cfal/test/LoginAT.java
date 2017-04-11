/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Predicate;

public class LoginAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "STANDARD_LOGIN";

    @Before
    public void setUp() {
        gd.getAccountService().logout();

        gd.getAccountService().getCurrent(); // do log
    }

    @Test
    public void testLoginMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate());
    }

    @Test
    public void testLoginMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate());
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate() {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE));
    }

}
