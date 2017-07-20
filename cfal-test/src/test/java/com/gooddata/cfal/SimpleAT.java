/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.gooddata.GoodDataRestException;
import com.gooddata.account.Account;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.collections.PageableList;
import org.apache.http.HttpStatus;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class SimpleAT extends AbstractAT {

    @Test
    public void shouldReachDomainResource() throws Exception {
        final PageableList<AuditEventDTO> events = service.listAuditEvents(props.getDomain());
        assertThat(events, is(notNullValue()));
    }

    @Test
    public void shouldReturnErrorOnInvalidDomain() throws Exception {
        try {
            service.listAuditEvents("this_domain_should_never_exists");
        } catch (GoodDataRestException e) {
            assertThat(e.getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
            assertThat(e.getErrorCode(), is("gdc.auditlog.user.not_authorized"));
        }
    }

    @Test
    public void shouldReachUserResource() throws Exception {
        final Account account = gd.getAccountService().getCurrent();

        final PageableList<AuditEventDTO> events = service.listAuditEvents(account);
        assertThat(events, is(notNullValue()));
    }
}
