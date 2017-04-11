/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import com.gooddata.account.Account;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.collections.PageableList;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class SimpleAT extends AbstractAT {

    @Test
    public void shouldReachDomainResource() throws Exception {
        final PageableList<AuditEventDTO> events = service.listAuditEvents(domain);
        assertThat(events, is(notNullValue()));
    }

    @Test
    public void shouldReachUserResource() throws Exception {
        final Account account = gd.getAccountService().getCurrent();

        final PageableList<AuditEventDTO> events = service.listAuditEvents(account);
        assertThat(events, is(notNullValue()));
    }
}
