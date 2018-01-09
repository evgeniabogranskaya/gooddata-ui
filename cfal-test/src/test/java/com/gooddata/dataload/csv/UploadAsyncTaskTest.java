/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.dataload.csv;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.gooddata.util.ResourceUtils;
import org.testng.annotations.Test;

public class UploadAsyncTaskTest {

    @Test
    public void shouldDeserialize() {
        final UploadAsyncTask task =
                ResourceUtils.readObjectFromResource("/dataload/csv/uploadAsyncTask.json", UploadAsyncTask.class);

        assertThat(task, notNullValue());
        assertThat(task.getUri(), is("/polling/uri"));
    }
}