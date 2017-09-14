/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import com.gooddata.auditevent.AuditEvent;
import com.gooddata.cfal.restapi.model.AuditEventEntity;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Matcher between AuditEvent entity and AuditEvent DTO based on their IDs
 */
public class EntityDTOIdMatcher extends TypeSafeMatcher<AuditEvent> {

    private final AuditEventEntity auditEvent;

    public EntityDTOIdMatcher(final AuditEventEntity auditEvent) {
        this.auditEvent = notNull(auditEvent);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Audit event id " + auditEvent.getId());
    }

    public static EntityDTOIdMatcher hasSameIdAs(final AuditEventEntity auditEvent) {
        return new EntityDTOIdMatcher(auditEvent);
    }

    @Override
    protected boolean matchesSafely(AuditEvent item) {
        return auditEvent.getId().toString().equals(item.getId());
    }
}
