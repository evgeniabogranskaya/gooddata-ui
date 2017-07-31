/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import javax.annotation.PreDestroy;
import java.io.IOException;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Single threaded blocking audit log writing service. Suitable for HTTP GCF workers.
 */
public class SimpleAuditLogService extends AbstractAuditLogService {

    protected final AuditLogEventWriter writer;

    public SimpleAuditLogService(final String component) throws IOException {
        this(component, new AuditLogEventFileWriter(component));
    }

    protected SimpleAuditLogService(final String component, final AuditLogEventWriter writer) throws IOException {
        super(component);
        this.writer = notNull(writer, "writer");
    }

    @Override
    protected void doLogEvent(final AuditLogEvent event) {
        writer.logEvent(event);
    }

    @Override
    public long getErrorCount() {
        return writer.getErrorCounter();
    }

    @PreDestroy
    public void destroy() throws Exception {
        writer.close();
    }
}
