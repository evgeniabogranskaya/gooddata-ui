/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.gooddata.cfal.CfalMonitoringMetricConstants.QUEUE_REJECTED_COUNT;
import static com.gooddata.cfal.CfalMonitoringMetricConstants.QUEUE_SIZE;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

/**
 * Multi-threaded non-blocking audit log writing service. Suitable for using in REST APIs.
 */
public class ConcurrentAuditLogService extends SimpleAuditLogService implements MetricSet {

    static final AuditLogEvent POISON_PILL = new AuditLogEvent("PP", "poison", "pill", "die");

    static final int DEFAULT_BACKLOG_SIZE = 1024;

    private final AtomicBoolean run = new AtomicBoolean(true);

    private final ExecutorService executorService;

    private final BlockingQueue<AuditLogEvent> queue;

    private final RejectionHandler rejectionHandler;

    private final AtomicLong enqueueErrorCounter = new AtomicLong();

    private final Future<?> future;

    public ConcurrentAuditLogService(final String component, final AuditLogEventWriter writer) throws IOException {
        this(component, writer, DEFAULT_BACKLOG_SIZE, null);
    }

    public ConcurrentAuditLogService(final String component, final AuditLogEventWriter writer,
                                     final int backlogSize) throws IOException {
        this(component, writer, backlogSize, null);
    }

    public ConcurrentAuditLogService(final String component, final AuditLogEventWriter writer, final int backlogSize,
                              final RejectionHandler rejectionHandler) throws IOException {
        super(component, writer);

        this.rejectionHandler = rejectionHandler != null ? rejectionHandler : new DefaultRejectionHandler();
        this.queue = new LinkedBlockingQueue<>(backlogSize);
        this.executorService = Executors.newSingleThreadExecutor();
        this.future = createConsumerTask(writer);
    }

    /**
     * @return Size of internal queue used for buffering events before writing
     */
    public long getQueueSize() {
        return queue.size();
    }

    /**
     * @return Number of errors caused by putting events into queue which was full
     */
    public long getEnqueueErrors() {
        return enqueueErrorCounter.get();
    }

    @Override
    public Map<String, Metric> getMetrics() {

        final Map<String, Metric> gauges = super.getMetrics();

        final Gauge<Long> gaugeQueueSize = () -> getQueueSize();
        final Gauge<Long> gaugeEnqueueErrorCount = () -> getEnqueueErrors();

        gauges.put(QUEUE_SIZE, gaugeQueueSize);
        gauges.put(QUEUE_REJECTED_COUNT, gaugeEnqueueErrorCount);

        return gauges;
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
            enqueueErrorCounter.incrementAndGet();
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

    private class DefaultRejectionHandler implements RejectionHandler {

        @Override
        public void handle(final AuditLogEvent event) {
            logger.error("action=cfal status=error Maximum queue length={} reached, unable to log event={}", queue.size(), event.getType());
        }
    }
}
