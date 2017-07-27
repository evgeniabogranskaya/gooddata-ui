/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.Test;

public class UserInfoTest {

    public static final String USER_ID = "aa";
    public static final String USER_LOGIN = "bbb";
    public static final String DOMAIN_ID = "ccc";

    final UserInfo userInfo = new UserInfo(USER_ID, USER_LOGIN, DOMAIN_ID);
    final UserInfo userInfo2 = new UserInfo(USER_ID, USER_LOGIN, DOMAIN_ID);
    final UserInfo userInfo3 = new UserInfo(USER_ID, "bb", DOMAIN_ID);

    @Test
    public void testHashCode() throws Exception {
        assertThat(userInfo.hashCode(), is(userInfo.hashCode()));
        assertThat(userInfo.hashCode(), is(userInfo2.hashCode()));
        assertThat(userInfo.hashCode(), is(not(userInfo3.hashCode())));
    }

    @Test
    public void testEquals() throws Exception {
        assertThat(userInfo, is(equalTo(userInfo)));
        assertThat(userInfo, is(equalTo(userInfo2)));
        assertThat(userInfo, is(not(equalTo(userInfo3))));
        assertThat(userInfo, is(not(equalTo(null))));
    }
}