/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.dataload.csv;

import static java.util.Arrays.asList;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooddata.util.ResourceUtils;
import org.testng.annotations.Test;

public class DataHeaderTest {

    private static final String JSON_PATH = "/dataload/csv/dataHeader.json";

    @Test
    public void shouldSerialize() throws Exception {
        final DataHeader dataHeader = DataHeader.fromColumnNames(asList("column1", "column2")).withHeaderRowAt(2);
        final String json = new ObjectMapper().writeValueAsString(dataHeader);
        final String expectedJson = ResourceUtils.readStringFromResource(JSON_PATH);
        assertThat(json, jsonEquals(expectedJson));
    }

    @Test
    public void shouldDeserialize() {
        final DataHeader dataHeader = ResourceUtils.readObjectFromResource(JSON_PATH, DataHeader.class);

        assertThat(dataHeader, notNullValue());
        assertThat(dataHeader.getHeaderRowIndex(), is(2));
        assertThat(dataHeader.getColumnNames(), hasItems("column1", "column2"));
    }
}