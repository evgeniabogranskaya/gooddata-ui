/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import com.gooddata.auditlog.AuditLogService;
import org.apache.http.client.HttpClient;
import org.springframework.web.client.RestTemplate;

/**
 * CFAL extension of GoodData Java client. To be removed once moved into SDK.
 */
public class CfalGoodData extends GoodData {

    private final AuditLogService auditLogService;

    public CfalGoodData(final String hostname, final String login, final String password) {
        super(hostname, login, password);
        auditLogService = new AuditLogService(getRestTemplate());
    }

    public AuditLogService getAuditLogService() {
        return auditLogService;
    }

    public static RestTemplate createRestTemplate(final GoodDataEndpoint endpoint, final HttpClient httpClient) {
        return GoodData.createRestTemplate(endpoint, httpClient);
    }
}
