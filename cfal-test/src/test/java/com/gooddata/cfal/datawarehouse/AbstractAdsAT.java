/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.datawarehouse;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.warehouse.Warehouse;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.function.Predicate;


/**
 * Common parent for all ADS related AT
 */
abstract class AbstractAdsAT extends AbstractAT {

    private final JdbcTemplate jdbcTemplate;
    private final Warehouse warehouse;

    AbstractAdsAT() {
        super();
        this.warehouse = adsService.createWarehouse();
        this.jdbcTemplate = adsService.createJdbcTemplate(warehouse);
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

    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    protected Warehouse getWarehouse() {
        return warehouse;
    }
}
