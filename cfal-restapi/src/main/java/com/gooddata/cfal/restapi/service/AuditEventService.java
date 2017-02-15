/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.model.AuditEvent;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI_TEMPLATE;
import static com.gooddata.cfal.restapi.dto.AuditEventDTO.USER_URI_TEMPLATE;
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

        //Limit is incremented to check if list returned from database is last page or not.
        final RequestParameters parametersForRepository = requestParameters.withIncrementedLimit();

        //find up to (requestParameters.getSanitizedLimit + 1) records, which match requestParameters. +1 to check if list is last page.
        final List<AuditEvent> list = auditLogEventRepository.findByDomain(domain, parametersForRepository);

        final String baseUri = ADMIN_URI_TEMPLATE.expand(domain).toString();

        final AuditEventsDTO auditEventDTOs = createAuditEventsDTO(baseUri, list, requestParameters);

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

        //Limit is incremented to check if list returned from database is last page or not.
        final RequestParameters parametersForRepository = requestParameters.withIncrementedLimit();

        //find up to (requestParameters.getSanitizedLimit + 1) records, which match requestParameters. +1 to check if list is last page.
        final List<AuditEvent> list = auditLogEventRepository.findByDomainAndUser(domain, userId, parametersForRepository);

        final String baseUri = USER_URI_TEMPLATE.expand(userId).toString();

        final AuditEventsDTO auditEventDTOs = createAuditEventsDTO(baseUri, list, requestParameters);

        logger.info("action=find_by_domain_and_user status=finished domain={} user_id={} offset={} limit={} from={} to={} entries_on_page={}",
                domain, userId, requestParameters.getOffset(), requestParameters.getLimit(), requestParameters.getFrom(), requestParameters.getTo(), auditEventDTOs.size());

        return auditEventDTOs;
    }
}
