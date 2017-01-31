/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.exception.InvalidOffsetException;
import com.gooddata.cfal.restapi.model.AuditEvent;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import com.gooddata.collections.PageRequest;
import com.gooddata.collections.Paging;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AuditEventService.class);

    private final AuditLogEventRepository auditLogEventRepository;

    @Autowired
    public AuditEventService(final AuditLogEventRepository auditLogEventRepository) {
        this.auditLogEventRepository = notNull(auditLogEventRepository, "auditEventRepository cannot be null");
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

        final ObjectId offsetId = getObjectIdFromOffset(pageReq);

        logger.info("action=find_by_domain status=start domain={} offset={} limit={}",
                domain, pageReq.getOffset(), pageReq.getLimit());

        final List<AuditEvent> list = auditLogEventRepository.findByDomain(domain, pageReq.getLimit(), offsetId);

        final AuditEventsDTO auditEventDTOs = getAuditEventDTOs(pageReq, list, ADMIN_URI);

        logger.info("action=find_by_domain status=finished domain={} offset={} limit={} entries_on_page={}",
                domain, pageReq.getOffset(), pageReq.getLimit(), auditEventDTOs.size());

        return auditEventDTOs;
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

        final ObjectId offsetId = getObjectIdFromOffset(pageReq);

        logger.info("action=find_by_domain_and_user status=start domain={} user_id={} offset={} limit={}",
                domain, userId, pageReq.getOffset(), pageReq.getLimit());

        final List<AuditEvent> list = auditLogEventRepository
                .findByDomainAndUser(domain, userId, pageReq.getSanitizedLimit(), offsetId);

        final AuditEventsDTO auditEventDTOs = getAuditEventDTOs(pageReq, list, USER_URI);

        logger.info("action=find_by_domain_and_user status=finished domain={} user_id={} offset={} limit={} entries_on_page={}",
                domain, userId, pageReq.getOffset(), pageReq.getLimit(), auditEventDTOs.size());

        return auditEventDTOs;
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

    private ObjectId getObjectIdFromOffset(final PageRequest pageReq) {
        try {
            return pageReq.getOffset() == null ? null : new ObjectId(pageReq.getOffset());
        } catch (IllegalArgumentException ex) {
            throw new InvalidOffsetException("Invalid offset " + pageReq.getOffset(), ex);
        }
    }
}
