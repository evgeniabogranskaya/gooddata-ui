/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import static com.gooddata.cfal.restapi.util.PagingUtils.createPaging;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.Validate.notNull;

import com.gooddata.auditevent.AuditEvent;
import com.gooddata.auditevent.AuditEvents;
import com.gooddata.auditevent.AuditEventPageRequest;
import com.gooddata.cfal.restapi.model.AuditEventEntity;
import com.gooddata.collections.Paging;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;

public abstract class ConversionUtils {

    /**
     * Create AuditEventsDTO
     *
     * @param baseUri for next link construction and self link
     * @param list of audit events, if list is empty then next uri is not generated in Paging,
     *             if there are more elements than requestParameters.getSanitizedLimit,
     *             then these elements will not be part of result list and Paging will be generated
     * @param requestParameters for next link construction
     * @return AuditEventsDTO
     */
    public static AuditEvents createAuditEventsDTO(final String baseUri, final List<AuditEventEntity> list, final AuditEventPageRequest requestParameters) {
        notNull(list, "list cannot be null");
        notNull(baseUri, "baseUri cannot be null");
        notNull(requestParameters, "requestParameters cannot be null");

        //indicator whether there is next page, if false next uri is not generated in Paging
        boolean hasNextPage = list.size() > requestParameters.getSanitizedLimit();

        final List<AuditEventEntity> auditEventsOnPage = list.size() <= requestParameters.getSanitizedLimit()? list : list.subList(0, requestParameters.getSanitizedLimit());

        final String offset = getOffset(auditEventsOnPage, hasNextPage);

        Paging paging = createPaging(baseUri, requestParameters, offset);

        final List<AuditEvent> listDTOs = auditEventsOnPage
                .stream()
                .map(ConversionUtils::createAuditEventDTO)
                .collect(toList());

        return new AuditEvents(listDTOs, paging, singletonMap("self", baseUri));
    }

    /**
     * Create <i>AuditEventDTO</i> instance from <i>AuditEvent</i> instance
     */
    public static AuditEvent createAuditEventDTO(final AuditEventEntity auditEvent) {
        notNull(auditEvent, "auditEvent cannot be null");

        return new AuditEvent(auditEvent.getId().toString(),
                auditEvent.getUserLogin(),
                auditEvent.getOccurred(),
                new DateTime(auditEvent.getId().getDate(), DateTimeZone.UTC),
                auditEvent.getUserIp(),
                auditEvent.isSuccess(),
                auditEvent.getType(),
                auditEvent.getParams(),
                auditEvent.getLinks());
    }

    /**
     * Get new offset based on list or if list is empty return null.
     * If <code>hasNextPage</code> is false, then return null.
     */
    private static String getOffset(final List<AuditEventEntity> list, boolean hasNextPage) {
        if(!hasNextPage){
            return null;
        }

        if (!list.isEmpty()) {
            return list.get(list.size() - 1).getId().toString(); //last element's ID is offset for next page
        }

        return null;
    }
}
