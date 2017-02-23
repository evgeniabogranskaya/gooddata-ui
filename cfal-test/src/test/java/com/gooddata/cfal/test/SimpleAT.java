/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import com.gooddata.CfalGoodData;
import com.gooddata.account.Account;
import com.gooddata.auditlog.AuditLogService;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.collections.PageableList;
import org.junit.Test;

import static java.lang.System.getProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class SimpleAT {

    private final CfalGoodData gd;
    private final AuditLogService service;

    public SimpleAT() {
        gd = new CfalGoodData(
                getProperty("host", "localhost"),
                getProperty("user", "bear@gooddata.com"),
                getProperty("pass", "jindrisska")
        );
        service = gd.getAuditLogService();
    }

    @Test
    public void shouldReachDomainResource() throws Exception {
        final PageableList<AuditEventDTO> events = service.listAuditEvents("default");
        assertThat(events, is(notNullValue()));
    }

    @Test
    public void shouldReachUserResource() throws Exception {
        final Account account = gd.getAccountService().getCurrent();

        final PageableList<AuditEventDTO> events = service.listAuditEvents(account);
        assertThat(events, is(notNullValue()));
    }
}
