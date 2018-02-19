/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.CfalGoodData;
import com.gooddata.FutureResult;
import com.gooddata.GoodData;
import com.gooddata.project.Environment;
import com.gooddata.warehouse.Warehouse;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Singleton for ADS related stuff.
 * Lazy initialized. Not thread safe.
 */
public class AdsHelper {

    private static AdsHelper instance;

    private final List<Warehouse> warehouses = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(AdsHelper.class);

    private static final String CFAL_INSTANCE_NAME = "CFAL test";
    private static final List<String> DEFAULT_MODEL_DDL_SCRIPTS = asList("city.sql", "person.sql");

    private final GoodData gd;
    private final TestEnvironmentProperties props;

    public static AdsHelper getInstance() {
        if (instance == null) {
            final TestEnvironmentProperties props = TestEnvironmentProperties.getInstance();
            final CfalGoodData gd = CfalGoodData.getInstance();
            instance = new AdsHelper(gd, props);
        }
        return instance;
    }

    private AdsHelper(final GoodData gd, final TestEnvironmentProperties props) {
        this.gd = notNull(gd, "gd");
        this.props = notNull(props, "props");
    }

    /**
     * Creates JdbcTemplate for given warehouse
     * @param warehouse ADS warehouse
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
     * Sets up default data model in the given ADS (adds city and person tables).
     *
     * @param warehouse ADS warehouse
     * @throws Exception if reading of any DDL script fails
     */
    public void setupDefaultModel(final Warehouse warehouse) throws Exception {
        notNull(warehouse, "warehouse cannot be null!");

        final JdbcTemplate jdbcTemplate = createJdbcTemplate(warehouse);

        for (String scriptPath : DEFAULT_MODEL_DDL_SCRIPTS) {
            updateModelWithScript(jdbcTemplate, scriptPath);
        }

        logger.info("executed ddl scripts on warehouse_id={}", warehouse.getId());
    }

    /**
     * Removes all created warehouses
     */
    public void destroy() {
        warehouses.forEach(warehouse -> {
            try {
                logger.info("removing warehouse_id={}", warehouse.getId());
                gd.getWarehouseService().removeWarehouse(warehouse);
                logger.info("warehouse_id={} removed", warehouse.getId());
            } catch (Exception ex) {
                logger.warn("could not remove warehouse_id=" + warehouse.getId(), ex);
            }
        });
        warehouses.clear();
    }

    private DriverManagerDataSource createDataSource(final Warehouse warehouse) {
        return new DriverManagerDataSource(warehouse.getConnectionUrl(), props.getUser(), props.getPass());
    }

    private Warehouse createWarehouseRequest() {
        final Warehouse warehouse = new Warehouse(CFAL_INSTANCE_NAME, props.getDatawarehouseToken());
        warehouse.setEnvironment(Environment.TESTING);
        return warehouse;
    }

    private void updateModelWithScript(JdbcTemplate jdbcTemplate, String ddlScriptPath) throws Exception {
        final URL resource = getClass().getClassLoader().getResource(ddlScriptPath);
        if (resource == null) {
            throw new IllegalArgumentException("ADS update DDL script '" + ddlScriptPath + "' not found.");
        }

        final File ddlScriptFile = new File(resource.toURI());
        final String ddlScript = FileUtils.readFileToString(ddlScriptFile);
        jdbcTemplate.execute(ddlScript);
    }
}
