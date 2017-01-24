/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.exception.InvalidOffsetException;
import com.gooddata.cfal.restapi.model.AuditEvent;
import com.gooddata.cfal.restapi.repository.AuditEventRepository;
import com.gooddata.collections.PageRequest;
import com.gooddata.collections.Paging;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI;
import static com.gooddata.cfal.restapi.dto.AuditEventDTO.USER_URI;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;
import static org.springframework.web.util.UriComponentsBuilder.fromUri;

/**
 * Service for management of audit events
 */
@Service
public class AuditEventService {

    private AuditEventRepository auditEventRepository;

    @Autowired
    public AuditEventService(final AuditEventRepository auditEventRepository) {
        this.auditEventRepository = notNull(auditEventRepository, "auditEventRepository cannot be null");
    }

    /**
     * Finds all events for domain, result is paged list
     *
     * @param domain  to find events for
     * @param pageReq paging parameter, finds events younger (greater ID) than offset ID (if not null)
     * @return paged list
     */
    public AuditEventsDTO findByDomain(final String domain, final PageRequest pageReq) {
        notEmpty(domain, "domain cannot be empty");
        notNull(pageReq, "pageReq cannot be null");

        final ObjectId offsetId = getObjectId(pageReq);
        final List<AuditEvent> list = auditEventRepository.findByDomain(domain, pageReq.getLimit(), offsetId);

        return getAuditEventDTOs(pageReq, list, ADMIN_URI);
    }

    /**
     * Finds all events for domain and user, result is paged list
     *
     * @param domain  to find events for
     * @param userId  to find events for
     * @param pageReq paging parameter, finds events younger (greater ID) than offset ID (if not null)
     * @return paged list
     */
    public AuditEventsDTO findByDomainAndUser(final String domain, final String userId, final PageRequest pageReq) {
        notEmpty(domain, "domain cannot be empty");
        notEmpty(userId, "userId cannot be empty");
        notNull(pageReq, "pageReq cannot be null");

        final ObjectId offsetId = getObjectId(pageReq);
        final List<AuditEvent> list = auditEventRepository
                .findByDomainAndUser(domain, userId, pageReq.getSanitizedLimit(), offsetId);

        return getAuditEventDTOs(pageReq, list, USER_URI);
    }

    private AuditEventsDTO getAuditEventDTOs(final PageRequest pageReq, final List<AuditEvent> list, final String baseUri) {
        final String offset = getOffset(pageReq, list);

        final Paging paging = new Paging(new PageRequest(offset, pageReq.getSanitizedLimit()).getPageUri(fromUri(URI.create(baseUri))).toString());

        final List<AuditEventDTO> listDTOs = list
                .stream()
                .map(e -> new AuditEventDTO(e.getId().toString(), e.getDomain(), e.getUserId(), e.getTimestamp()))
                .collect(toList());

        return new AuditEventsDTO(listDTOs, paging, singletonMap("self", baseUri));
    }

    private String getOffset(final PageRequest pageReq, final List<AuditEvent> list) {
        if (!list.isEmpty()) {
            return list.get(list.size() - 1).getId().toString(); //last element's ID is offset for next page
        }
        return pageReq.getOffset();
    }

    private ObjectId getObjectId(final PageRequest pageReq) {
        try {
            return pageReq.getOffset() == null ? null : new ObjectId(pageReq.getOffset());
        } catch (IllegalArgumentException ex) {
            throw new InvalidOffsetException("Invalid offset " + pageReq.getOffset(), ex);
        }
    }
}
