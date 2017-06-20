/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import static org.apache.commons.lang3.Validate.notNull;

import com.gooddata.collections.PageRequest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.BeanUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Class to encapsulate time filtering and paging parameters
 */
public class RequestParameters extends PageRequest {

    private DateTime from;

    private DateTime to;

    private String type;

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

    public String getType() {
        return type;
    }

    /**
     * Specify event type for filtering purposes
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Copy <code>requestParameters</code>
     *
     * @param requestParameters RequestParameters object (not null) to create copy of
     * @return new instance of RequestParameters object, which fields has same value as fields of <code>requestParameters</code>
     */
    public static RequestParameters copy(final RequestParameters requestParameters) {
        notNull(requestParameters, "requestParameters cannot be null");

        final RequestParameters copy = new RequestParameters();
        BeanUtils.copyProperties(requestParameters, copy);

        return copy;
    }

    /**
     * Copy this request parameters and increment request parameter limit.
     * If Limit is negative, than sanitized limit is taken and incremented.
     *
     * @return new instance of RequestParameters with incremented limit
     */
    public RequestParameters withIncrementedLimit() {
        final RequestParameters copy = RequestParameters.copy(this);
        copy.setLimit(this.getSanitizedLimit() + 1);
        return copy;
    }

    @Override
    public UriComponentsBuilder updateWithPageParams(final UriComponentsBuilder builder) {
        UriComponentsBuilder builderWithPaging = super.updateWithPageParams(builder);
        if (from != null) {
            builderWithPaging.queryParam("from", from.toDateTime(DateTimeZone.UTC));
        }
        if (to != null) {
            builderWithPaging.queryParam("to", to.toDateTime(DateTimeZone.UTC));
        }
        if (type != null) {
            builderWithPaging.queryParam("type", type);
        }

        return builderWithPaging;
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
                .append(type, that.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(from)
                .append(to)
                .append(type)
                .toHashCode();
    }
}
