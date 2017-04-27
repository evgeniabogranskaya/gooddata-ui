/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.sso;

import org.testng.annotations.Test;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static org.hamcrest.MatcherAssert.*;

public class SSOLoginTest {

    @Test
    public void shouldSerialize() throws Exception {
        final SSOLogin login = new SSOLogin("bear@gooddata.com");
        assertThat(login, jsonEquals(resource("sso/bear.json")));
    }
}