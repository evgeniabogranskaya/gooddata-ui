/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.GoodData;
import com.gooddata.account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Singleton for account related stuff. Not thread safe.
 */
public class AccountService {

    private static AccountService instance;

    private List<Account> accounts = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final GoodData gd;
    private final TestEnvironmentProperties props;

    public static AccountService getInstance(final GoodData gd, final TestEnvironmentProperties props) {
        if (instance != null) {
            return instance;
        }
        return instance = new AccountService(gd, props);
    }

    private AccountService(final GoodData gd, final TestEnvironmentProperties props) {
        this.gd = gd;
        this.props = props;
    }

    /**
     * Gets user created before or creates it
     * @return Account instance
     */
    public Account getOrCreateUser() {
        if (accounts.isEmpty()) {
            return createUser();
        }
        return accounts.get(0);

    }

    /**
     * Creates new user for testing purposes
     * @return created user
     */
    public Account createUser() {
        final String email = UUID.randomUUID() + "@mail.com";
        final String password = "passpasspass";
        final String firstName = "hugo";
        final String lastName = "boss";
        final Account account = gd.getAccountService().createAccount(new Account(email, password, firstName, lastName), props.getDomain());

        logger.info("created user_id={}", account.getId());

        accounts.add(account);

        return account;
    }

    /**
     * remove all created users
     */
    public void destroy() {
        accounts.stream().forEach(e -> {
            try {
                gd.getAccountService().removeAccount(e);
                logger.info("removed account_id={}", e.getId());
            } catch (Exception ex) {
                logger.warn("could not remove account_id={}", e.getId());
            }
        });
    }
}
