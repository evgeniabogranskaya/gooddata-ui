/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.publicaccess;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("publicProjectArtifact")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class PublicProjectArtifact {

    @JsonProperty("artifact")
    private final String artifact;

    public PublicProjectArtifact(final String artifact) {
        this.artifact = artifact;
    }

    public String getArtifact() {
        return artifact;
    }
}
