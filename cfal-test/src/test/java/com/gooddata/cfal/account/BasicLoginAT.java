/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.gooddata.cfal.AbstractAT;
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

    @Test(groups = MESSAGE_TYPE)
    public void shouldLogUsingBasicAuth() throws IOException {
        final HttpResponse response = doBasicAuth(props.getPass());

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
    }

    @Test(groups = MESSAGE_TYPE)
    public void shouldNotLogWithBadPasswordUsingBasicAuth() throws IOException {
        final HttpResponse response = doBasicAuth(WRONG_PASS);

        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "shouldLogUsingBasicAuth")
    public void testLoginMessageUserApi() throws InterruptedException, IOException {
        doTestUserApi(pageCheckPredicate(true), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "shouldLogUsingBasicAuth")
    public void testLoginMessageAdminApi() throws InterruptedException, IOException {
        doTestAdminApi(pageCheckPredicate(true), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "shouldNotLogWithBadPasswordUsingBasicAuth")
    public void testLoginBadPasswordMessageUserApi() throws InterruptedException, IOException {
        doTestUserApi(pageCheckPredicate(false), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE, dependsOnMethods = "shouldNotLogWithBadPasswordUsingBasicAuth")
    public void testLoginBadPasswordMessageAdminApi() throws InterruptedException, IOException {
        doTestAdminApi(pageCheckPredicate(false), MESSAGE_TYPE);
    }

    private HttpResponse doBasicAuth(final String password) throws IOException {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(props.getHost(), 443), new UsernamePasswordCredentials(props.getUser(), password));

        final AuthCache authCache = new BasicAuthCache();
        authCache.put(new HttpHost(props.getHost(), 443, "https"), new BasicScheme());

        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credentialsProvider);
        context.setAuthCache(authCache);

        final HttpClient httpClient = HttpClientBuilder.create().build();

        final HttpGet get = new HttpGet(getUrl(getAccount().getUri()));
        try {
            return httpClient.execute(get, context);
        } finally {
            get.releaseConnection();
        }
    }

    private String getUrl(final String uri) {
        return HTTPS + props.getHost() + ":" + 443 + uri;
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final boolean success) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(getAccount().getLogin()) && e.getType().equals(MESSAGE_TYPE) && e.isSuccess() == success);
    }
}
