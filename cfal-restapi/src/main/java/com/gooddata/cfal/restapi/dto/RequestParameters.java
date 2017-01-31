/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import com.gooddata.cfal.restapi.exception.InvalidOffsetException;
import com.gooddata.collections.PageRequest;
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
        try {
            return getOffset() == null ? null : new ObjectId(getOffset());
        } catch (IllegalArgumentException ex) {
            throw new InvalidOffsetException("Invalid offset " + getOffset(), ex);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        final RequestParameters that = (RequestParameters) o;

        if (from != null ? !from.equals(that.from) : that.from != null)
            return false;
        return to != null ? to.equals(that.to) : that.to == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }
}
