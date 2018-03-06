/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.gooddata.CfalGoodData;
import com.gooddata.account.Account;
import com.gooddata.auditlog.*;
import com.gooddata.auditevent.AuditEventService;
import com.gooddata.auditevent.AuditEvent;
import com.gooddata.auditevent.AuditEventPageRequest;
import com.gooddata.collections.PageableList;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.testng.Assert.fail;

public abstract class AbstractAT {

    protected static final String SSH_GROUP = "ssh";
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final int POLL_LIMIT = 10;
    protected static final int POLL_INTERVAL_SECONDS = 30;

    private static final int DEFAULT_TIMES = 1;

    protected final CfalGoodData gd;

    protected final AuditEventService service;

    protected final TestEnvironmentProperties props;

    protected final DateTime startTime;

    protected final AdsHelper adsHelper;
    protected final AccountHelper accountHelper;
    protected final ProjectHelper projectHelper;
    protected final MetadataHelper metadataHelper;
    protected final LoginHelper loginHelper;
    protected final ProcessHelper processHelper;
    protected final WebDavHelper webDavHelper;
    protected final CsvUploadHelper csvUploadHelper;
    protected final SftpHelper sftpHelper;
    protected final ScheduledMailHelper scheduledMailHelper;

    public AbstractAT() {
        props = TestEnvironmentProperties.getInstance();

        gd = CfalGoodData.getInstance();

        service = gd.getAuditEventService();

        startTime = new DateTime();

        adsHelper = AdsHelper.getInstance();
        accountHelper = AccountHelper.getInstance();
        projectHelper = ProjectHelper.getInstance();
        metadataHelper = MetadataHelper.getInstance();
        loginHelper = LoginHelper.getInstance();
        processHelper = ProcessHelper.getInstance();
        webDavHelper = WebDavHelper.getInstance();
        csvUploadHelper = CsvUploadHelper.getInstance();
        sftpHelper = SftpHelper.getInstance();
        scheduledMailHelper = ScheduledMailHelper.getInstance();
    }

    @BeforeSuite(alwaysRun = true)
    public void logConnectionInfo() throws Exception {
        logger.info("host={} user={} domain={}", props.getHost(), props.getUser(), props.getDomain());
    }

    /**
     * When tests are aborted without giving them a chance to run serviceTearDown, objects necessary for tests are not deleted.
     * Therefore delete these objects at next tests run.
     */
    @BeforeSuite(alwaysRun = true)
    public void preDestroy(){
        projectHelper.preDestroy();
        adsHelper.preDestroy();
    }

    @AfterSuite(alwaysRun = true)
    public void serviceTearDown() throws Exception {
        logger.info("clearing unnecessary thrash after tests...");
        //delete processes and schedules first
        processHelper.destroy();
        scheduledMailHelper.destroy();
        //delete all unnecessary metadata (e.g. project dashboards)
        metadataHelper.destroy();
        //delete projects before ADS instances (project links to existing ADS)
        projectHelper.destroy();
        //delete ADS instances before created accounts
        adsHelper.destroy();
        //delete accounts at last
        accountHelper.destroy();
    }

    /**
     * Tests whether message is contained in audit log via user API
     *
     * @param predicate predicate used to check whether list of audit events contains required message
     * @param type      type of the event you want to check on API
     */
    public void doTestUserApi(final Predicate<AuditEvent> predicate, final String type) {
        doTestUserApi(predicate, type, DEFAULT_TIMES);
    }

    /**
     * Tests whether message is contained in audit log via admin API
     *
     * @param predicate predicate used to check whether list of audit events contains required message
     * @param type      type of the event you want to check on API
     */
    public void doTestAdminApi(final Predicate<AuditEvent> predicate, final String type) {
        doTestAdminApi(predicate, type, DEFAULT_TIMES);
    }


    /**
     * Tests whether certain message is contained multiple times in audit log via user API
     *
     * @param predicate predicate used to check whether list of audit events contains required message
     * @param type      type of the event you want to check on API
     * @param times     how many events should at least match predicate
     */
    public void doTestUserApi(final Predicate<AuditEvent> predicate, final String type, final int times) {
        final AuditEventPageRequest request = createRequestParameters(type);

        doTest(() -> service.listAuditEvents(getAccount(), request), predicate, type, times);
    }

    /**
     * Tests whether certain message is contained multiple times in audit log via admin API
     *
     * @param predicate predicate used to check whether list of audit events contains required message
     * @param type      type of the event you want to check on API
     * @param times     how many events should at least match predicate
     */
    public void doTestAdminApi(final Predicate<AuditEvent> predicate, final String type, final int times) {
        final AuditEventPageRequest request = createRequestParameters(type);

        doTest(() -> service.listAuditEvents(props.getDomain(), request), predicate, type, times);
    }

    private void doTest(final Supplier<PageableList<AuditEvent>> serviceCall,
                        final Predicate<AuditEvent> predicate,
                        final String type,
                        final int times) {
        final String testMethodName = getTestMethodName();

        //poll until message is found in audit log or poll limit is hit
        int count = 1;
        while (count++ <= POLL_LIMIT) {
            if (hasMessage(serviceCall, predicate, times)) {
                logger.info("{}(): message {} found", testMethodName, type);
                return;
            }
            logger.info("{}(): message {} not found, waiting {} seconds", testMethodName, type, POLL_INTERVAL_SECONDS);
            try {
                TimeUnit.SECONDS.sleep(POLL_INTERVAL_SECONDS);
            } catch (InterruptedException e) {
                fail("Interrupted while waiting for message " + type, e);
            }
        }

        logger.error("{}(): message {} not found", testMethodName, type);
        fail("message " + type + " not found");
    }

    protected String getTestMethodName() {
        return Arrays.stream(Thread.currentThread().getStackTrace())
                    .filter(e -> Objects.equals(e.getClassName(), getClass().getName()))
                    .map(StackTraceElement::getMethodName)
                    .findFirst()
                    .orElse("unknown");
    }

    private boolean hasMessage(final Supplier<PageableList<AuditEvent>> serviceCall, final Predicate<AuditEvent> predicate, final int times) {
        final PageableList<AuditEvent> events = serviceCall.get();
        final long countOfEvents = events.stream().filter(predicate).count();
        logger.info("found {} matching events", countOfEvents);
        return countOfEvents >= times;
    }

    private AuditEventPageRequest createRequestParameters(final String type) {
        final AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setFrom(startTime);
        requestParameters.setType(type);
        return requestParameters;
    }

    public Account getAccount() {
        return accountHelper.getCurrentAccount();
    }
}
