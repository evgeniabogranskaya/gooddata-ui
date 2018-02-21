/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata;

import static com.gooddata.registration.Captcha.CAPTCHA_URI;
import static com.gooddata.registration.RegistrationRequest.REGISTRATION_URI;
import static com.gooddata.util.Validate.notNull;

import com.gooddata.account.Account;
import com.gooddata.account.AccountService;
import com.gooddata.registration.Captcha;
import com.gooddata.registration.RegisteredAccount;
import com.gooddata.registration.RegistrationRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link AccountService} extended by functionality needed in acceptance tests.
 */
public class ExtendedAccountService extends AccountService {

    private static final String SST_COOKIE_KEY_NAME = "GDCAuthSST";
    private static final String DEFAULT_LICENCE = "1";

    public ExtendedAccountService(final RestTemplate restTemplate) {
        super(restTemplate);
    }

    /**
     * Registers given account in GD platform. Returns {@link RegisteredAccount} object containing profile URI and SST
     * for immediate session returned by this registration. You can login with this SST without confirming registration
     * email.
     *
     * This use case doesn't make sense to be implemented in Java SDK itself because we have possibility to create user
     * via {@link AccountService#createAccount(Account, String)}.
     *
     * @param account account which should be registered
     * @return {@link RegisteredAccount} object with profile URI and SST
     */
    public RegisteredAccount registerAccount(final Account account) {
        final RegistrationRequest request = createRegistrationRequest(notNull(account, "account"));

        final ResponseEntity<RegisteredAccount> response;
        try {
            response = restTemplate.postForEntity(REGISTRATION_URI, request, RegisteredAccount.class);
        } catch (RestClientException e) {
            throw new GoodDataException("Could not register new account:" + account.toString(), e);
        }

        final String sst = extractSstFromResponseCookies(response);

        final RegisteredAccount body = response.getBody();
        if (body == null) {
            throw new GoodDataException("Empty body returned by registration of new account:" + account.toString());
        }

        return body.withSst(sst);
    }

    /**
     * Builds registration request.
     * We need to generate captcha before that because request itself needs it.
     */
    private RegistrationRequest createRegistrationRequest(final Account account) {
        final Captcha captcha;
        try {
            captcha = restTemplate.getForObject(CAPTCHA_URI, Captcha.class);
        } catch (RestClientException e) {
            throw new GoodDataException("Error fetching captcha for the new user registration.", e);
        }

        return new RegistrationRequest.Builder()
                .withLogin(account.getLogin())
                .withLicence(DEFAULT_LICENCE)
                .withFirstName(account.getFirstName())
                .withLastName(account.getLastName())
                .withPassword(account.getPassword())
                .withCaptcha(captcha)
                .build();
    }

    /**
     * Extracts and returns Super Secure Token from cookies returned by successful registration of the new user.
     * There's no possibility to set up registration to be able to return SST as regular header (like in login
     * verification_level=2). Therefore we need to use cookies.
     * We need SST for further calling GD APIs with registered user without confirming registration via email.
     */
    private static String extractSstFromResponseCookies(final ResponseEntity response) {
        final List<String> cookiesStrings = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookiesStrings == null) {
            throw new GoodDataException("No cookies found in user registration response: " + response.toString());
        }
        final String sstCookie = getSstCookie(response, cookiesStrings);

        final Map<String, String> sstCookieParts = getSstCookieParts(sstCookie);

        final String sstValue = sstCookieParts.get(SST_COOKIE_KEY_NAME);
        if (StringUtils.isNotEmpty(sstValue)) {
            return sstValue;
        } else {
            throw new GoodDataException("Empty 'GDCAuthSST' cookie value found in user registration response: " +
                    response.toString());
        }
    }

    private static String getSstCookie(final ResponseEntity response, final List<String> cookies) {
        return cookies.stream()
                    .filter(cookieString -> cookieString.contains(SST_COOKIE_KEY_NAME))
                    .findFirst()
                    .orElseThrow(() -> new GoodDataException("No 'GDCAuthSST' cookie found in user registration "
                            + "response: " + response.toString()));
    }

    private static Map<String, String> getSstCookieParts(final String sstCookie) {
        return Arrays.stream(sstCookie.split(";"))
                .map(cookie -> cookie.split("="))
                .collect(Collectors.toMap(cookieSplit -> cookieSplit[0],
                        cookieSplit -> cookieSplit.length > 1 ? cookieSplit[1] : ""));
    }
}
