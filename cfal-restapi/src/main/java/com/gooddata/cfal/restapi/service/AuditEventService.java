/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.exception.OffsetAndFromSpecifiedException;
import com.gooddata.cfal.restapi.model.AuditEvent;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI;
import static com.gooddata.cfal.restapi.dto.AuditEventDTO.USER_URI;
import static com.gooddata.cfal.restapi.util.ConversionUtils.createAuditEventsDTO;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

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
     * Finds all events for domain within given time interval, result is paged list.
     *
     * @param domain  to find events for
     * @param requestParameters specifies time range to find events for and paging parameters
     * @return paged list
     */
    public AuditEventsDTO findByDomain(final String domain, final RequestParameters requestParameters) {
        notEmpty(domain, "domain cannot be empty");
        notNull(requestParameters, "requestParameters cannot be null");

        logger.info("action=find_by_domain status=start domain={} offset={} limit={} from={} to={}",
                domain, requestParameters.getOffset(), requestParameters.getLimit(), requestParameters.getFrom(), requestParameters.getTo());

        final List<AuditEvent> list = auditLogEventRepository.findByDomain(domain, requestParameters);

        final AuditEventsDTO auditEventDTOs = createAuditEventsDTO(list, ADMIN_URI, requestParameters);

        logger.info("action=find_by_domain status=finished domain={} offset={} limit={} from={} to={} entries_on_page={}",
                domain, requestParameters.getOffset(), requestParameters.getLimit(), requestParameters.getFrom(), requestParameters.getTo(), auditEventDTOs.size());

        return auditEventDTOs;
    }

    /**
     * Finds all events for domain and user within given time interval, result is paged list
     *
     * @param domain  to find events for
     * @param userId  to find events for
     * @param requestParameters specifies time range for finding events and paging parameters
     * @return paged list
     */
    public AuditEventsDTO findByDomainAndUser(final String domain, final String userId, final RequestParameters requestParameters) {
        notEmpty(domain, "domain cannot be empty");
        notEmpty(userId, "userId cannot be empty");
        notNull(requestParameters, "requestParameters cannot be null");

        logger.info("action=find_by_domain_and_user status=start domain={} user_id={} offset={} limit={} from={} to={}",
                domain, userId, requestParameters.getOffset(), requestParameters.getLimit(), requestParameters.getFrom(), requestParameters.getTo());

        final List<AuditEvent> list = auditLogEventRepository.findByDomainAndUser(domain, userId, requestParameters);

        final AuditEventsDTO auditEventDTOs = createAuditEventsDTO(list, USER_URI, requestParameters);

        logger.info("action=find_by_domain_and_user status=finished domain={} user_id={} offset={} limit={} from={} to={} entries_on_page={}",
                domain, userId, requestParameters.getOffset(), requestParameters.getLimit(), requestParameters.getFrom(), requestParameters.getTo(), auditEventDTOs.size());

        return auditEventDTOs;
    }
}
