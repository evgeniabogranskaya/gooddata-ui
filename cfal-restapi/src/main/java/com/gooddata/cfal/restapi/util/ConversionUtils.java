/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import static com.gooddata.cfal.restapi.util.PagingUtils.createPaging;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.Validate.notNull;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.model.AuditEvent;
import com.gooddata.collections.Paging;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;

public class ConversionUtils {

    /**
     * Create AuditEventsDTO
     *
     * @param list of audit events
     * @param baseUri for next link construction
     * @param requestParameters for next link construction
     * @return AuditEventsDTO
     */
    public static AuditEventsDTO createAuditEventsDTO(final List<AuditEvent> list, final String baseUri, final RequestParameters requestParameters) {
        notNull(list, "list cannot be null");
        notNull(baseUri, "baseUri cannot be null");
        notNull(requestParameters, "requestParameters cannot be null");

        final String offset = getOffset(list);

        Paging paging = createPaging(baseUri, requestParameters, offset);

        final List<AuditEventDTO> listDTOs = list
                .stream()
                .map(ConversionUtils::createAuditEventDTO)
                .collect(toList());

        return new AuditEventsDTO(listDTOs, paging, singletonMap("self", baseUri));
    }

    /**
     * Create <i>AuditEventDTO</i> instance from <i>AuditEvent</i> instance
     */
    public static AuditEventDTO createAuditEventDTO(final AuditEvent auditEvent) {
        notNull(auditEvent, "auditEvent cannot be null");

        return new AuditEventDTO(auditEvent.getId().toString(),
                auditEvent.getDomain(),
                auditEvent.getUserId(),
                auditEvent.getRealTimeOccurrence(),
                new DateTime(auditEvent.getId().getDate(), DateTimeZone.UTC));
    }

    /**
     * Get new offset based on list or if list is empty returns null
     */
    private static String getOffset(final List<AuditEvent> list) {
        if (!list.isEmpty()) {
            return list.get(list.size() - 1).getId().toString(); //last element's ID is offset for next page
        }

        return null;
    }
}
