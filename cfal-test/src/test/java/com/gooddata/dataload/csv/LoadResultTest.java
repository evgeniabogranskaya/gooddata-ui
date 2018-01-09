/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.dataload.csv;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.gooddata.util.ResourceUtils;
import org.testng.annotations.Test;

public class LoadResultTest {

    @Test
    public void shouldDeserialize() {
        final LoadResult res = ResourceUtils.readObjectFromResource("/dataload/csv/loadResult.json", LoadResult.class);

        assertThat(res, notNullValue());
        assertThat(res.isOk(), is(true));
    }
}