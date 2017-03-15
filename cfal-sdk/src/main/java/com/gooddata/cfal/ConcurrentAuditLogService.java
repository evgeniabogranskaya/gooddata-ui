/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Multi-threaded non-blocking audit log writing service. Suitable for using in REST APIs.
 */
public class ConcurrentAuditLogService extends SimpleAuditLogService {

    private static final int DEFAULT_BACKLOG_SIZE = 1024;

    private final AtomicBoolean run = new AtomicBoolean(true);

    private final ExecutorService executorService;

    private final BlockingQueue<AuditLogEvent> queue;

    private final RejectionHandler rejectionHandler;

    private final Future<?> future;

    /**
     * Creates a new instance of multi-threaded non-blocking audit log writing service
     * @param component component name
     * @throws IOException if output file can't be created
     */
    public ConcurrentAuditLogService(final String component) throws IOException {
        this(component, DEFAULT_BACKLOG_SIZE);
    }

    /**
     * Creates a new instance of multi-threaded non-blocking audit log writing service
     * @param component component name
     * @param backlogSize size of queue
     * @throws IOException if output file can't be created
     */
    public ConcurrentAuditLogService(final String component, final int backlogSize) throws IOException {
        this(component, new AuditLogEventFileWriter(component), backlogSize, event -> {});
    }

    ConcurrentAuditLogService(final String component, final AuditLogEventWriter writer) throws IOException {
        this(component, writer, DEFAULT_BACKLOG_SIZE, event -> {});
    }

    ConcurrentAuditLogService(final String component, final AuditLogEventWriter writer, final int backlogSize,
                              final RejectionHandler rejectionHandler) throws IOException {
        super(component, writer);

        this.rejectionHandler = notNull(rejectionHandler, "rejection handler");
        this.queue = new LinkedBlockingQueue<>(backlogSize);
        this.executorService = Executors.newSingleThreadExecutor();
        this.future = createConsumerTask(writer);
    }

    private Future<?> createConsumerTask(final AuditLogEventWriter writer) {
        return executorService.submit(() -> {
            while (run.get() || !queue.isEmpty()) {
                try {
                    final AuditLogEvent event = queue.take();
                    writer.logEvent(event);
                } catch (InterruptedException ignored) {
                    // ignore interruption as we are driven by the 'run' flag
                }
            }
        });
    }

    @Override
    protected void doLogEvent(final AuditLogEvent event) {
        if (!queue.offer(event)) {
            logger.error("action=cfal status=error Maximum queue length={} reached, unable to log event={}", queue.size(), event.getType());
            rejectionHandler.handle(event);
        }
    }

    @PreDestroy
    public void destroy() throws Exception {
        try {
            // signal consumer it should stop processing tasks
            run.set(false);
            // wait for consumer to process all remaining tasks
            future.get();
            executorService.shutdown();
        } finally {
            writer.close();
        }
    }

}
