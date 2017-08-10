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

/**
 * Multi-threaded non-blocking audit log writing service. Suitable for using in REST APIs.
 */
public class ConcurrentAuditLogService extends SimpleAuditLogService {

    static final AuditLogEvent POISON_PILL = new AuditLogEvent("PP", "poison", "pill", "die");

    static final int DEFAULT_BACKLOG_SIZE = 1024;

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
        this(component, new AuditLogEventFileWriter(component), backlogSize, null);
    }

    ConcurrentAuditLogService(final String component, final AuditLogEventWriter writer) throws IOException {
        this(component, writer, DEFAULT_BACKLOG_SIZE, null);
    }

    ConcurrentAuditLogService(final String component, final AuditLogEventWriter writer, final int backlogSize,
                              final RejectionHandler rejectionHandler) throws IOException {
        super(component, writer);

        this.rejectionHandler = rejectionHandler != null ? rejectionHandler : new DefaultRejectionHandler();
        this.queue = new LinkedBlockingQueue<>(backlogSize);
        this.executorService = Executors.newSingleThreadExecutor();
        this.future = createConsumerTask(writer);
    }

    private Future<?> createConsumerTask(final AuditLogEventWriter writer) {
        return executorService.submit(() -> {
            while (run.get() || !queue.isEmpty()) {
                try {
                    final AuditLogEvent event = queue.take();
                    if (event == POISON_PILL) {
                        break;
                    }
                    writer.logEvent(event);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); // to set interrupt flag
                }
            }
        });
    }

    @Override
    protected void doLogEvent(final AuditLogEvent event) {
        if (!queue.offer(event)) {
            rejectionHandler.handle(event);
        }
    }

    @PreDestroy
    public void destroy() throws Exception {
        try {
            // signal consumer it should stop processing tasks
            run.set(false);
            //force consumer to die
            queue.put(POISON_PILL);
            // wait for consumer to process all remaining tasks
            future.get();
            executorService.shutdown();
        } finally {
            writer.close();
        }
    }

    public int getQueueSize() {
        return queue.size();
    }

    private class DefaultRejectionHandler implements RejectionHandler {

        @Override
        public void handle(final AuditLogEvent event) {
            logger.error("action=cfal status=error Maximum queue length={} reached, unable to log event={}", queue.size(), event.getType());
        }
    }
}
