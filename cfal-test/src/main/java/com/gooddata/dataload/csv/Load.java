/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.dataload.csv;

import static com.gooddata.util.Validate.notEmpty;
import static com.gooddata.util.Validate.notNull;

import com.fasterxml.jackson.annotation.*;

import java.util.List;

/**
 * {@link Load} represents metadata of file uploaded to the staging (e.g. S3).
 */
@JsonTypeName("load")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
class Load {

    static final String LOADS_URI = "/gdc/dataload/internal/projects/{projectId}/csv/loads";
    private static final String EXECUTIONS_SUFFIX = "/executions";

    private final String stagingUrl;
    private final String fileName;
    private final DataHeader dataHeader;
    private final String datasetId;
    private final String status;
    private final String errorMessage;
    private final Links links;

    @JsonCreator
    private Load(@JsonProperty("stagingUrl") final String stagingUrl,
            @JsonProperty("fileName") final String fileName,
            @JsonProperty("dataHeader") final DataHeader dataHeader,
            @JsonProperty("datasetId") final String datasetId,
            @JsonProperty("status") final String status,
            @JsonProperty("errorMessage") String errorMessage,
            @JsonProperty("links") final Links links) {
        notEmpty(fileName, "fileName cannot be empty!");
        notNull(dataHeader, "dataHeader cannot be null!");

        this.stagingUrl = stagingUrl;
        this.fileName = fileName;
        this.dataHeader = dataHeader;
        this.datasetId = datasetId;
        this.status = status;
        this.errorMessage = errorMessage;
        this.links = links;
    }

    /**
     * Creates new CSV load request body.
     *
     * @param stagingUrl staging URL
     * @param fileName name of the file uploaded to given staging URL
     * @param columns column names of CSV columns
     * @return CSV load request body
     */
    static Load newLoadRequest(final String stagingUrl, final String fileName, final List<String> columns) {
        notEmpty(stagingUrl, "stagingUrl");
        notEmpty(fileName, "fileName");
        notEmpty(columns, "columns");

        return new Load(stagingUrl, fileName, DataHeader.fromColumnNames(columns).withHeaderRowAt(1),
                null, null, null, null);
    }

    public String getStagingUrl() {
        return stagingUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public DataHeader getDataHeader() {
        return dataHeader;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonIgnore
    public String getSelfLink() {
        return links == null ? null : links.self;
    }

    @JsonIgnore
    public String getDatasetLink() {
        return links == null ? null : links.dataset;
    }

    @JsonIgnore
    public String getExecutionsLink() {
        final String self = getSelfLink();
        return self == null ? null : self + EXECUTIONS_SUFFIX;
    }

    /**
     * Helper class containing links related to {@link Load}. Deserialization only.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Links {

        private final String self;
        private final String dataset;

        @JsonCreator
        Links(@JsonProperty("self") final String self,
                @JsonProperty("dataset") final String dataset) {
            notEmpty(self, "self cannot be empty");

            this.self = self;
            this.dataset = dataset;
        }
    }
}
