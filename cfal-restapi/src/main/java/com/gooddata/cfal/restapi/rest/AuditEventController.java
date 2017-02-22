/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.rest;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.validation.RequestParametersValidator;
import com.gooddata.cfal.restapi.exception.UserNotAuthorizedException;
import com.gooddata.cfal.restapi.exception.UserNotSpecifiedException;
import com.gooddata.cfal.restapi.service.AuditEventService;
import com.gooddata.cfal.restapi.service.UserDomainService;
import com.gooddata.context.GdcCallContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.apache.commons.lang3.Validate.notNull;

import javax.validation.Valid;

/**
 * Audit event controller
 */
@RestController
public class AuditEventController {

    private final AuditEventService auditEventService;
    private final UserDomainService userDomainService;

    public AuditEventController(final AuditEventService auditEventService,
                                final UserDomainService userDomainService) {
        this.auditEventService = notNull(auditEventService, "auditEventService cannot be null");
        this.userDomainService = notNull(userDomainService, "userDomainService cannot be null");
    }

    @RequestMapping(path = AuditEventDTO.ADMIN_URI, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditEventsDTO listAuditEvents(@PathVariable String domainId, @Valid @ModelAttribute RequestParameters requestParameters) {

        final String userId = getUserIdFromContext();

        userDomainService.authorizeAdmin(userId, domainId);

        return auditEventService.findByDomain(domainId, requestParameters);
    }

    @RequestMapping(path = AuditEventDTO.USER_URI, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditEventsDTO listAuditEventsForUser(@PathVariable String userId, @Valid @ModelAttribute RequestParameters requestParameters) {

        final String loggedUserId = getUserIdFromContext();

        final String domainForUser = userDomainService.findDomainForUser(userId);

        if (userId.equals(loggedUserId)) {
            return auditEventService.findByDomainAndUser(domainForUser, userId, requestParameters);
        }

        final String domainForLoggedUser = userDomainService.findDomainForUser(loggedUserId);

        if (!domainForUser.equals(domainForLoggedUser) || !userDomainService.isUserDomainAdmin(loggedUserId, domainForLoggedUser)) {
            throw new UserNotAuthorizedException("user with ID" + loggedUserId + " is not authorized to access this resource");
        }

        return auditEventService.findByDomainAndUser(domainForUser, userId, requestParameters);
    }

    /**
     * register validator
     */
    @InitBinder
    protected void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.setValidator(new RequestParametersValidator());
    }

    private String getUserIdFromContext() {
        final GdcCallContext currentContext = GdcCallContext.getCurrentContext();
        final String userId = currentContext.getUserId();
        if (userId == null) {
            throw new UserNotSpecifiedException("User ID is not specified");
        }
        return userId;
    }
}
