/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.invalid;

import com.gooddata.cfal.AbstractMongoAT;
import com.gooddata.cfal.AuditLogEvent;
import com.gooddata.cfal.ExtensibleAuditLogEvent;
import com.gooddata.cfal.RemoteAuditLogFile;
import org.apache.commons.io.FileUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.UUID;

public class InvalidRecordsAT extends AbstractMongoAT {

    private static final String LOGIN = "bear@gooddata.com";
    private static final String IP = "127.0.0.1";
    private static final String DOMAIN = "test";
    protected static final String INVALID = "invalid";

    private RemoteAuditLogFile auditLog;
    private String uniqueType;
    private Query query;

    @BeforeMethod(groups = {SSH_GROUP, INVALID})
    public void setUp() throws Exception {
        auditLog = new RemoteAuditLogFile(ssh);
        uniqueType = UUID.randomUUID().toString(); // give each event an unique type to be able to search it easily
        query = new Query()
                .addCriteria(Criteria.where("type").is(uniqueType));
    }

    @Test(groups = {SSH_GROUP, INVALID})
    public void shouldAddDomainWithDollarToInvalidCollection() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(uniqueType, LOGIN, IP, "$domain");
        auditLog.appendEvent(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    @Test(groups = {SSH_GROUP, INVALID})
    public void shouldAddNoDomainToInvalidCollection() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(uniqueType, LOGIN, IP, null);
        auditLog.appendEvent(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    @Test(groups = {SSH_GROUP, INVALID})
    public void shouldAddNoUserIpToInvalidCollection() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(uniqueType, LOGIN, null, DOMAIN);
        auditLog.appendEvent(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    @Test(groups = {SSH_GROUP, INVALID})
    public void shouldAddNoUserLoginToInvalidCollection() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(uniqueType, null, IP, DOMAIN);
        auditLog.appendEvent(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    @Test(groups = {SSH_GROUP, INVALID})
    public void shouldReplaceDollarAndDotInKeyName() throws Exception {
        final AuditLogEvent event = new ExtensibleAuditLogEvent(uniqueType, LOGIN, IP, DOMAIN)
                .withProperty("$fo.o", "bar");
        auditLog.appendEvent(event);

        query.addCriteria(Criteria.where("__dollar__fo__dot__o").is("bar"));

        assertQuery(query, "cfal_test");
    }

    @Test(groups = {SSH_GROUP, INVALID})
    public void shouldAddBadOccurredDateToInvalidCollection() throws Exception {
        final String event = readEventFromResource("auditEventWithBadOccurred.json");

        auditLog.appendString(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    @Test(groups = {SSH_GROUP, INVALID})
    public void shouldAddBadSuccessToInvalidCollection() throws Exception {
        final String event = readEventFromResource("auditEventWithBadSuccess.json");

        auditLog.appendString(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    @Test(groups = {SSH_GROUP, INVALID})
    public void shouldAddBadParamsToInvalidCollection() throws Exception {
        final String event = readEventFromResource("auditEventWithBadParams.json");

        auditLog.appendString(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    @Test(groups = {SSH_GROUP, INVALID})
    public void shouldAddBadLinksToInvalidCollection() throws Exception {
        final String event = readEventFromResource("auditEventWithBadLinks.json");

        auditLog.appendString(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    /**
     * read audit event from resource file and removes new lines and replaces $REPLACE_TYPE with <code>uniqueType</code>
     * @param resourceName to read audit event from
     * @return
     * @throws Exception
     */
    private String readEventFromResource(final String resourceName) throws Exception {
        final File file = new File(getClass().getClassLoader().getResource(resourceName).toURI());

        final String auditEventString = FileUtils.readFileToString(file);

        final String auditEventWithReplacedType = auditEventString.replace("$REPLACE_TYPE", uniqueType);
        final String auditEventWithReplacedNewLines = auditEventWithReplacedType.replaceAll("\n","");
        return auditEventWithReplacedNewLines + "\n";
    }

}
