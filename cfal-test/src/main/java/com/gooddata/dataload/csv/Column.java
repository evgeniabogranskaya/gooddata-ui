/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.dataload.csv;

import static com.gooddata.util.Validate.notEmpty;

import com.fasterxml.jackson.annotation.*;

/**
 * CSV column metadata
 */
@JsonTypeName("column")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class Column {

    private static final String ATTRIBUTE = "ATTRIBUTE";
    private static final String FACT = "FACT";

    private final String name;
    private final String type;

    @JsonCreator
    private Column(@JsonProperty("name") final String name, @JsonProperty("type") final String type) {
        this.name = notEmpty(name, "name");
        this.type = notEmpty(type, "type");
    }

    /**
     * Creates new CSV column with the ATTRIBUTE type
     *
     * @param name column name
     * @return new attribute column
     */
    static Column newAttributeColumn(final String name) {
        return new Column(notEmpty(name, "name"), ATTRIBUTE);
    }

    static Column newFactColumn(final String name) {
        return new Column(notEmpty(name, "name"), FACT);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}