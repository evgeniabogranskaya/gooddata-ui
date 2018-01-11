/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.registration;

import com.fasterxml.jackson.annotation.*;

/**
 * Result of registration.
 * Deserialization only.
 */
@JsonTypeName("registrationCreated")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class RegisteredAccount {

    private final String profileUri;
    private final String sst;

    @JsonCreator
    private RegisteredAccount(@JsonProperty("profile") final String profileUri) {
        this(profileUri, null);
    }

    private RegisteredAccount(final String profileUri, final String sst) {
        this.profileUri = profileUri;
        this.sst = sst;
    }

    public String getProfileUri() {
        return profileUri;
    }

    /**
     * @return Super Secure Token of session returned from registration
     */
    public String getSst() {
        return sst;
    }

    /**
     * Appends SST from registration result to this bean.
     *
     * @param sst Super Secure Token from registration
     * @return new instance of {@link RegisteredAccount}
     */
    @JsonIgnore
    public RegisteredAccount withSst(final String sst) {
        return new RegisteredAccount(this.profileUri, sst);
    }
}
