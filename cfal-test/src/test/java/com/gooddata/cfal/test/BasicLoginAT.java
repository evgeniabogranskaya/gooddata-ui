/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import org.apache.http.HttpHost;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Predicate;

public class BasicLoginAT extends AbstractAT {

    private static final String HTTPS = "https://";
    private static final String MESSAGE_TYPE = "BASIC_LOGIN";

    @BeforeMethod
    public void setUp() throws Exception {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(host, 443), new UsernamePasswordCredentials(user, pass));

        final AuthCache authCache = new BasicAuthCache();
        authCache.put(new HttpHost(host, 443, "https"), new BasicScheme());

        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credentialsProvider);
        context.setAuthCache(authCache);

        final HttpClient httpClient = HttpClientBuilder.create().build();

        final HttpGet get = new HttpGet(getUrl(account.getUri()));
        try {
            httpClient.execute(get, context);
        } finally {
            get.releaseConnection();
        }
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLoginMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate());
    }

    @Test(groups = MESSAGE_TYPE)
    public void testLoginMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate());
    }

    private String getUrl(final String uri) {
        return HTTPS + host + ":" + 443 + uri;
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate() {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE));
    }
}
