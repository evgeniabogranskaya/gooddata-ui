/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * Single threaded blocking per-process file audit log writing service. Suitable for old GCF workers.
 */
public class ProcessAuditLogService extends SimpleAuditLogService {

    public ProcessAuditLogService(final String component) throws IOException {
        super(component, new AuditLogEventFileWriter(component, getCurrentPid()));
    }

    private static String getCurrentPid() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

}
