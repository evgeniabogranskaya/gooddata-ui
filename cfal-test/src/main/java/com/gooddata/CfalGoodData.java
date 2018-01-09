/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import com.gooddata.auditlog.TestEnvironmentProperties;
import com.gooddata.authentication.LoginPasswordAuthentication;
import com.gooddata.dataload.csv.SimpleCsvUploadService;
import org.apache.http.client.HttpClient;
import org.springframework.web.client.RestTemplate;

/**
 * CFAL extension of GoodData Java client. Singleton for keeping only one GD session during ATs run.
 * Lazy initialized. Not thread safe.
 */
public class CfalGoodData extends GoodData {

    private static CfalGoodData instance;

    private final GoodDataEndpoint endpoint;
    private final ReportExecuteService reportExecuteService;
    private final ExtendedMetadataService extendedMetadataService;
    private final ScheduledMailsAccelerateService scheduledMailsAccelerateService;
    private final SimpleCsvUploadService csvUploadService;

    private CfalGoodData(final GoodDataEndpoint endpoint, final String login, final String password) {
        super(endpoint, new LoginPasswordAuthentication(login, password));

        this.endpoint = endpoint;
        this.reportExecuteService = new ReportExecuteService(getRestTemplate(), endpoint);
        this.extendedMetadataService = new ExtendedMetadataService(getRestTemplate());
        this.scheduledMailsAccelerateService = new ScheduledMailsAccelerateService(getRestTemplate());
        this.csvUploadService = new SimpleCsvUploadService(getRestTemplate());
    }

    public static CfalGoodData getInstance() {
        if (instance == null) {
            final String login = TestEnvironmentProperties.getInstance().getUser();
            final String password = TestEnvironmentProperties.getInstance().getPass();
            final GoodDataEndpoint endpoint = new GoodDataEndpoint(TestEnvironmentProperties.getInstance().getHost());
            instance = new CfalGoodData(endpoint, login, password);
        }
        return instance;
    }

    /**
     * Creates new REST template with new HTTP Client configuration and GoodData Platform endpoint of this GD session
     *
     * @param httpClient new HTTP client config
     * @return REST template
     */
    public RestTemplate createRestTemplate(final HttpClient httpClient) {
        return GoodData.createRestTemplate(endpoint, httpClient);
    }

    /**
     * @return GoodData Platform endpoint of this GD session
     */
    public GoodDataEndpoint getEndpoint() {
        return endpoint;
    }

    public ReportExecuteService getReportExecuteService() {
        return reportExecuteService;
    }

    @Override
    public ExtendedMetadataService getMetadataService() {
        return extendedMetadataService;
    }

    public ScheduledMailsAccelerateService getScheduledMailsAccelerateService() {
        return scheduledMailsAccelerateService;
    }

    public SimpleCsvUploadService getCsvUploadService() {
        return csvUploadService;
    }
}
