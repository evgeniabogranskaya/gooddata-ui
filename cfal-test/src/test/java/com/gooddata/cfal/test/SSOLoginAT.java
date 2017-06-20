/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooddata.GoodDataEndpoint;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.sso.SSOLogin;
import com.gooddata.security.pgp.PgpEncryptor;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.function.Predicate;

import static com.gooddata.CfalGoodData.createRestTemplate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * This test expects existing SSO provider named 'smurfs' (created by cfal::sso Puppet class).
 * <h3>How to generate Smurfs PGP Keys</h3>
 * <pre>
gpg --gen-key
gpg --armor --export smurfs@gooddata.com > smurfs.pub
gpg --armor --export-secret-keys smurfs@gooddata.com > smurfs.priv
 </pre>
 * <h3>How to create the sso.txt</h3>
 * <pre>
gpg --import dev-gooddata.com.pub
gpg --import smurfs.priv

echo '{"email": "bear@gooddata.com"}' > bear.json
gpg --armor -u smurfs@gooddata.com --output signed.txt --sign bear.json
gpg --armor --output sso.txt --encrypt --recipient test@gooddata.com signed.txt

 curl -i --insecure 'https://cfal.na.intgdc.com/gdc/account/customerlogin?sessionId=<sso.txt>&serverURL=smurfs&targetURL=/gdc'
 </pre>
 */
public class SSOLoginAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "SSO_LOGIN";

    private static final String URI = "/gdc/account/customerlogin?sessionId={sessionId}&serverURL={server}&targetURL=/gdc";
    private final String session;

    public SSOLoginAT() throws Exception {
        final PgpEncryptor encryptor = new PgpEncryptor.Builder()
                .setPublicKeyForEncryption(new ClassPathResource("/sso/dev-gooddata.com.pub").getInputStream())
                .setSecretKeyForSigning(new ClassPathResource("/sso/smurfs.priv").getInputStream())
                .createPgpEncryptor();

        final String json = new ObjectMapper().writeValueAsString(new SSOLogin(user));

        final ByteArrayOutputStream signedMessageOut = new ByteArrayOutputStream();
        encryptor.signMessage(IOUtils.toInputStream(json), signedMessageOut, true);

        final ByteArrayOutputStream encryptedMessageOut = new ByteArrayOutputStream();
        encryptor.encryptMessage(encryptedMessageOut, new ByteArrayInputStream(signedMessageOut.toByteArray()), true);

        session = encryptedMessageOut.toString();
    }

    @Test
    public void shouldLoginUserWithSSO() throws Exception {
        final RestTemplate rest = createRestTemplate(new GoodDataEndpoint(host), HttpClientBuilder.create().build());

        final ResponseEntity<String> result = rest.getForEntity(URI, String.class, session, "smurfs");
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

    @Test(dependsOnMethods = "shouldLoginUserWithSSO")
    public void testLoginMessageUserApi() throws InterruptedException {
        doTestUserApi(pageCheckPredicate(), MESSAGE_TYPE);
    }

    @Test(dependsOnMethods = "shouldLoginUserWithSSO")
    public void testLoginMessageAdminApi() throws InterruptedException {
        doTestAdminApi(pageCheckPredicate(), MESSAGE_TYPE);
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate() {
        return (auditEvents) -> auditEvents.stream().anyMatch(e -> e.getUserLogin().equals(account.getLogin()) && e.getType().equals(MESSAGE_TYPE));
    }
}
