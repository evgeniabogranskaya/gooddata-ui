/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import com.gooddata.auditlog.TestEnvironmentProperties;
import com.gooddata.authentication.LoginPasswordAuthentication;
import com.gooddata.authentication.SstAuthentication;
import com.gooddata.dataload.csv.SimpleCsvUploadService;
import org.apache.http.client.HttpClient;
import org.springframework.web.client.RestTemplate;

/**
 * CFAL extension of GoodData Java client.
 * Singleton for keeping only one GD session during ATs run.
 * Contains one public constructor for being able to initialize new GD client via SST (used in registrations).
 * Lazy initialized. Not thread safe.
 */
public class CfalGoodData extends GoodData {

    private static CfalGoodData instance;

    private final GoodDataEndpoint endpoint;
    private final ReportExecuteService reportExecuteService;
    private final ExtendedMetadataService extendedMetadataService;
    private final ScheduledMailsAccelerateService scheduledMailsAccelerateService;
    private final SimpleCsvUploadService csvUploadService;
    private final ExtendedAccountService extendedAccountService;
    private final ExtendedExportService extendedExportService;
    private final ExtendedProjectService extendedProjectService;

    /**
     * Creates new GoodData from the given endpoint and authenticated via given Super Secure Token.
     *
     * @param endpoint GD endpoint
     * @param sst Super Secure Token
     */
    public CfalGoodData(final GoodDataEndpoint endpoint, final String sst) {
        this(endpoint, new SstAuthentication(sst));
    }

    /**
     * Created and returned as singleton.
     */
    private CfalGoodData(final GoodDataEndpoint endpoint, final String login, final String password) {
        this(endpoint, new LoginPasswordAuthentication(login, password));
    }

    private CfalGoodData(final GoodDataEndpoint endpoint, final Authentication authentication) {
        super(endpoint, authentication);

        this.endpoint = endpoint;
        this.reportExecuteService = new ReportExecuteService(getRestTemplate(), endpoint);
        this.extendedMetadataService = new ExtendedMetadataService(getRestTemplate());
        this.scheduledMailsAccelerateService = new ScheduledMailsAccelerateService(getRestTemplate());
        this.csvUploadService = new SimpleCsvUploadService(getRestTemplate());
        this.extendedAccountService = new ExtendedAccountService(getRestTemplate());
        this.extendedExportService = new ExtendedExportService(getRestTemplate(), endpoint);
        this.extendedProjectService = new ExtendedProjectService(getRestTemplate(), getAccountService());
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

    @Override
    public ExtendedAccountService getAccountService() {
        return extendedAccountService;
    }

    @Override
    public ExtendedExportService getExportService() {
        return extendedExportService;
    }

    @Override
    public ExtendedProjectService getProjectService() {
        return extendedProjectService;
    }
}
