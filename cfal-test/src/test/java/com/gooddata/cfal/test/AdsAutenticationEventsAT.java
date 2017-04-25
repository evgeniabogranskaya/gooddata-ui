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
 * Acceptance test for ADS login and logout
 */
public class AdsAutenticationEventsAT extends AbstractAT {

    private static final String TEST_QUERY = "SELECT 1";
    private static final String CFAL_INSTANCE_NAME = "CFAL test";
    private static final String MESSAGE_TYPE_LOGIN = "DATAWAREHOUSE_USERNAME_PASSWORD_LOGIN";
    private static final String MESSAGE_TYPE_LOGOUT = "DATAWAREHOUSE_LOGOUT";

    private final String datawarehouseToken;
    private final JdbcTemplate jdbcTemplate;
    private final Warehouse warehouse;

    public AdsAutenticationEventsAT() {
        super();
        this.datawarehouseToken = getProperty("datawarehouseToken", "vertica");
        this.warehouse = gd.getWarehouseService().createWarehouse(createWarehouseRequest()).get();
        final DataSource dataSource = createDataSource(warehouse);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test(groups = MESSAGE_TYPE_LOGIN)
    public void testUsernamePasswordAuthUserApi() throws InterruptedException {
        jdbcTemplate.execute(TEST_QUERY);
        doTestUserApi(pageCheckPredicate(MESSAGE_TYPE_LOGIN));
        doTestUserApi(pageCheckPredicate(MESSAGE_TYPE_LOGOUT));
    }

    @Test(groups = MESSAGE_TYPE_LOGIN)
    public void testUsernamePasswordAuthAdminApi() throws InterruptedException {
        jdbcTemplate.execute(TEST_QUERY);
        doTestAdminApi(pageCheckPredicate(MESSAGE_TYPE_LOGIN));
        doTestAdminApi(pageCheckPredicate(MESSAGE_TYPE_LOGOUT));
    }

    @AfterGroups(groups = MESSAGE_TYPE_LOGIN)
    public void tearDown() {
        if (warehouse != null) {
            gd.getWarehouseService().removeWarehouse(warehouse);
        }
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final String eventType) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(eventType));
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
