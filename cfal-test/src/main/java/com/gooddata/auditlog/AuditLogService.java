/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.AbstractService;
import com.gooddata.account.Account;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.collections.Page;
import com.gooddata.collections.PageableList;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static com.gooddata.util.Validate.notEmpty;
import static com.gooddata.util.Validate.notNull;

/**
 * List audit events. To be moved into SDK.
 */
public class AuditLogService extends AbstractService {

    public AuditLogService(final RestTemplate restTemplate) {
        super(restTemplate);
    }

    /**
     * Get list of audit events for the given domain id
     * @param domainId domain id
     * @return non-null paged list of events
     */
    public PageableList<AuditEventDTO> listAuditEvents(final String domainId) {
        return listAuditEvents(domainId, new RequestParameters());
    }

    /**
     * Get list of audit events for the given domain id
     * @param domainId domain id
     * @param page request parameters
     * @return non-null paged list of events
     */
    public PageableList<AuditEventDTO> listAuditEvents(final String domainId, final Page page) {
        notEmpty(domainId, "domainId");
        notNull(page, "page");

        final String expandedUri = AuditEventDTO.ADMIN_URI_TEMPLATE.expand(domainId).toString();
        final String uri = page.updateWithPageParams(UriComponentsBuilder.fromUriString(expandedUri)).build().toUriString();

        return restTemplate.getForObject(uri, AuditEventsDTO.class);
    }

    /**
     * Get list of audit events for the given account
     * @param account account with valid id
     * @return non-null paged list of events
     */
    public PageableList<AuditEventDTO> listAuditEvents(final Account account) {
        return listAuditEvents(account, new RequestParameters());
    }

    /**
     * Get list of audit events for the given account
     * @param account account with valid id
     * @param page request parameters
     * @return non-null paged list of events
     */
    public PageableList<AuditEventDTO> listAuditEvents(final Account account, final Page page) {
        notNull(account, "account");
        notEmpty(account.getId(), "account.id");
        notNull(page, "page");

        final String expandedUri = AuditEventDTO.USER_URI_TEMPLATE.expand(account.getId()).toString();
        final String uri = page.updateWithPageParams(UriComponentsBuilder.fromUriString(expandedUri)).build().toUriString();

        return restTemplate.getForObject(uri, AuditEventsDTO.class);
    }
}
