/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.dataload.csv;

import com.fasterxml.jackson.annotation.*;

/**
 * Asynchronous task containing link for polling.
 * This task differs from {@link com.gooddata.gdc.AsyncTask} in links field.
 * Deserialization only.
 *
 * This is the hard copy of {@link com.gooddata.dataload.processes.AsyncTask} class which is package private.
 * It doesn't make sense to make it public for now because in original SDK only processes resources use this type of
 * async response. When the CSV upload is moved to SDK, the original class will be set to public and this one will
 * be deleted.
 */
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName("asyncTask")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class UploadAsyncTask {

    @JsonProperty
    private final Links links;

    @JsonCreator
    private UploadAsyncTask(@JsonProperty("links") Links links) {
        this.links = links;
    }

    @JsonIgnore
    public String getUri() {
        return links.poll;
    }

    private static class Links {

        private final String poll;

        @JsonCreator
        private Links(@JsonProperty("poll") String poll) {
            this.poll = poll;
        }
    }
}
