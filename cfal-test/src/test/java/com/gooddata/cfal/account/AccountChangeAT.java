/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.test.AbstractAT;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class AccountChangeAT extends AbstractAT {

    private final String USER_PROFILE_CHANGE = "USER_PROFILE_CHANGE";
    private final String USER_PASSWORD_CHANGE = "USER_PASSWORD_CHANGE";
    private final String USER_IP_WHITELIST_CHANGE = "USER_IP_WHITELIST_CHANGE";

    @BeforeClass
    public void setUp() {
        updateUser();
    }

    private void updateUser() {
        final String newPass = "alohaalohaaloha";
        anotherAccount.setPassword(newPass);
        anotherAccount.setVerifyPassword(newPass);

        anotherAccount.setFirstName("Petr");

        anotherAccount.setIpWhitelist(Arrays.asList("127.0.0.1/32"));

        gd.getAccountService().updateAccount(anotherAccount);

        logger.info("updated user_id={}", anotherAccount.getId());
    }

    @Test(groups = USER_PROFILE_CHANGE)
    public void testUserProfileChangeMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(USER_PROFILE_CHANGE), USER_PROFILE_CHANGE);
    }

    @Test(groups = USER_PROFILE_CHANGE)
    public void testUserProfileChangeMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(USER_PROFILE_CHANGE), USER_PROFILE_CHANGE);
    }

    @Test(groups = USER_PASSWORD_CHANGE)
    public void testUserPasswordChangeMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(USER_PASSWORD_CHANGE), USER_PASSWORD_CHANGE);
    }

    @Test(groups = USER_PASSWORD_CHANGE)
    public void testUserPasswordChangeMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(USER_PASSWORD_CHANGE), USER_PASSWORD_CHANGE);
    }

    @Test(groups = USER_IP_WHITELIST_CHANGE)
    public void testUserIpWhitelistChangeMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(USER_IP_WHITELIST_CHANGE), USER_IP_WHITELIST_CHANGE);
    }

    @Test(groups = USER_IP_WHITELIST_CHANGE)
    public void testUserIpWhitelistChangeMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(USER_IP_WHITELIST_CHANGE), USER_IP_WHITELIST_CHANGE);
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final String messageType) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(messageType));
    }
}
