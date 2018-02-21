/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.registration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.gooddata.util.ResourceUtils;
import org.testng.annotations.Test;

public class RegisteredAccountTest {

    @Test
    public void shouldDeserialize() {
        final RegisteredAccount account =
                ResourceUtils.readObjectFromResource("/registration/registrationResponse.json", RegisteredAccount.class);

        assertThat(account.getProfileUri(), is("/some/profile/uri"));
    }
}