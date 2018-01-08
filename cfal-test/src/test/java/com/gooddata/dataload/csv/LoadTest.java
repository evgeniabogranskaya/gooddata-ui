/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.dataload.csv;

import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooddata.util.ResourceUtils;
import org.testng.annotations.Test;

public class LoadTest {

    @Test
    public void shouldSerializeRequest() throws Exception {
        final Load load = Load.newLoadRequest("https://s3.staging/url", "data.csv", singletonList("column1"));
        final String json = new ObjectMapper().writeValueAsString(load);
        final String expectedJson = ResourceUtils.readStringFromResource("/dataload/csv/loadRequest.json");
        assertThat(json, jsonEquals(expectedJson));
    }

    @Test
    public void shouldDeserializeResponse() {
        final Load load = ResourceUtils.readObjectFromResource("/dataload/csv/loadResponse.json", Load.class);

        assertThat(load, notNullValue());
        assertThat(load.getStagingUrl(), nullValue());
        assertThat(load.getDataHeader(), notNullValue());
        assertThat(load.getFileName(), is("data.csv"));
        assertThat(load.getDatasetId(), is("dataset.csv_data"));
        assertThat(load.getStatus(), is("ERROR"));
        assertThat(load.getErrorMessage(), is("Error message."));
        assertThat(load.getSelfLink(), is("/self/link"));
        assertThat(load.getDatasetLink(), is("/dataset/dataset.csv_data"));
        assertThat(load.getExecutionsLink(), is("/self/link/executions"));
    }
}