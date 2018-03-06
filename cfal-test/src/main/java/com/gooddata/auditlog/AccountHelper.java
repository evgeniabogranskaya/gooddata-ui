/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.CfalGoodData;
import com.gooddata.GoodData;
import com.gooddata.account.Account;
import com.gooddata.registration.RegisteredAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Singleton for account related stuff.
 * Lazy initialized. Not thread safe.
 */
public class AccountHelper {

    private static final Logger logger = LoggerFactory.getLogger(AccountHelper.class);

    private static AccountHelper instance;

    private final List<Account> accounts = new ArrayList<>();

    private final CfalGoodData gd;

    private final TestEnvironmentProperties props;

    private Account currentAccount;

    public static AccountHelper getInstance() {
        if (instance == null) {
            final TestEnvironmentProperties props = TestEnvironmentProperties.getInstance();
            final CfalGoodData gd = CfalGoodData.getInstance();
            instance = new AccountHelper(gd, props);
        }
        return instance;
    }

    private AccountHelper(final CfalGoodData gd, final TestEnvironmentProperties props) {
        this.gd = notNull(gd, "gd");
        this.props = notNull(props, "props");
    }

    /**
     * Get current account
     * @return current account
     */
    public Account getCurrentAccount() {
        if (currentAccount == null) {
            currentAccount = gd.getAccountService().getCurrent();
        }
        return currentAccount;
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
        final Account account = gd.getAccountService().createAccount(createRandomAccount(), props.getDomain());
        logger.info("created user_id={}", account.getId());

        accounts.add(account);
        return account;
    }

    /**
     * Registers new account and deletes it immediately to prevent piling up of unverified accounts.
     */
    public void registerAndDeleteUser(final Account account) {
        final RegisteredAccount newAccount = gd.getAccountService().registerAccount(account);
        logger.info("registered user_uri={}", newAccount.getProfileUri());

        final GoodData sstGd = new CfalGoodData(gd.getEndpoint(), newAccount.getSst());
        final Account current = sstGd.getAccountService().getCurrent();
        sstGd.getAccountService().removeAccount(current);
        logger.info("deleted registered user_id={}", current.getId());
    }

    /**
     * remove all created users
     */
    public void destroy() {
        accounts.forEach(account -> {
            try {
                logger.info("removing account_id={}", account.getId());
                gd.getAccountService().removeAccount(account);
                logger.info("account_id={} removed", account.getId());
            } catch (Exception ex) {
                logger.warn("could not remove account_id=" + account.getId(), ex);
            }
        });
        accounts.clear();
    }

    /**
     * Creates and returns new random generated account
     */
    public Account createRandomAccount() {
        return new Account(UUID.randomUUID() + "@mail.com", "passpasspass", "hugo", "boss");
    }

}
