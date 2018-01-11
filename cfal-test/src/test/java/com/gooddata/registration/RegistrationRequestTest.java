/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.registration;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooddata.util.ResourceUtils;
import org.testng.annotations.Test;

public class RegistrationRequestTest {

    @Test
    public void shouldSerialize() throws Exception {
        final RegistrationRequest request = new RegistrationRequest.Builder()
                .withLogin("my@email.com")
                .withLicence("1")
                .withFirstName("Vulpes")
                .withLastName("Lagopus")
                .withPassword("new_password")
                .withCaptcha(new Captcha("x7jp8", "b4eb5b9d56edfed5bf759dca84217044"))
                .build();

        final String json = new ObjectMapper().writeValueAsString(request);
        final String expectedJson = ResourceUtils.readStringFromResource("/registration/registrationRequest.json");
        assertThat(json, jsonEquals(expectedJson));
    }
}