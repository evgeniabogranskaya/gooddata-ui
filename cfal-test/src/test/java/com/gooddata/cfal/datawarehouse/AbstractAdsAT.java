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

    private static JdbcTemplate jdbcTemplate;

    protected Predicate<List<AuditEventDTO>> pageCheckPredicate(final String eventType) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> matchEvent(eventType, e));
    }

    private boolean matchEvent(String eventType, AuditEventDTO e) {
        return e.getUserLogin().equals(account.getLogin()) &&
                e.getType().equals(eventType) &&
                e.getLinks() != null &&
                getWarehouse().getUri().equals(e.getLinks().get("datawarehouse"));
    }

    protected JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }

        return jdbcTemplate = adsService.createJdbcTemplate(getWarehouse());
    }

    protected Warehouse getWarehouse() {
        return adsService.getOrCreateWarehouse();
    }
}
