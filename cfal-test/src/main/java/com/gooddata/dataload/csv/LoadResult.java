/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.dataload.csv;


import static org.apache.commons.lang.Validate.notEmpty;

import com.fasterxml.jackson.annotation.*;

/**
 * {@link LoadResult} represents result of CSV upload execution.
 * Deserialization only.
 */
@JsonTypeName("loadResult")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
class LoadResult {

    private static final String OK_STATUS = "OK";

    /** Result status of csv load (execution) */
    private final String status;

    @JsonCreator
    private LoadResult(@JsonProperty("status") final String status) {
        this.status = status;
    }

    @JsonIgnore
    boolean isOk() {
        return OK_STATUS.equals(status);
    }
}
