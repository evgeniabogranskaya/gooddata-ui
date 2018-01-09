/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooddata.CfalGoodData;
import com.gooddata.account.Account;
import com.gooddata.cfal.sso.SSOLogin;
import com.gooddata.security.pgp.PgpEncryptor;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Singleton for login related stuff.
 * Lazy initialized. Not thread safe.
 */
public class LoginHelper {

    private static final String GD_SSO_PUBLIC_KEY_PATH = "/sso/dev-gooddata.com.pub";
    private static final String SMURFS_SSO_PRIVATE_KEY_PATH = "/sso/smurfs.priv";
    private static final String SMURFS_SERVER_URL = "smurfs";
    private static final String SSO_LOGIN_URI =
            "/gdc/account/customerlogin?sessionId={sessionId}&serverURL={server}&targetURL=/gdc";

    private static LoginHelper instance;

    private final CfalGoodData gd;
    private final TestEnvironmentProperties props;

    private LoginHelper(final CfalGoodData gd, final TestEnvironmentProperties props) {
        this.gd = gd;
        this.props = props;
    }

    public static LoginHelper getInstance() {
        if (instance == null) {
            final CfalGoodData gd = CfalGoodData.getInstance();
            final TestEnvironmentProperties props = TestEnvironmentProperties.getInstance();
            instance = new LoginHelper(gd, props);
        }
        return instance;
    }

    /**
     * Does request to given account with basic auth login and the given password
     *
     * @param account account
     * @param password account password for basic auth
     * @return {@link HttpResponse}
     * @throws IOException in a case when error occurred during HTTP request execution
     */
    public HttpResponse basicAuthLoginRequest(final Account account, final String password) throws IOException {
        notNull(account, "account cannot be null!");
        notEmpty(password, "password cannot be empty!");

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(props.getHost(), 443),
                new UsernamePasswordCredentials(account.getLogin(), password));

        final AuthCache authCache = new BasicAuthCache();
        authCache.put(new HttpHost(props.getHost(), 443, "https"), new BasicScheme());

        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credentialsProvider);
        context.setAuthCache(authCache);

        final HttpClient httpClient = HttpClientBuilder.create().build();

        final HttpGet get = new HttpGet(getAccountUrl(account));
        try {
            return httpClient.execute(get, context);
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Simulates "username-password" login for existing GoodData session
     *
     * @return account from this login
     */
    public Account usernamePasswordLogin() {
        gd.getAccountService().logout();

        // do username password login
        return gd.getAccountService().getCurrent();
    }

    /**
     * Does SSO login for the given account and returns HTTP response entity from this SSO login.
     *
     * This login expects existing SSO provider named 'smurfs' (created by cfal::sso Puppet class).
     * <h3>How to generate Smurfs PGP Keys</h3>
     * <pre>
     *      gpg --gen-key
     *      gpg --armor --export smurfs@gooddata.com > smurfs.pub
     *      gpg --armor --export-secret-keys smurfs@gooddata.com > smurfs.priv
     * </pre>
     *
     * <h3>How to create the sso.txt</h3>
     * <pre>
     *      gpg --import dev-gooddata.com.pub
     *      gpg --import smurfs.priv
     *
     *      echo '{"email": "bear@gooddata.com"}' > bear.json
     *      gpg --armor -u smurfs@gooddata.com --output signed.txt --sign bear.json
     *      gpg --armor --output sso.txt --encrypt --recipient test@gooddata.com signed.txt
     *
     *      curl -i --insecure 'https://cfal.na.intgdc.com/gdc/account/customerlogin?sessionId=<sso.txt>&serverURL=smurfs&targetURL=/gdc'
     * </pre>
     *
     * @return {@link ResponseEntity} of SSO login
     * @throws Exception when error occurs during creating and encrypting the SSO session
     */
    public ResponseEntity<String> ssoLogin(final Account account) throws Exception {
        notNull(account, "account cannot be null!");

        final PgpEncryptor encryptor = new PgpEncryptor.Builder()
                .setPublicKeyForEncryption(new ClassPathResource(GD_SSO_PUBLIC_KEY_PATH).getInputStream())
                .setSecretKeyForSigning(new ClassPathResource(SMURFS_SSO_PRIVATE_KEY_PATH).getInputStream())
                .createPgpEncryptor();

        final String json = new ObjectMapper().writeValueAsString(new SSOLogin(account.getLogin()));

        final ByteArrayOutputStream signedMessageOut = new ByteArrayOutputStream();
        encryptor.signMessage(IOUtils.toInputStream(json), signedMessageOut, true);

        final ByteArrayOutputStream encryptedMessageOut = new ByteArrayOutputStream();
        encryptor.encryptMessage(encryptedMessageOut, new ByteArrayInputStream(signedMessageOut.toByteArray()), true);

        final String session = encryptedMessageOut.toString();

        final RestTemplate rest = gd.createRestTemplate(HttpClientBuilder.create().build());

        return rest.getForEntity(SSO_LOGIN_URI, String.class, session, SMURFS_SERVER_URL);
    }

    private String getAccountUrl(final Account account) {
        return gd.getEndpoint().toUri() + account.getUri();
    }
}
