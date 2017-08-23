/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import com.codahale.metrics.Timer;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.dto.UserInfo;
import com.gooddata.cfal.restapi.model.AuditEvent;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import com.gooddata.commons.monitoring.metrics.Measure;
import com.gooddata.commons.monitoring.metrics.Monitored;
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
@Monitored("cfal.AuditEventService")
public class AuditEventService {

    private static final Logger logger = LoggerFactory.getLogger(AuditEventService.class);

    private final AuditLogEventRepository auditLogEventRepository;

    private final Timer findByDomainTimer = new Timer();

    private final Timer findByUserTimer = new Timer();

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

        final Timer.Context time = findByDomainTimer.time();

        try {
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
        } finally {
            time.stop();
        }
    }

    /**
     * Finds all events for domain and user within given time interval, result is paged list
     *
     * @param userInfo  to find events for
     *@param requestParameters specifies time range for finding events and paging parameters  @return paged list
     */
    public AuditEventsDTO findByUser(final UserInfo userInfo, final RequestParameters requestParameters) {
        notNull(userInfo, "userInfo cannot be null");
        notNull(requestParameters, "requestParameters cannot be null");

        final Timer.Context time = findByUserTimer.time();

        try {
            logger.info("action=find_by_domain_and_user status=start domain={} user_id={} user_login={} offset={} limit={} from={} to={}",
                    userInfo.getDomainId(), userInfo.getUserId(), userInfo.getUserLogin(), requestParameters.getOffset(), requestParameters.getLimit(), requestParameters.getFrom(), requestParameters.getTo());

            //Limit is incremented to check if list returned from database is last page or not.
            final RequestParameters parametersForRepository = requestParameters.withIncrementedLimit();

            //find up to (requestParameters.getSanitizedLimit + 1) records, which match requestParameters. +1 to check if list is last page.
            final List<AuditEvent> list = auditLogEventRepository.findByUser(userInfo, parametersForRepository);

            final String baseUri = USER_URI_TEMPLATE.expand(userInfo.getUserId()).toString();

            final AuditEventsDTO auditEventDTOs = createAuditEventsDTO(baseUri, list, requestParameters);

            logger.info("action=find_by_domain_and_user status=finished domain={} user_id={} user_login={} offset={} limit={} from={} to={} entries_on_page={}",
                    userInfo.getDomainId(), userInfo.getUserId(), userInfo.getUserLogin(), requestParameters.getOffset(), requestParameters.getLimit(), requestParameters.getFrom(), requestParameters.getTo(), auditEventDTOs.size());

            return auditEventDTOs;
        } finally {
            time.stop();
        }
    }

    @Measure("find.by.domain.time")
    public Timer getFindByDomainTimer() {
        return findByDomainTimer;
    }

    @Measure("find.by.user.time")
    public Timer getFindByUserTimer() {
        return findByUserTimer;
    }
}
