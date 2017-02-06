/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gooddata.collections.PageableList;
import com.gooddata.collections.Paging;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Pageable list DTO for Audit events
 */
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName(AuditEventsDTO.ROOT_NODE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = AuditEventsDTOSerializer.class)
@JsonDeserialize(using = AuditEventsDTODeserializer.class)
public class AuditEventsDTO extends PageableList<AuditEventDTO> {

    static final String ROOT_NODE = "events";

    public AuditEventsDTO(final List<AuditEventDTO> items, final Paging paging, final Map<String, String> links) {
        super(items, paging, links);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final AuditEventsDTO that = (AuditEventsDTO) o;

        if (!Arrays.deepEquals(this.toArray(), that.toArray())) {
            return false;
        }

        if ((this.getPaging() == null && that.getPaging() != null) || (this.getPaging() != null && that.getPaging() == null)) {
            return false;
        }

        if(this.getPaging() != null && that.getPaging() != null) {
            if ((this.getPaging().getNextUri() == null && that.getPaging().getNextUri() != null) ||  (this.getPaging().getNextUri() != null && that.getPaging().getNextUri() == null)) {
                return false;
            }
            if(this.getPaging().getNextUri() != that.getPaging().getNextUri()) {
                if (!this.getPaging().getNextUri().equals(that.getPaging().getNextUri())) {
                    return false;
                }
            }
        }

        if(this.getLinks() != null && that.getLinks() != null) {
            return this.getLinks().equals(that.getLinks());
        }

        return this.getLinks() == that.getLinks();
    }

    @Override
    public int hashCode() {
        return iterator().hashCode() +
                (getNextPage() == null ? 0 : getNextPage().hashCode()) +
                (getLinks() == null ? 0 : getLinks().hashCode());
    }
}
