/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.FutureResult;
import com.gooddata.GoodData;
import com.gooddata.project.Environment;
import com.gooddata.warehouse.Warehouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Singleton for ADS related stuff. Not thread safe.
 */
public class AdsService {

    private static AdsService instance;

    private List<Warehouse> warehouses = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(AdsService.class);

    private static final String CFAL_INSTANCE_NAME = "CFAL test";

    private final GoodData gd;
    private final TestEnvironmentProperties props;

    public static AdsService getInstance(final GoodData gd, final TestEnvironmentProperties props) {
        if (instance != null) {
            return instance;
        }
        return instance = new AdsService(gd, props);
    }

    private AdsService(final GoodData gd, final TestEnvironmentProperties props) {
        this.gd = notNull(gd, "gd");
        this.props = notNull(props, "props");
    }

    /**
     * Creates JdbcTemplate for given warehouse
     * @param warehouse
     * @return instance of JdbcTemplate
     */
    public JdbcTemplate createJdbcTemplate(final Warehouse warehouse) {
        final DataSource dataSource = createDataSource(warehouse);
        return new JdbcTemplate(dataSource);
    }

    /**
     * Get warehouse created before or create a new one
     * @return Warehouse instance
     */
    public Warehouse getOrCreateWarehouse() {
        if (warehouses.isEmpty()) {
            return createWarehouse();
        }
        return warehouses.get(0);

    }

    /**
     * Create warehouse
     * @return warehouse instance
     */
    public Warehouse createWarehouse() {
        final FutureResult<Warehouse> result = gd.getWarehouseService().createWarehouse(createWarehouseRequest());
        logger.info("Creating warehouse uri={}", result.getPollingUri());
        final Warehouse warehouse = result.get(props.getPollTimeoutMinutes(), props.getPollTimeoutUnit());

        logger.info("Created warehouse_id={}", warehouse.getId());

        warehouses.add(warehouse);

        return warehouse;
    }

    /**
     * Removes all created warehouses
     */
    public void destroy() {
        warehouses.stream().forEach(e -> {
            try {
                gd.getWarehouseService().removeWarehouse(e);
                logger.info("removed warehouse_id={}", e.getId());
            } catch (Exception ex) {
                logger.warn("could not remove warehouse_id={}", e.getId());
            }
        });
    }

    private DriverManagerDataSource createDataSource(final Warehouse warehouse) {
        return new DriverManagerDataSource(warehouse.getConnectionUrl(), props.getUser(), props.getPass());
    }

    private Warehouse createWarehouseRequest() {
        final Warehouse warehouse = new Warehouse(CFAL_INSTANCE_NAME, props.getDatawarehouseToken());
        warehouse.setEnvironment(Environment.TESTING);
        return warehouse;
    }
}
