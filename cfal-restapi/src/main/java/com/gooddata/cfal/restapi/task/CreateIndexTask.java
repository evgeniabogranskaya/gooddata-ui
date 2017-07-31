/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.task;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.UniformReservoir;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import static com.codahale.metrics.MetricRegistry.name;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.TimeUnit;

/**
 * Task for index creation
 */
@Component
@ManagedResource
public class CreateIndexTask {

    private static final Logger logger = LoggerFactory.getLogger(CreateIndexTask.class);

    static final String TTL_INDEX_TIMER_NAME = "ttlIndexTimer";
    static final String LOGIN_INDEX_TIMER_NAME = "loginIndexTimer";

    private final Timer ttlIndexTimer;
    private final Timer loginIndexTimer;

    private final AuditLogEventRepository repository;
    private final MetricRegistry metricRegistry;

    public CreateIndexTask(final AuditLogEventRepository repository, final MetricRegistry metricRegistry) {
        notNull(repository, "repository cannot be null");
        notNull(metricRegistry, "metricRegistry cannot be null");

        this.repository = repository;
        this.metricRegistry = metricRegistry;

        ttlIndexTimer = initTimer(TTL_INDEX_TIMER_NAME);
        loginIndexTimer = initTimer(LOGIN_INDEX_TIMER_NAME);
    }

    /**
     * This task is responsible for creation of the TTL-indexes on CFAL related collections.
     * <p>
     * Task is scheduled to midnight, but it has no performance impact to this application, as all indexes should be
     * created in asynchronously.
     * <p>
     * See {@link AuditLogEventRepository#createTtlIndexes} for more information about indexes being created.
     */
    @Scheduled(cron = "${gdc.cfal.mongo.task.ttl-index.cron}")
    @ManagedOperation(description="Create TTL indexes on CFAL mongo collections")
    public void createTtlIndexes() {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            logger.info("action=create_ttl_indexes action=start");
            repository.createTtlIndexes();
            stopWatch.stop();
            logger.info("action=create_ttl_indexes action=finished time=" + stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            stopWatch.stop();
            logger.warn("action=create_ttl_indexes action=error time=" + stopWatch.getTotalTimeMillis(), e);
        }
        finally {
            ttlIndexTimer.update(stopWatch.getTotalTimeMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Scheduled(cron = "${gdc.cfal.mongo.task.user-login-index.cron}")
    @ManagedOperation(description="Create User-login indexes on CFAL mongo collections")
    public void createUserLoginIndexes() {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            logger.info("action=create_user_login_indexes action=start");
            repository.createUserLoginIndexes();
            stopWatch.stop();
            logger.info("action=create_user_login_indexes action=finished time=" + stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            stopWatch.stop();
            logger.warn("action=create_user_login_indexes action=error time=" + stopWatch.getTotalTimeMillis(), e);
        }
        finally {
            loginIndexTimer.update(stopWatch.getTotalTimeMillis(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Timers use by default exponentially decaying reservoir,
     * which is by default heavily biased to the past 5 minutes of measurements,
     * which is not desired behaviour for sparsely triggered events (like index creation).
     */
    private Timer initTimer(final String name) {
        final Timer timer = new Timer(new UniformReservoir());
        metricRegistry.register(name(getClass().getSimpleName(), name), timer);
        return timer;
    }
}
