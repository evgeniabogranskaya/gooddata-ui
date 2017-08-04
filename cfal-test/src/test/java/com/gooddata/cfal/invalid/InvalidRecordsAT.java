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

    private RemoteAuditLogFile auditLog;
    private String uniqueType;

    @BeforeMethod(groups = {"ssh", "invalid"})
    public void setUp() throws Exception {
        auditLog = new RemoteAuditLogFile(ssh);
        uniqueType = UUID.randomUUID().toString(); // give each event an unique type to be able to search it easily
    }

    @Test(groups = {"ssh", "invalid"})
    public void shouldAddDomainWithDollarToInvalidCollection() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(uniqueType, "bear@gooddata.com", "127.0.0.1", "$domain");
        auditLog.appendEvent(event);

        final Query query = new Query()
                .addCriteria(Criteria.where("type").is(uniqueType));

        assertQuery(query, "cfalinvalid");
    }

    @Test(groups = {"ssh", "invalid"})
    public void shouldAddNoDomainToInvalidCollection() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(uniqueType, "bear@gooddata.com", "127.0.0.1", null);
        auditLog.appendEvent(event);

        final Query query = new Query()
                .addCriteria(Criteria.where("type").is(uniqueType));

        assertQuery(query, "cfalinvalid");
    }

    @Test(groups = {"ssh", "invalid"})
    public void shouldReplaceDollarAndDotInKeyName() throws Exception {
        final AuditLogEvent event = new ExtensibleAuditLogEvent(uniqueType, "bear@gooddata.com", "127.0.0.1", "test")
                .withProperty("$fo.o", "bar");
        auditLog.appendEvent(event);

        final Query query = new Query()
                .addCriteria(Criteria.where("type").is(uniqueType))
                .addCriteria(Criteria.where("__dollar__fo__dot__o").is("bar"))
                ;

        assertQuery(query, "cfal_test");
    }

}
