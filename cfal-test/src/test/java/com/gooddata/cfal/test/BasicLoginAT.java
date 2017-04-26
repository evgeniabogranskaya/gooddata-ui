/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
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
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

public class BasicLoginAT extends AbstractAT {

    private static final String HTTPS = "https://";
    private static final String MESSAGE_TYPE = "BASIC_LOGIN";
    private static final String WRONG_PASS = "123";

    @Test
    public void shouldLogUsingBasicAuth() throws IOException {
        final HttpResponse response = doBasicAuth(pass);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
    }

    @Test
    public void shouldNotLogWithBadPasswordUsingBasicAuth() throws IOException {
        final HttpResponse response = doBasicAuth(WRONG_PASS);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
    }

    @Test(dependsOnMethods = "shouldLogUsingBasicAuth")
    public void testLoginMessageUserApi() throws InterruptedException, IOException {
        doTestUserApi(pageCheckPredicate(true));
    }

    @Test(dependsOnMethods = "shouldLogUsingBasicAuth")
    public void testLoginMessageAdminApi() throws InterruptedException, IOException {
        doTestAdminApi(pageCheckPredicate(true));
    }

    @Test(dependsOnMethods = "shouldNotLogWithBadPasswordUsingBasicAuth")
    public void testLoginBadPasswordMessageUserApi() throws InterruptedException, IOException {
        doTestUserApi(pageCheckPredicate(false));
    }

    @Test(dependsOnMethods = "shouldNotLogWithBadPasswordUsingBasicAuth")
    public void testLoginBadPasswordMessageAdminApi() throws InterruptedException, IOException {
        doTestAdminApi(pageCheckPredicate(false));
    }

    private HttpResponse doBasicAuth(final String password) throws IOException {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(host, 443), new UsernamePasswordCredentials(user, password));

        final AuthCache authCache = new BasicAuthCache();
        authCache.put(new HttpHost(host, 443, "https"), new BasicScheme());

        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credentialsProvider);
        context.setAuthCache(authCache);

        final HttpClient httpClient = HttpClientBuilder.create().build();

        final HttpGet get = new HttpGet(getUrl(account.getUri()));
        try {
            return httpClient.execute(get, context);
        } finally {
            get.releaseConnection();
        }
    }

    private String getUrl(final String uri) {
        return HTTPS + host + ":" + 443 + uri;
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final boolean success) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE) && e.isSuccess() == success);
    }
}
