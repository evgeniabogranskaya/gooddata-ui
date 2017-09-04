/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.SardineImpl;
import com.gooddata.UriPrefixer;
import org.apache.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.fail;

public class WebDAVBasicLoginAT extends AbstractLoginAT {

    private static final String BASIC = "BASIC";
    private static final String WEBDAV = "WEBDAV";

    private final String path;
    private final String host;

    public WebDAVBasicLoginAT() {
        final UriPrefixer prefixer = createUriPrefixer();
        final URI uri = prefixer.getUriPrefix();
        this.host = uri.getHost();
        this.path = uri.toString();
    }

    @BeforeSuite(groups = MESSAGE_TYPE)
    @Override
    public void logConnectionInfo() throws Exception {
        logger.info("host={} user={} domain={} path={}", host, props.getUser(), props.getDomain(), path);
    }

    private UriPrefixer createUriPrefixer() {
        final String userStaging = gd.getGdcService().getRootLinks().getUserStagingUri();
        final URI userStagingUri = URI.create(userStaging);
        final URI endpointUri = URI.create(endpoint.toUri());
        return new UriPrefixer(userStagingUri.isAbsolute() ? userStagingUri : endpointUri.resolve(userStaging));
    }

    private Sardine createSardine(final String pass) {
        final Sardine sardine = new SardineImpl(props.getUser(), pass);
        sardine.enablePreemptiveAuthentication(host);
        return sardine;
    }

    @BeforeClass(groups = MESSAGE_TYPE)
    public void shouldLoginUsingWebDAV() throws Exception {
        final Sardine sardine = createSardine(props.getPass());
        final List<DavResource> list = sardine.list(path);
        assertThat(list, is(notNullValue()));
    }

    @BeforeClass(groups = MESSAGE_TYPE)
    public void shouldFailLoginUsingWebDAV() throws Exception {
        final Sardine sardine = createSardine("certainly invalid password");
        try {
            sardine.list(path);
            fail("Expected 401 unauthorized on invalid password");
        } catch (SardineException e) {
            assertThat("status code", e.getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
        }
    }

    @Test(groups = MESSAGE_TYPE)
    public void testWebDAVMessageUserApi() throws Exception {
        doTestUserApi(pageCheckPredicate(true, WEBDAV, BASIC), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testWebDAVMessageAdminApi() throws Exception {
        doTestAdminApi(pageCheckPredicate(true, WEBDAV, BASIC), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testWebDAVMessageUserApiFail() throws Exception {
        doTestUserApi(pageCheckPredicate(false, WEBDAV, BASIC), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testWebDAVMessageAdminApiFail() throws Exception {
        doTestAdminApi(pageCheckPredicate(false, WEBDAV, BASIC), MESSAGE_TYPE);
    }
}
