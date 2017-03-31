/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

/**
 * Type of audit event.
 */
public enum AuditLogEventType {

    /** Login using user name and password */
    STANDARD_LOGIN,
    /** Login using SST in ADS proxy */
    DATAWAREHOUSE_SST_LOGIN,
    /** Login using user name and password in ADS proxy */
    DATAWAREHOUSE_USERNAME_PASSWORD_LOGIN,
    /** ETL schedule change */
    ETL_SCHEDULE_CHANGE,
    ;
}
