/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;

@Ignore
public class BasicLoginAT extends AbstractLoginAT {

    private static final String WRONG_PASS = "123";
    private static final String BASIC = "BASIC";

    @Test(groups = MESSAGE_TYPE)
    public void shouldLogUsingBasicAuth() throws IOException {
        final HttpResponse response = loginHelper.basicAuthLoginRequest(getAccount(), props.getPass());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
    }

    @Test(groups = MESSAGE_TYPE)
    public void shouldNotLogWithBadPasswordUsingBasicAuth() throws IOException {
        final HttpResponse response = loginHelper.basicAuthLoginRequest(getAccount(), WRONG_PASS);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "shouldLogUsingBasicAuth")
    public void testLoginMessageUserApi() throws InterruptedException, IOException {
        doTestUserApi(eventCheck(true, WEBAPP, BASIC), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "shouldLogUsingBasicAuth")
    public void testLoginMessageAdminApi() throws InterruptedException, IOException {
        doTestAdminApi(eventCheck(true, WEBAPP, BASIC), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "shouldNotLogWithBadPasswordUsingBasicAuth")
    public void testLoginBadPasswordMessageUserApi() throws InterruptedException, IOException {
        doTestUserApi(eventCheck(false, WEBAPP, BASIC), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "shouldNotLogWithBadPasswordUsingBasicAuth")
    public void testLoginBadPasswordMessageAdminApi() throws InterruptedException, IOException {
        doTestAdminApi(eventCheck(false, WEBAPP, BASIC), MESSAGE_TYPE);
    }
}
