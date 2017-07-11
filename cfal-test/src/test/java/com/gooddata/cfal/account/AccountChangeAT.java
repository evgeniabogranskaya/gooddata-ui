/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import com.gooddata.account.Account;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.test.AbstractAT;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class AccountChangeAT extends AbstractAT {

    private final String USER_PROFILE_CHANGE = "USER_PROFILE_CHANGE";
    private final String USER_PASSWORD_CHANGE = "USER_PASSWORD_CHANGE";
    private final String USER_IP_WHITELIST_CHANGE = "USER_IP_WHITELIST_CHANGE";

    private Account accountToUpdate;

    @BeforeClass
    public void setUp() {
        createUser();
        updateUser();
    }

    private void createUser() {
        final String email = UUID.randomUUID() + "@mail.com";
        final String password = "passpasspass";
        final String firstName = "hugo";
        final String lastName = "boss";
        accountToUpdate = gd.getAccountService().createAccount(new Account(email, password, firstName, lastName), props.getDomain());

        logger.info("created user_id={}", accountToUpdate.getId());
    }

    private void updateUser() {
        final String newPass = "alohaalohaaloha";
        accountToUpdate.setPassword(newPass);
        accountToUpdate.setVerifyPassword(newPass);

        accountToUpdate.setFirstName("Petr");

        accountToUpdate.setIpWhitelist(Arrays.asList("127.0.0.1/32"));

        gd.getAccountService().updateAccount(accountToUpdate);

        logger.info("updated user_id={}", accountToUpdate.getId());
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

    @AfterClass
    public void tearDown() {
        if (accountToUpdate != null) {
            gd.getAccountService().removeAccount(accountToUpdate);
        }
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final String messageType) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(messageType));
    }
}
