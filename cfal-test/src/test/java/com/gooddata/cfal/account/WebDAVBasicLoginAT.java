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

import static org.apache.commons.lang3.StringUtils.appendIfMissing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.fail;

public class WebDAVBasicLoginAT extends AbstractLoginAT {

    private static final String GROUP = "WEBDAV";
    private static final String LOGIN_TYPE = "BASIC";
    private static final String COMPONENT = "WEBDAV";

    private final String path;
    private final String host;

    public WebDAVBasicLoginAT() {
        final UriPrefixer prefixer = createUriPrefixer();
        final URI uri = prefixer.getUriPrefix();
        this.host = uri.getHost();
        final String path = uri.toString();
        this.path = appendIfMissing(path, "/");
    }

    @BeforeSuite(groups = GROUP)
    public void logWebDAVConnectionInfo() throws Exception {
        logger.info("host={} user={} path={}", host, props.getUser(), path);
    }

    private UriPrefixer createUriPrefixer() {
        final String userStaging = gd.getGdcService().getRootLinks().getUserStagingUri();
        final URI userStagingUri = URI.create(userStaging);
        final URI endpointUri = URI.create(gd.getEndpoint().toUri());
        return new UriPrefixer(userStagingUri.isAbsolute() ? userStagingUri : endpointUri.resolve(userStaging));
    }

    private Sardine createSardine(final String pass) {
        final Sardine sardine = new SardineImpl(props.getUser(), pass);
        sardine.enablePreemptiveAuthentication(host);
        return sardine;
    }

    @BeforeClass(groups = GROUP)
    public void shouldLoginUsingWebDAV() throws Exception {
        final Sardine sardine = createSardine(props.getPass());
        final List<DavResource> list = sardine.list(path);
        assertThat(list, is(notNullValue()));
    }

    @BeforeClass(groups = GROUP)
    public void shouldFailLoginUsingWebDAV() throws Exception {
        final Sardine sardine = createSardine("certainly invalid password");
        try {
            sardine.list(path);
            fail("Expected 401 unauthorized on invalid password");
        } catch (SardineException e) {
            assertThat("status code", e.getStatusCode(), is(HttpStatus.SC_UNAUTHORIZED));
        }
    }

    @Test(groups = GROUP)
    public void testWebDAVMessageUserApi() throws Exception {
        doTestUserApi(eventCheck(true, COMPONENT, LOGIN_TYPE), MESSAGE_TYPE);
    }

    @Test(groups = GROUP)
    public void testWebDAVMessageAdminApi() throws Exception {
        doTestAdminApi(eventCheck(true, COMPONENT, LOGIN_TYPE), MESSAGE_TYPE);
    }

    @Test(groups = GROUP)
    public void testWebDAVMessageUserApiFail() throws Exception {
        doTestUserApi(eventCheck(false, COMPONENT, LOGIN_TYPE), MESSAGE_TYPE);
    }

    @Test(groups = GROUP)
    public void testWebDAVMessageAdminApiFail() throws Exception {
        doTestAdminApi(eventCheck(false, COMPONENT, LOGIN_TYPE), MESSAGE_TYPE);
    }
}
