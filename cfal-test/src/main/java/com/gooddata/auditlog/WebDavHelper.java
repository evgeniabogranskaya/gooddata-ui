/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import static org.apache.commons.lang3.StringUtils.appendIfMissing;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineImpl;
import com.gooddata.CfalGoodData;
import com.gooddata.UriPrefixer;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Singleton for WebDAV related stuff.
 * Lazy initialized. Not thread safe.
 */
public class WebDavHelper {

    private static WebDavHelper instance;

    private final String path;
    private final String host;
    private TestEnvironmentProperties props;

    private WebDavHelper(final CfalGoodData gd, final TestEnvironmentProperties props) {
        final UriPrefixer prefixer = createUriPrefixer(gd);
        final URI uri = prefixer.getUriPrefix();

        this.host = uri.getHost();
        this.path = appendIfMissing(uri.toString(), "/");
        this.props = props;
    }

    public static WebDavHelper getInstance() {
        if (instance == null) {
            final CfalGoodData gd = CfalGoodData.getInstance();
            final TestEnvironmentProperties props = TestEnvironmentProperties.getInstance();
            instance = new WebDavHelper(gd, props);
        }
        return instance;
    }

    /**
     * Gets directory listing for the WebDAV using given password for login.
     *
     * @param pass password for WebDAV login
     * @throws IOException I/O error or HTTP response validation failure
     */
    public List<DavResource> listWebdav(final String pass) throws IOException {
        final Sardine sardine = createSardine(pass);
        return sardine.list(path);
    }

    /**
     * @return configured WebDAV path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return configured WebDAV host
     */
    public String getHost() {
        return host;
    }

    private UriPrefixer createUriPrefixer(final CfalGoodData gd) {
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
}
