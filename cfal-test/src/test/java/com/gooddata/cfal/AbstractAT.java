/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.gooddata.CfalGoodData;
import com.gooddata.GoodDataEndpoint;
import com.gooddata.account.Account;
import com.gooddata.auditlog.AccountService;
import com.gooddata.auditlog.AdsService;
import com.gooddata.auditlog.AuditLogService;
import com.gooddata.auditlog.TestEnvironmentProperties;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.collections.Page;
import com.gooddata.collections.PageableList;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.testng.Assert.fail;

public abstract class AbstractAT {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int POLL_LIMIT = 10;
    private static final int POLL_INTERVAL_SECONDS = 30;

    protected final CfalGoodData gd;
    protected final GoodDataEndpoint endpoint;

    protected final AuditLogService service;

    protected final TestEnvironmentProperties props;

    protected final Account account;

    private final DateTime startTime;

    protected final AdsService adsService;
    protected final AccountService accountService;

    public AbstractAT() {
        props = new TestEnvironmentProperties();

        endpoint = new GoodDataEndpoint(props.getHost());

        gd = new CfalGoodData(endpoint, props.getUser(), props.getPass());
        service = gd.getAuditLogService();

        account = gd.getAccountService().getCurrent();
        startTime = new DateTime();

        this.adsService = AdsService.getInstance(gd, props);
        this.accountService = AccountService.getInstance(gd, props);
    }

    @BeforeSuite(alwaysRun = true)
    public void logConnectionInfo() throws Exception {
        logger.info("host={} user={} domain={}", props.getHost(), props.getUser(), props.getDomain());
    }

    @AfterSuite(alwaysRun = true)
    public void serviceTearDown() {
        accountService.destroy();
        adsService.destroy();
    }

    /**
     * Tests whether message is contained in audit log via user API
     *
     * @param pageCheckPredicate predicate used to checker whether list of audit events (page) contains required message
     * @param type               type of the even you want to check on API
     * @throws InterruptedException
     */
    public void doTestUserApi(Predicate<List<AuditEventDTO>> pageCheckPredicate, String type) throws InterruptedException {
        doTest((Page page) -> service.listAuditEvents(account, page), pageCheckPredicate, type);

    }

    /**
     * Tests whether message is contained in audit log via admin API
     *
     * @param pageCheckPredicate predicate used to checker whether list of audit events (page) contains required message
     * @param type               type of the even you want to check on API
     * @throws InterruptedException
     */
    public void doTestAdminApi(Predicate<List<AuditEventDTO>> pageCheckPredicate, String type) throws InterruptedException {
        doTest((Page page) -> service.listAuditEvents(props.getDomain(), page), pageCheckPredicate, type);
    }

    private void doTest(final Function<Page, PageableList<AuditEventDTO>> serviceCall,
                        final Predicate<List<AuditEventDTO>> pageCheckPredicate,
                        final String type) throws InterruptedException {
        final String testMethodName = Arrays.stream(Thread.currentThread().getStackTrace())
                .filter(e -> Objects.equals(e.getClassName(), getClass().getName()))
                .map(StackTraceElement::getMethodName)
                .findFirst()
                .orElse("unknown");

        //poll until message is found in audit log or poll limit is hit
        int count = 1;
        while (count++ <= POLL_LIMIT) {
            if (hasMessage(serviceCall, pageCheckPredicate, type)) {
                logger.info("{}(): message {} found", testMethodName, type);
                return;
            }
            logger.info("{}(): message {} not found, waiting {} seconds", testMethodName, type, POLL_INTERVAL_SECONDS);
            TimeUnit.SECONDS.sleep(POLL_INTERVAL_SECONDS);
        }

        fail("message not found");
    }

    /**
     * gets pages from audit log (using serviceCall) and check whether it contains event (using pageCheckPredicate)
     */
    private boolean hasMessage(final Function<Page, PageableList<AuditEventDTO>> serviceCall,
                               final Predicate<List<AuditEventDTO>> pageCheckPredicate,
                               final String type) {
        final RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(startTime);
        requestParameters.setType(type);

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
