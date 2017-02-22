/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.AbstractService;
import com.gooddata.account.Account;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.collections.PageableList;
import org.springframework.web.client.RestTemplate;

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
        notEmpty(domainId, "domainId");
        return restTemplate.getForObject(AuditEventDTO.ADMIN_URI, AuditEventsDTO.class, domainId);
    }

    /**
     * Get list of audit events for the given account
     * @param account account with valid id
     * @return non-null paged list of events
     */
    public PageableList<AuditEventDTO> listAuditEvents(final Account account) {
        notNull(account, "account");
        notEmpty(account.getId(), "account.id");
        return restTemplate.getForObject(AuditEventDTO.USER_URI, AuditEventsDTO.class, account.getId());
    }
}
