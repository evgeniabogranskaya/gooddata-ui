/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import com.fasterxml.jackson.databind.JsonNode;
import com.gooddata.export.ExportService;
import com.gooddata.export.ReportRequest;
import com.gooddata.project.Project;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

public class ReportExecuteService extends ExportService {

    private static final String APP_EXECUTE_URI = "/gdc/app/projects/{id}/execute";
    private static final String EXECUTE_URI = "/gdc/projects/{id}/execute";

    //number of times of data result get
    private int timesExecuted = 0;

    public ReportExecuteService(RestTemplate restTemplate, GoodDataEndpoint endpoint) {
        super(restTemplate, endpoint);
    }

    /**
     * Executes report request using /gdc/app/projects/{id}/execute resource
     * @param project
     * @param reportRequest
     * @return result of execution as String
     * @throws Exception
     */
    public String executeUsingAppExecuteResource(final Project project, final ReportRequest reportRequest) throws Exception {
        return doExecute(project, reportRequest, APP_EXECUTE_URI);
    }

    /**
     * Executes report request using /gdc/projects/{id}/execute resource
     * @param project
     * @param reportRequest
     * @return result of execution as String
     * @throws Exception
     */
    public String executeUsingExecuteResource(final Project project, final ReportRequest reportRequest) throws Exception {
        return doExecute(project, reportRequest, EXECUTE_URI);
    }

    /**
     * Executes report request using /gdc/xtab2/executor3 resource
     * @param project
     * @param reportRequest
     * @return result of execution as String
     * @throws Exception
     */
    public String executeUsingXtabExecutorResource(final Project project, final ReportRequest reportRequest) throws Exception {
        return doExecute(project, reportRequest, ReportRequest.URI);
    }

    /**
     *
     * @return how many times execute* methods were called
     */
    public int getTimesExecuted() {
        return timesExecuted;
    }

    private String doExecute(final Project project, final ReportRequest reportRequest, final String uri) {
        final UriTemplate template = new UriTemplate(uri);
        final String result = execute(template.expand(project.getId()).toString(), reportRequest)
                .get();

        timesExecuted++;

        return result;
    }

    /**
     * executes report using given uri and given ReportRequest
     *
     * @param executeUri
     * @param request
     * @return
     * @throws Exception
     */
    private FutureResult<String> execute(final String executeUri, final ReportRequest request) {
        final JsonNode jsonNode = executeReport(executeUri, request);

        final String dataResultUri = jsonNode.path("execResult").path("dataResult").asText();

        return new PollResult<>(this, new SimplePollHandler<String>(dataResultUri, String.class) {
            @Override
            public void handlePollException(final GoodDataRestException e) {
                throw e;
            }
        });
    }
}
