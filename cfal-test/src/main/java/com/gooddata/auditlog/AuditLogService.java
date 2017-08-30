/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.AbstractService;
import com.gooddata.GoodDataRestException;
import com.gooddata.account.Account;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.collections.MultiPageList;
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
     * @throws GoodDataRestException is something wents wrong
     */
    public PageableList<AuditEventDTO> listAuditEvents(final String domainId) {
        return listAuditEvents(domainId, new RequestParameters());
    }

    /**
     * Get list of audit events for the given domain id
     * @param domainId domain id
     * @param page request parameters
     * @return non-null paged list of events
     * @throws GoodDataRestException is something wents wrong
     */
    public PageableList<AuditEventDTO> listAuditEvents(final String domainId, final Page page) {
        notEmpty(domainId, "domainId");
        notNull(page, "page");

        final String uri = AuditEventDTO.ADMIN_URI_TEMPLATE.expand(domainId).toString();
        return new MultiPageList<>(page, (p) -> doListAuditEvents(getAuditEventsUri(p, uri)));
    }

    /**
     * Get list of audit events for the given account
     * @param account account with valid id
     * @return non-null paged list of events
     * @throws GoodDataRestException is something wents wrong
     */
    public PageableList<AuditEventDTO> listAuditEvents(final Account account) {
        return listAuditEvents(account, new RequestParameters());
    }

    /**
     * Get list of audit events for the given account
     * @param account account with valid id
     * @param page request parameters
     * @return non-null paged list of events
     * @throws GoodDataRestException is something wents wrong
     */
    public PageableList<AuditEventDTO> listAuditEvents(final Account account, final Page page) {
        notNull(account, "account");
        notEmpty(account.getId(), "account.id");
        notNull(page, "page");

        final String uri = AuditEventDTO.USER_URI_TEMPLATE.expand(account.getId()).toString();

        return new MultiPageList<>(page, (p) -> doListAuditEvents(getAuditEventsUri(p, uri)));
    }

    private AuditEventsDTO doListAuditEvents(final String uri) {
        return restTemplate.getForObject(uri, AuditEventsDTO.class);
    }

    private String getAuditEventsUri(final Page page, final String uri) {
        return page.updateWithPageParams(UriComponentsBuilder.fromUriString(uri)).build().toUriString();
    }
}
