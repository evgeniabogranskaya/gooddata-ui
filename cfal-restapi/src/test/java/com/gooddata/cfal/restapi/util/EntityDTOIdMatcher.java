/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.model.AuditEvent;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Matcher between AuditEvent entity and AuditEvent DTO based on their IDs
 */
public class EntityDTOIdMatcher extends TypeSafeMatcher<AuditEventDTO> {

    private final AuditEvent auditEvent;

    public EntityDTOIdMatcher(final AuditEvent auditEvent) {
        this.auditEvent = notNull(auditEvent);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Audit event id " + auditEvent.getId());
    }

    public static EntityDTOIdMatcher hasSameIdAs(final AuditEvent auditEvent) {
        return new EntityDTOIdMatcher(auditEvent);
    }

    @Override
    protected boolean matchesSafely(AuditEventDTO item) {
        return auditEvent.getId().toString().equals(item.getId());
    }
}
