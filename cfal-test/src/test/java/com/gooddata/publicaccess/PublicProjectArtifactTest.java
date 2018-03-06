/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.publicaccess;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooddata.util.ResourceUtils;
import org.testng.annotations.Test;

public class PublicProjectArtifactTest {

    @Test
    public void shouldSerialize() throws Exception {
        final PublicProjectArtifact artifact = new PublicProjectArtifact("project");

        final String json = new ObjectMapper().writeValueAsString(artifact);
        final String expectedJson = ResourceUtils.readStringFromResource("/publicaccess/publicProjectArtifact.json");
        assertThat(json, jsonEquals(expectedJson));
    }
}