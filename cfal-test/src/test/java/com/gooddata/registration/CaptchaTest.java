/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.registration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.gooddata.util.ResourceUtils;
import org.testng.annotations.Test;

public class CaptchaTest {

    @Test
    public void shouldDeserialize() {
        final Captcha captcha = ResourceUtils.readObjectFromResource("/registration/captcha.json", Captcha.class);

        assertThat(captcha.getCaptcha(), is("cptch"));
        assertThat(captcha.getVerifyCaptcha(), is("captchaabcd"));
    }
}