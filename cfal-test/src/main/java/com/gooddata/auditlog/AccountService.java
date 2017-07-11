/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.GoodData;
import com.gooddata.account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final GoodData gd;
    private final TestEnvironmentProperties props;

    public AccountService(final GoodData gd, final TestEnvironmentProperties props) {
        this.gd = gd;
        this.props = props;
    }

    /**
     * Creates user for testing purposes
     * @return created user
     */
    public Account createUser() {
        final String email = UUID.randomUUID() + "@mail.com";
        final String password = "passpasspass";
        final String firstName = "hugo";
        final String lastName = "boss";
        final Account account = gd.getAccountService().createAccount(new Account(email, password, firstName, lastName), props.getDomain());

        logger.info("created user_id={}", account.getId());

        return account;
    }
}
