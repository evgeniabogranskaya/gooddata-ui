/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.dataload.csv;

import com.fasterxml.jackson.annotation.*;

/**
 * CSV Upload Staging Info. Deserialization only.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class StagingInfo {

    public static final String URI = "/gdc/dataload/internal/projects/{projectId}/csv/stagingInfo";

    private final String stagingUrl;

    @JsonCreator
    StagingInfo(@JsonProperty("stagingUrl") final String stagingUrl) {
        this.stagingUrl = stagingUrl;
    }

    public String getStagingUrl() {
        return stagingUrl;
    }
}
