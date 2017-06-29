/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.SardineImpl;
import com.gooddata.UriPrefixer;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import org.apache.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.List;
import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.fail;

public class WebDAVBasicLoginAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "WEBDAV_BASIC_LOGIN";

    private final String path;

    public WebDAVBasicLoginAT() {
        final UriPrefixer prefixer = createUriPrefixer();
        this.path = prefixer.getUriPrefix().toString();
    }

    private UriPrefixer createUriPrefixer() {
        final String userStaging = gd.getGdcService().getRootLinks().getUserStagingUri();
        final URI userStagingUri = URI.create(userStaging);
        final URI endpointUri = URI.create(endpoint.toUri());
        return new UriPrefixer(userStagingUri.isAbsolute() ? userStagingUri : endpointUri.resolve(userStaging));
    }

    private Sardine createSardine(final String pass) {
        final Sardine sardine = new SardineImpl(props.getUser(), pass);
        sardine.enablePreemptiveAuthentication(props.getHost());
        return sardine;
    }

    @BeforeClass
    public void shouldLoginUsingWebDAV() throws Exception {
        final Sardine sardine = createSardine(props.getPass());
        final List<DavResource> list = sardine.list(path);
        assertThat(list, is(notNullValue()));
    }

    @BeforeClass
    public void shouldFailLoginUsingWebDAV() throws Exception {
        final Sardine sardine = createSardine("certainly invalid password");
        try {
            sardine.list(path);
            fail("Expected 401 unauthorized on invalid password");
        } catch (SardineException e) {
            assertThat("status code", e.getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
        }
    }

    @Test
    public void testWebDAVMessageUserApi() throws Exception {
        doTestUserApi(pageCheckPredicate(true), MESSAGE_TYPE);
    }

    @Test
    public void testWebDAVMessageAdminApi() throws Exception {
        doTestAdminApi(pageCheckPredicate(true), MESSAGE_TYPE);
    }

    @Test
    public void testWebDAVMessageUserApiFail() throws Exception {
        doTestUserApi(pageCheckPredicate(false), MESSAGE_TYPE);
    }

    @Test
    public void testWebDAVMessageAdminApiFail() throws Exception {
        doTestAdminApi(pageCheckPredicate(false), MESSAGE_TYPE);
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final boolean success) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE) && e.isSuccess() == success);
    }

}
