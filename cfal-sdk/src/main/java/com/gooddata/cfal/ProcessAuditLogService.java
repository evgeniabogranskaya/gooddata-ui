/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import java.io.IOException;

/**
 * Single threaded blocking per-process file audit log writing service. Suitable for old GCF workers.
 */
public class ProcessAuditLogService extends SimpleAuditLogService {

    public ProcessAuditLogService(final String component, final AuditLogEventWriter auditLogEventWriter) throws IOException {
        super(component, auditLogEventWriter);
    }
}
