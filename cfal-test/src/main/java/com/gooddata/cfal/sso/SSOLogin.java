/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.sso;

public class SSOLogin {

    private final String email;

    public SSOLogin(final String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
