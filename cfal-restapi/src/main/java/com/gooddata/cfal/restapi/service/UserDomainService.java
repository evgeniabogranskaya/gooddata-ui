/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import com.gooddata.c4.C4SomethingNotFoundException;
import com.gooddata.c4.domain.C4Domain;
import com.gooddata.c4.domain.C4DomainNotFoundException;
import com.gooddata.c4.domain.DomainService;
import com.gooddata.c4.setting.C4SettingEntry;
import com.gooddata.c4.user.C4User;
import com.gooddata.c4.user.C4UserNotFoundException;
import com.gooddata.c4.user.UserService;
import com.gooddata.cfal.restapi.dto.UserInfo;
import com.gooddata.cfal.restapi.exception.AuditLogNotEnabledException;
import com.gooddata.cfal.restapi.exception.DomainNotFoundException;
import com.gooddata.cfal.restapi.exception.UserNotDomainAdminException;
import com.gooddata.cfal.restapi.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Service for management of users and domains
 */
@Service
public class UserDomainService {

    private static final Logger logger = LoggerFactory.getLogger(UserDomainService.class);

    private static final String SETTING_NAME = "cfal";

    private final UserService userService;
    private final DomainService domainService;

    public UserDomainService(final UserService userService, final DomainService domainService) {
        this.userService = notNull(userService, "userService cannot be null");
        this.domainService = notNull(domainService, "domainService cannot be null");
    }

    /**
     * Get info about user
     *
     * @param userId ID of user
     * @return ID of domain for user
     */
    public UserInfo getUserInfo(final String userId) {
        notEmpty(userId, "userId cannot be empty");

        logger.info("action=get_user_info status=start user_id={}", userId);

        try {
            final C4User user = userService.getUser(userId);
            final String domain = C4Domain.DOMAIN_URI_TEMPLATE.match(user.getDomainUri()).get("id");

            final UserInfo userInfo = new UserInfo(userId, user.getLogin(), domain);
            logger.info("action=get_user_info status=finished user_id={}", userId);

            return userInfo;
        } catch (C4UserNotFoundException ex) {
            throw new UserNotFoundException("user with ID " + userId + " not found", ex);
        }
    }

    /**
     * Checks if user is admin of domain
     *
     * @param userId   ID of user
     * @param domainId ID of domain
     * @throws UserNotDomainAdminException when user is not admin
     */
    public void authorizeAdmin(final String userId, final String domainId) {
        notEmpty(userId, "userId cannot be empty");
        notEmpty(domainId, "domainId cannot be empty");

        logger.info("action=authorize_admin status=start domain={} user_id={}",
                domainId, userId);

        if (!isUserDomainAdmin(userId, domainId)) {
            logger.error("action=authorize_admin status=error domain={} user_id={}",
                    domainId, userId);
            throw new UserNotDomainAdminException("user with ID " + userId + " is not admin of domain with ID " + domainId);
        }

        logger.info("action=authorize_admin status=finished domain={} user_id={}",
                domainId, userId);
    }

    /**
     * Checks if user is admin of domain
     *
     * @param userId ID of user
     * @param domainId ID of domain
     * @return true if user is domain admin, else false
     */
    public boolean isUserDomainAdmin(final String userId, final String domainId) {
        notEmpty(userId, "userId cannot be empty");
        notEmpty(domainId, "domainId cannot be empty");

        logger.info("action=is_user_domain_admin status=start domain={} user_id={}", domainId, userId);

        try {
            final C4Domain domain = domainService.getDomain(domainId);
            final boolean isAdmin = C4User.uri2id(domain.getOwner()).equals(userId);

            logger.info("action=is_user_domain_admin status=finished domain={} user_id={} is_admin={}",
                    domainId, userId, isAdmin);

            return isAdmin;
        } catch (C4DomainNotFoundException ex) {
            throw new DomainNotFoundException("domain with ID " + domainId + " not found", ex);
        }
    }

    /**
     * Ensure CFAL is enabled for the given user
     * @param userId user
     * @throws AuditLogNotEnabledException
     */
    public void ensureFeatureEnabled(final String userId) {
        notEmpty(userId, "userId cannot be empty");
        try {
            final C4SettingEntry setting = userService.getSetting(userId, SETTING_NAME);
            if (!Boolean.parseBoolean(setting.getValue())) {
                throw new AuditLogNotEnabledException("Audit log is not enabled for user " + userId);
            }
        } catch (C4SomethingNotFoundException e) {
            throw new AuditLogNotEnabledException("Audit log is not enabled for user " + userId, e);
        }
    }
}
