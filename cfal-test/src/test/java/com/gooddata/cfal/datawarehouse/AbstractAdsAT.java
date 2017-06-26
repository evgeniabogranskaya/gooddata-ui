/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.datawarehouse;

import com.gooddata.FutureResult;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.test.AbstractAT;
import com.gooddata.project.Environment;
import com.gooddata.warehouse.Warehouse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.System.getProperty;

/**
 * Common parent for all ADS related AT
 */
abstract class AbstractAdsAT extends AbstractAT {
    private static final String CFAL_INSTANCE_NAME = "CFAL test";

    private final String datawarehouseToken;
    private final JdbcTemplate jdbcTemplate;
    private final Warehouse warehouse;

    AbstractAdsAT() {
        super();
        this.datawarehouseToken = getProperty("datawarehouseToken", "vertica");
        final FutureResult<Warehouse> result = gd.getWarehouseService().createWarehouse(createWarehouseRequest());
        logger.info("Creating warehouse uri={}", result.getPollingUri());
        this.warehouse = result.get(POLL_TIMEOUT, POLL_TIMEOUT_UNIT);
        logger.info("Created warehouse_id={}", this.warehouse.getId());
        final DataSource dataSource = createDataSource(this.warehouse);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    protected Predicate<List<AuditEventDTO>> pageCheckPredicate(final String eventType) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> matchEvent(eventType, e));
    }

    private boolean matchEvent(String eventType, AuditEventDTO e) {
        return e.getUserLogin().equals(account.getLogin()) &&
                e.getType().equals(eventType) &&
                e.getLinks() != null &&
                warehouse.getUri().equals(e.getLinks().get("datawarehouse"));
    }

    protected void safelyDeleteAds() {
        if (warehouse != null) {
            gd.getWarehouseService().removeWarehouse(warehouse);
        }
    }

    private DriverManagerDataSource createDataSource(final Warehouse warehouse) {
        return new DriverManagerDataSource(warehouse.getConnectionUrl(), user, pass);
    }

    private Warehouse createWarehouseRequest() {
        final Warehouse warehouse = new Warehouse(CFAL_INSTANCE_NAME, datawarehouseToken);
        warehouse.setEnvironment(Environment.TESTING);
        return warehouse;
    }

    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
