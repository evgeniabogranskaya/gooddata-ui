/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.account;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.SardineImpl;
import com.gooddata.UriPrefixer;
import com.gooddata.auditlog.WebDavHelper;
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

    @BeforeSuite(groups = GROUP)
    public void logWebDAVConnectionInfo() throws Exception {
        logger.info("host={} user={} path={}", webDavHelper.getHost(), props.getUser(), webDavHelper.getPath());
    }

    @BeforeClass(groups = GROUP)
    public void shouldLoginUsingWebDAV() throws Exception {
        final List<DavResource> list = webDavHelper.listWebdav(props.getPass());
        assertThat(list, is(notNullValue()));
    }

    @BeforeClass(groups = GROUP)
    public void shouldFailLoginUsingWebDAV() throws Exception {
        try {
            webDavHelper.listWebdav("certainly invalid password");
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
