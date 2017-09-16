/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import com.gooddata.authentication.LoginPasswordAuthentication;
import org.apache.http.client.HttpClient;
import org.springframework.web.client.RestTemplate;

/**
 * CFAL extension of GoodData Java client. To be removed once moved into SDK.
 */
public class CfalGoodData extends GoodData {

    private final ReportExecuteService reportExecuteService;
    private final ExtendedMetadataService extendedMetadataService;

    public CfalGoodData(final GoodDataEndpoint endpoint, final String login, final String password) {
        super(endpoint, new LoginPasswordAuthentication(login, password));

        reportExecuteService = new ReportExecuteService(getRestTemplate(), endpoint);
        extendedMetadataService = new ExtendedMetadataService(getRestTemplate());
    }

    public static RestTemplate createRestTemplate(final GoodDataEndpoint endpoint, final HttpClient httpClient) {
        return GoodData.createRestTemplate(endpoint, httpClient);
    }

    public ReportExecuteService getReportExecuteService() {
        return reportExecuteService;
    }

    @Override
    public ExtendedMetadataService getMetadataService() {
        return extendedMetadataService;
    }
}
