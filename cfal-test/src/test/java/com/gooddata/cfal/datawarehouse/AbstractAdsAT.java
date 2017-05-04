/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.datawarehouse;

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
class AbstractAdsAT extends AbstractAT {
    private static final String CFAL_INSTANCE_NAME = "CFAL test";

    private final String datawarehouseToken;
    private final JdbcTemplate jdbcTemplate;
    private final Warehouse warehouse;

    AbstractAdsAT() {
        super();
        this.datawarehouseToken = getProperty("datawarehouseToken", "vertica");
        this.warehouse = gd.getWarehouseService().createWarehouse(createWarehouseRequest()).get();
        final DataSource dataSource = createDataSource(warehouse);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    protected Predicate<List<AuditEventDTO>> pageCheckPredicate(final String eventType) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(eventType));
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
