/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import com.gooddata.account.Account;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.AbstractAT;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.function.Predicate;

public class AccountChangeAT extends AbstractAT {

    private final String USER_PROFILE_CHANGE = "USER_PROFILE_CHANGE";
    private final String USER_PASSWORD_CHANGE = "USER_PASSWORD_CHANGE";
    private final String USER_IP_WHITELIST_CHANGE = "USER_IP_WHITELIST_CHANGE";

    @BeforeClass(groups = {USER_PROFILE_CHANGE, USER_PASSWORD_CHANGE, USER_IP_WHITELIST_CHANGE})
    public void setUp() {
        updateUser();
    }

    private void updateUser() {
        final Account anotherAccount = accountHelper.getOrCreateUser();

        final String newPass = "alohaalohaaloha";
        anotherAccount.setPassword(newPass);
        anotherAccount.setVerifyPassword(newPass);

        anotherAccount.setFirstName("Petr");

        anotherAccount.setIpWhitelist(Arrays.asList("127.0.0.1/32"));

        gd.getAccountService().updateAccount(anotherAccount);

        logger.info("updated user_id={}", anotherAccount.getId());
    }

    @Test(groups = USER_PROFILE_CHANGE)
    public void testUserProfileChangeMessageUserApi() {
        doTestUserApi(eventCheck(USER_PROFILE_CHANGE), USER_PROFILE_CHANGE);
    }

    @Test(groups = USER_PROFILE_CHANGE)
    public void testUserProfileChangeMessageAdminApi() {
        doTestAdminApi(eventCheck(USER_PROFILE_CHANGE), USER_PROFILE_CHANGE);
    }

    @Test(groups = USER_PASSWORD_CHANGE)
    public void testUserPasswordChangeMessageUserApi() {
        doTestUserApi(eventCheck(USER_PASSWORD_CHANGE), USER_PASSWORD_CHANGE);
    }

    @Test(groups = USER_PASSWORD_CHANGE)
    public void testUserPasswordChangeMessageAdminApi() {
        doTestAdminApi(eventCheck(USER_PASSWORD_CHANGE), USER_PASSWORD_CHANGE);
    }

    @Test(groups = USER_IP_WHITELIST_CHANGE)
    public void testUserIpWhitelistChangeMessageUserApi() {
        doTestUserApi(eventCheck(USER_IP_WHITELIST_CHANGE), USER_IP_WHITELIST_CHANGE);
    }

    @Test(groups = USER_IP_WHITELIST_CHANGE)
    public void testUserIpWhitelistChangeMessageAdminApi() {
        doTestAdminApi(eventCheck(USER_IP_WHITELIST_CHANGE), USER_IP_WHITELIST_CHANGE);
    }

    private Predicate<AuditEventDTO> eventCheck(final String messageType) {
        return (e -> e.getUserLogin().equals(getAccount().getLogin()) && e.getType().equals(messageType));
    }
}
