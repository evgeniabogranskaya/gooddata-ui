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
    /** Login using basic auth*/
    BASIC_LOGIN,
    /** Login using SSO */
    SSO_LOGIN,
    /** Login using SST in ADS proxy */
    DATAWAREHOUSE_SST_LOGIN,
    /** Login using user name and password in ADS proxy */
    DATAWAREHOUSE_USERNAME_PASSWORD_LOGIN,
    /** Logout from ADS proxy */
    DATAWAREHOUSE_LOGOUT,
    /** Data access on ADS proxy */
    DATAWAREHOUSE_DATA_ACCESS,
    /** ETL schedule change */
    ETL_SCHEDULE_CHANGE,
    ;
}
