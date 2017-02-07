/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import com.gooddata.collections.PageRequest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;

/**
 * Class to encapsulate time filtering and paging parameters
 */
public class RequestParameters extends PageRequest {

    private DateTime from;

    private DateTime to;

    public RequestParameters() {
    }

    public DateTime getFrom() {
        return from;
    }

    /**
     * Specify lower bound of interval
     */
    public void setFrom(final DateTime from) {
        this.from = from;
    }

    public DateTime getTo() {
        return to;
    }

    /**
     * Specify upper bound of interval
     */
    public void setTo(final DateTime to) {
        this.to = to;
    }

    public ObjectId getOffsetAsObjectId() {
        return getOffset() == null ? null : new ObjectId(getOffset());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RequestParameters that = (RequestParameters) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(from, that.from)
                .append(to, that.to)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(from)
                .append(to)
                .toHashCode();
    }
}
