/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

/**
 * Callback called when {@link ConcurrentAuditLogService} is not able to process an event
 */
interface RejectionHandler {

    /**
     * Handle event which couldn't be processed by Audit Log Service
     * @param event event
     */
    void handle(AuditLogEvent event);
}
