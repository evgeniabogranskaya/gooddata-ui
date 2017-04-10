/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import static java.lang.System.getProperty;

import com.gooddata.CfalGoodData;
import com.gooddata.auditlog.AuditLogService;

public abstract class AbstractAT {

    protected final CfalGoodData gd;
    protected final AuditLogService service;

    protected final String host;
    protected final String user;
    protected final String pass;
    protected final String domain;

    public AbstractAT() {
        host = getProperty("host", "localhost");
        user = getProperty("user", "bear@gooddata.com");
        pass = getProperty("pass", "jindrisska");
        domain = getProperty("domain", "default");

        gd = new CfalGoodData(host, user, pass);
        service = gd.getAuditLogService();
    }
}
