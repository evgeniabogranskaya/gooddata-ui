/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import static org.junit.Assert.fail;

import com.gooddata.account.Account;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.collections.Page;
import com.gooddata.collections.PageableList;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class LoginAT extends AbstractAT {

    private static Logger logger = LoggerFactory.getLogger(LoginAT.class);

    private static final int POLL_LIMIT = 10;
    private static final int POLL_INTERVAL_SECONDS = 30;
    private static final String MESSAGE_TYPE = "STANDARD_LOGIN";

    private DateTime startTime;
    private Account account;

    @Before
    public void setUp() {
        account = gd.getAccountService().getCurrent();
        startTime = new DateTime();

        gd.getAccountService().logout();
    }

    @Test
    public void testLoginMessageUserApi() throws InterruptedException {
        doTest((Page page) -> service.listAuditEvents(account, page));
    }

    @Test
    public void testLoginMessageAdminApi() throws InterruptedException {
        doTest((Page page) -> service.listAuditEvents(domain, page));
    }

    /**
     * Test whether login message is contained in audit log.
     * This method accepts function parameter which encapsulates calls of services used for testing.
     */
    private void doTest(final Function<Page, PageableList<AuditEventDTO>> serviceCall) throws InterruptedException {
        gd.getAccountService().getCurrent(); // do log

        //poll until login message is found in audit log or poll limit is hit
        int count = 1;
        while (!hasLoginMessage(serviceCall) && count <= POLL_LIMIT) {
            logger.info("login message not found, waiting {} seconds", POLL_INTERVAL_SECONDS);
            TimeUnit.SECONDS.sleep(POLL_INTERVAL_SECONDS);
            count++;
        }

        if (!hasLoginMessage(serviceCall)) {
            fail("login message not found");
        }
    }

    /**
     * gets pages from audit log and check whether it contains login event
     */
    private boolean hasLoginMessage(final Function<Page, PageableList<AuditEventDTO>> serviceCall) {
        final RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(startTime);

        PageableList<AuditEventDTO> events = serviceCall.apply(requestParameters);
        if (checkPage(events)) {
            return true;
        }
        // check whether there are next pages and check whether they contain login event
        while (events.hasNextPage()) {
            events = serviceCall.apply(events.getNextPage());
            if (checkPage(events)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPage(final PageableList<AuditEventDTO> auditEvents) {
        return auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE));
    }

}
