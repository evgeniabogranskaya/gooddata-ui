/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.dataload.csv;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooddata.util.ResourceUtils;
import org.testng.annotations.Test;

public class ColumnTest {

    private static final String JSON_PATH = "/dataload/csv/column.json";

    @Test
    public void shouldSerialize() throws Exception {
        final Column column = Column.newAttributeColumn("column1");
        final String json = new ObjectMapper().writeValueAsString(column);
        final String expectedJson = ResourceUtils.readStringFromResource(JSON_PATH);
        assertThat(json, jsonEquals(expectedJson));
    }

    @Test
    public void shouldDeserialize() {
        final Column column = ResourceUtils.readObjectFromResource(JSON_PATH, Column.class);
        assertThat(column, notNullValue());
        assertThat(column.getName(), is("column1"));
        assertThat(column.getType(), is("ATTRIBUTE"));
    }
}