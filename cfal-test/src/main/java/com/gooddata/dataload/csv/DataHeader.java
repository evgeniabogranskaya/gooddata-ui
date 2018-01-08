/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.dataload.csv;

import static com.gooddata.util.Validate.notEmpty;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents CSV upload metadata.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataHeader {

    private final Integer headerRowIndex;
    private final List<Column> columns;

    @JsonCreator
    private DataHeader(@JsonProperty("headerRowIndex") final Integer headerRowIndex,
            @JsonProperty("columns") final List<Column> columns) {
        this.headerRowIndex = headerRowIndex;
        this.columns = columns;
    }

    /**
     * Creates DataHeader from column names converting names to {@link Column} objects with ATTRIBUTE type
     * and last column name to column with FACT type.
     *
     * @param columnNames CSV column names
     * @return new DataHeader
     */
    static DataHeader fromColumnNames(final List<String> columnNames) {
        notEmpty(columnNames, "columnNames");

        final List<Column> columns = new ArrayList<>();

        for (int i = 0; i < columnNames.size(); i++) {
            final String name = columnNames.get(i);
            if (i == (columnNames.size() - 1)) {
                columns.add(Column.newFactColumn(name));
            } else {
                columns.add(Column.newAttributeColumn(name));
            }
        }

        return new DataHeader(null, columns);
    }

    @JsonIgnore
    public DataHeader withHeaderRowAt(final int headerRowIndex) {
        return new DataHeader(headerRowIndex, getColumns());
    }

    @JsonIgnore
    public List<String> getColumnNames() {
        return columns.stream().map(Column::getName).collect(toList());
    }

    public Integer getHeaderRowIndex() {
        return headerRowIndex;
    }

    public List<Column> getColumns() {
        return columns;
    }
}
