/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.registration;

import com.fasterxml.jackson.annotation.*;


/**
 * Captcha information.
 * Deserialization only.
 */
@JsonTypeName("captcha")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class Captcha {

    public static final String CAPTCHA_URI = "/gdc/tool/captcha";

    private final String captchaString;
    private final String verifyCaptcha;

    Captcha(@JsonProperty("captchaString") final String captchaString,
            @JsonProperty("verifyCaptcha") final String verifyCaptcha) {
        this.captchaString = captchaString;
        this.verifyCaptcha = verifyCaptcha;
    }

    public String getCaptcha() {
        return captchaString;
    }

    public String getVerifyCaptcha() {
        return verifyCaptcha;
    }
}
