/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.project.Environment;
import com.gooddata.warehouse.Warehouse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.System.getProperty;

/**
 * Acceptance test for ADS login
 */
public class AdsLoginAT extends AbstractAT {

    private static final String TEST_QUERY = "SELECT 1";
    private static final String CFAL_INSTANCE_NAME = "CFAL test";
    private static final String MESSAGE_TYPE = "DATAWAREHOUSE_USERNAME_PASSWORD_LOGIN";

    private final String datawarehouseToken;
    private final JdbcTemplate jdbcTemplate;
    private final Warehouse warehouse;

    public AdsLoginAT() {
        super();
        this.datawarehouseToken = getProperty("datawarehouseToken", "vertica");
        this.warehouse = gd.getWarehouseService().createWarehouse(createWarehouseRequest()).get();
        final DataSource dataSource = createDataSource(warehouse);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testUsernamePasswordLoginUserApi() throws InterruptedException {
        jdbcTemplate.execute(TEST_QUERY);
        doTestUserApi(pageCheckPredicate());
    }

    @Test(groups = MESSAGE_TYPE)
    public void testUsernamePasswordLoginAdminApi() throws InterruptedException {
        jdbcTemplate.execute(TEST_QUERY);
        doTestAdminApi(pageCheckPredicate());
    }

    @AfterGroups(groups = MESSAGE_TYPE)
    public void tearDown() {
        if (warehouse != null) {
            gd.getWarehouseService().removeWarehouse(warehouse);
        }
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate() {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE));
    }

    private DriverManagerDataSource createDataSource(final Warehouse warehouse) {
        return new DriverManagerDataSource(warehouse.getConnectionUrl(), user, pass);
    }

    private Warehouse createWarehouseRequest() {
        final Warehouse warehouse = new Warehouse(CFAL_INSTANCE_NAME, datawarehouseToken);
        warehouse.setEnvironment(Environment.TESTING);
        return warehouse;
    }
}
