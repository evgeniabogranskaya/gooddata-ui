/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Audit Log Event with any number of other root-level properties
 */
public class ExtensibleAuditLogEvent extends AuditLogEvent {

    private final Map<String, String> properties = new LinkedHashMap<>();

    public ExtensibleAuditLogEvent(final String type, final String userLogin, final String userIp, final String domain) {
        super(type, userLogin, userIp, domain);
    }

    @JsonAnyGetter
    public Map<String, String> getProperties() {
        return properties;
    }

    public ExtensibleAuditLogEvent withProperty(final String key, final String value) {
        properties.put(key, value);
        return this;
    }
}
