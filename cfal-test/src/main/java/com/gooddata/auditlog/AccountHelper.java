/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.CfalGoodData;
import com.gooddata.GoodData;
import com.gooddata.account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Singleton for account related stuff.
 * Lazy initialized. Not thread safe.
 */
public class AccountHelper {

    private static final Logger logger = LoggerFactory.getLogger(AccountHelper.class);

    private static AccountHelper instance;

    private final List<Account> accounts = new ArrayList<>();

    private final GoodData gd;

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

    private AccountHelper(final GoodData gd, final TestEnvironmentProperties props) {
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
        final Account create = new Account(UUID.randomUUID() + "@mail.com", "passpasspass", "hugo", "boss");

        final Account account = gd.getAccountService().createAccount(create, props.getDomain());
        logger.info("created user_id={}", account.getId());

        accounts.add(account);
        return account;
    }

    /**
     * remove all created users
     */
    public void destroy() {
        accounts.forEach(e -> {
            try {
                gd.getAccountService().removeAccount(e);
                logger.info("removed account_id={}", e.getId());
            } catch (Exception ex) {
                logger.warn("could not remove account_id={}", e.getId());
            }
        });
    }
}
