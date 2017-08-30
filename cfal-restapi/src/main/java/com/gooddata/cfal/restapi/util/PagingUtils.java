/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import static org.apache.commons.lang3.Validate.notNull;

import com.gooddata.auditevent.AuditEventPageRequest;
import com.gooddata.collections.PageRequest;
import com.gooddata.collections.Paging;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class PagingUtils {

    /**
     * Create paging
     *
     * @param baseUri to be used to build next uri
     * @param requestParameters to be used to build next uri
     * @param offsetOfNextPage to be used to build next uri, can be null
     * @return Paging for given parameters
     */
    public static Paging createPaging(final String baseUri, final AuditEventPageRequest requestParameters, final String offsetOfNextPage) {
        notNull(baseUri, "baseUri cannot be null");
        notNull(requestParameters, "requestParameters cannot be null");

        if (offsetOfNextPage == null) {
            return new Paging(null);
        }

        final UriComponentsBuilder uriWithTimeIntervalParams = constructUriWithTimeIntervalParam(baseUri, requestParameters);
        if (requestParameters.getType() != null) {
            uriWithTimeIntervalParams.query("type=" + requestParameters.getType());
        }
        return new Paging(offsetOfNextPage, new PageRequest(offsetOfNextPage, requestParameters.getSanitizedLimit()).getPageUri(uriWithTimeIntervalParams).toString());
    }

    private static UriComponentsBuilder constructUriWithTimeIntervalParam(final String baseUri, final AuditEventPageRequest requestParameters) {
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(baseUri);
        if (requestParameters.getTo() != null) {
            uriComponentsBuilder.query("to=" + requestParameters.getTo());
        }
        return uriComponentsBuilder;
    }
}
