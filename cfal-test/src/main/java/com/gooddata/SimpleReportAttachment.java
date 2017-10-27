/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gooddata.md.Attachment;

import java.util.List;

/**
 * Report attachment for creating scheduled mail
 */
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName(SimpleReportAttachment.ROOT_NODE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleReportAttachment extends Attachment {

    public static final String ROOT_NODE = "reportAttachment";

    private final List<String> formats;

    @JsonCreator
    public SimpleReportAttachment(@JsonProperty("uri") final String uri, final List<String> formats) {
        super(uri);
        this.formats = formats;
    }

    public List<String> getFormats() {
        return formats;
    }
}
