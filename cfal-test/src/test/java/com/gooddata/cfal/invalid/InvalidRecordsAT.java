/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.invalid;

import com.gooddata.cfal.AbstractMongoAT;
import com.gooddata.cfal.AuditLogEvent;
import com.gooddata.cfal.ExtensibleAuditLogEvent;
import com.gooddata.cfal.RemoteAuditLogFile;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.UUID;

public class InvalidRecordsAT extends AbstractMongoAT {

    private static final String INVALID_COLLECTION = "cfalinvalid";

    private static final String LOGIN = "bear@gooddata.com";
    private static final String IP = "127.0.0.1";
    private static final String DOMAIN = "test";

    private RemoteAuditLogFile auditLog;
    private String uniqueType;
    private Query query;

    @BeforeMethod(groups = {"ssh", "invalid"})
    public void setUp() throws Exception {
        auditLog = new RemoteAuditLogFile(ssh);
        uniqueType = UUID.randomUUID().toString(); // give each event an unique type to be able to search it easily
        query = new Query()
                .addCriteria(Criteria.where("type").is(uniqueType));
    }

    @Test(groups = {"ssh", "invalid"})
    public void shouldAddDomainWithDollarToInvalidCollection() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(uniqueType, LOGIN, IP, "$domain");
        auditLog.appendEvent(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    @Test(groups = {"ssh", "invalid"})
    public void shouldAddNoDomainToInvalidCollection() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(uniqueType, LOGIN, IP, null);
        auditLog.appendEvent(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    @Test(groups = {"ssh", "invalid"})
    public void shouldAddNoUserIpToInvalidCollection() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(uniqueType, LOGIN, null, DOMAIN);
        auditLog.appendEvent(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    @Test(groups = {"ssh", "invalid"})
    public void shouldAddNoUserLoginToInvalidCollection() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(uniqueType, null, IP, DOMAIN);
        auditLog.appendEvent(event);

        assertQuery(query, INVALID_COLLECTION);
    }

    @Test(groups = {"ssh", "invalid"})
    public void shouldReplaceDollarAndDotInKeyName() throws Exception {
        final AuditLogEvent event = new ExtensibleAuditLogEvent(uniqueType, LOGIN, IP, DOMAIN)
                .withProperty("$fo.o", "bar");
        auditLog.appendEvent(event);

        query.addCriteria(Criteria.where("__dollar__fo__dot__o").is("bar"));

        assertQuery(query, "cfal_test");
    }

}
