/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.dataload.csv;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.gooddata.util.ResourceUtils;
import org.testng.annotations.Test;

public class StagingInfoTest {

    @Test
    public void shouldDeserialize() {
        final StagingInfo stagingInfo =
                ResourceUtils.readObjectFromResource("/dataload/csv/stagingInfo.json", StagingInfo.class);

        assertThat(stagingInfo, notNullValue());
        assertThat(stagingInfo.getStagingUrl(), is("https://staging/url"));
    }
}