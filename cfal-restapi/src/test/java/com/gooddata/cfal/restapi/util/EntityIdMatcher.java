/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import static org.apache.commons.lang3.Validate.notNull;

import com.gooddata.cfal.restapi.model.AuditEventEntity;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher between AuditEvent entities based on ID
 */
public class EntityIdMatcher extends TypeSafeMatcher<AuditEventEntity> {

    private final AuditEventEntity auditEvent;

    public EntityIdMatcher(final AuditEventEntity auditEvent) {
        this.auditEvent = notNull(auditEvent);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Audit event id " + auditEvent.getId());
    }

    public static EntityIdMatcher hasSameIdAs(final AuditEventEntity auditEvent) {
        return new EntityIdMatcher(auditEvent);
    }

    @Override
    protected boolean matchesSafely(AuditEventEntity item) {
        return auditEvent.getId().equals(item.getId());
    }
}
