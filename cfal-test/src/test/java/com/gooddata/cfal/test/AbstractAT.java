/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import static java.lang.System.getProperty;
import static org.junit.Assert.fail;

import com.gooddata.CfalGoodData;
import com.gooddata.account.Account;
import com.gooddata.auditlog.AuditLogService;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.collections.Page;
import com.gooddata.collections.PageableList;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractAT {

    private static Logger logger = LoggerFactory.getLogger(AbstractAT.class);

    private static final int POLL_LIMIT = 10;
    private static final int POLL_INTERVAL_SECONDS = 30;

    protected final CfalGoodData gd;
    protected final AuditLogService service;

    protected final String host;
    protected final String user;
    protected final String pass;
    protected final String domain;

    protected final Account account;

    private final DateTime startTime;

    public AbstractAT() {
        host = getProperty("host", "localhost");
        user = getProperty("user", "bear@gooddata.com");
        pass = getProperty("pass", "jindrisska");
        domain = getProperty("domain", "default");

        gd = new CfalGoodData(host, user, pass);
        service = gd.getAuditLogService();

        account = gd.getAccountService().getCurrent();
        startTime = new DateTime();
    }

    /**
     * Tests whether message is contained in audit log via user API
     *
     * @param pageCheckPredicate predicate used to checker whether list of audit events (page) contains required message
     * @throws InterruptedException
     */
    public void doTestUserApi(Predicate<List<AuditEventDTO>> pageCheckPredicate) throws InterruptedException {
        doTest((Page page) -> service.listAuditEvents(account, page), pageCheckPredicate);

    }

    /**
     * Tests whether message is contained in audit log via admin API
     *
     * @param pageCheckPredicate predicate used to checker whether list of audit events (page) contains required message
     * @throws InterruptedException
     */
    public void doTestAdminApi(Predicate<List<AuditEventDTO>> pageCheckPredicate) throws InterruptedException {
        doTest((Page page) -> service.listAuditEvents(domain, page), pageCheckPredicate);
    }


    private void doTest(final Function<Page, PageableList<AuditEventDTO>> serviceCall, Predicate<List<AuditEventDTO>> pageCheckPredicate) throws InterruptedException {
        //poll until message is found in audit log or poll limit is hit
        int count = 1;
        while (!hasMessage(serviceCall, pageCheckPredicate) && count <= POLL_LIMIT) {
            logger.info("message not found, waiting {} seconds", POLL_INTERVAL_SECONDS);
            TimeUnit.SECONDS.sleep(POLL_INTERVAL_SECONDS);
            count++;
        }

        if (!hasMessage(serviceCall, pageCheckPredicate)) {
            fail("message not found");
        }
    }

    /**
     * gets pages from audit log (using serviceCall) and check whether it contains event (using pageCheckPredicate)
     */
    private boolean hasMessage(final Function<Page, PageableList<AuditEventDTO>> serviceCall, Predicate<List<AuditEventDTO>> pageCheckPredicate) {
        final RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(startTime);

        PageableList<AuditEventDTO> events = serviceCall.apply(requestParameters);
        if (pageCheckPredicate.test(events)) {
            return true;
        }
        // check whether there are next pages and check whether they contain event
        while (events.hasNextPage()) {
            events = serviceCall.apply(events.getNextPage());
            if (pageCheckPredicate.test(events)) {
                return true;
            }
        }
        return false;
    }
}
