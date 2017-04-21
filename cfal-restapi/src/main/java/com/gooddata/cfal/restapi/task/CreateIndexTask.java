/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.task;

import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Task for index creation
 */
@Component
@ManagedResource(objectName = "com.gooddata.cfal:name=CreateIndexTask")
public class CreateIndexTask {

    private static final Logger logger = LoggerFactory.getLogger(CreateIndexTask.class);
    private final AuditLogEventRepository repository;

    public CreateIndexTask(final AuditLogEventRepository repository) {
        notNull(repository, "repository cannot be null");

        this.repository = repository;
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
    }
}
