/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import static org.apache.commons.lang3.Validate.notEmpty;

/**
 * Info about user
 */
public class UserInfo {
    private final String userId;
    private final String userLogin;
    private final String domainId;

    public UserInfo(final String userId, final String userLogin, final String domainId) {
        this.userId = notEmpty(userId, "userId cannot be empty");
        this.userLogin = notEmpty(userLogin, "userLogin cannot be empty");
        this.domainId = notEmpty(domainId, "domainId cannot be empty");
    }

    public String getUserLogin() {
        return userLogin;
    }

    public String getUserId() {
        return userId;
    }

    public String getDomainId() {
        return domainId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final UserInfo userInfo = (UserInfo) o;

        if (!userId.equals(userInfo.userId))
            return false;
        if (!userLogin.equals(userInfo.userLogin))
            return false;
        return domainId.equals(userInfo.domainId);

    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + userLogin.hashCode();
        result = 31 * result + domainId.hashCode();
        return result;
    }
}
