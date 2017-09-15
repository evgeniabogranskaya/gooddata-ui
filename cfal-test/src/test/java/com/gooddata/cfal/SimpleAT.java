/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.gooddata.account.Account;
import com.gooddata.auditevent.AuditEvent;
import com.gooddata.auditevent.AuditEventsForbiddenException;
import com.gooddata.collections.PageableList;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.fail;

public class SimpleAT extends AbstractAT {

    @Test
    public void shouldReachDomainResource() throws Exception {
        final PageableList<AuditEvent> events = service.listAuditEvents(props.getDomain());
        assertThat(events, is(notNullValue()));
    }

    @Test
    public void shouldReturnErrorOnInvalidDomain() throws Exception {
        try {
            service.listAuditEvents("this_domain_should_never_exists");
            fail("Expected AuditEventsForbiddenException");
        } catch (AuditEventsForbiddenException ignored) {
        }
    }

    @Test
    public void shouldReachUserResource() throws Exception {
        final Account account = gd.getAccountService().getCurrent();

        final PageableList<AuditEvent> events = service.listAuditEvents(account);
        assertThat(events, is(notNullValue()));
    }
}
