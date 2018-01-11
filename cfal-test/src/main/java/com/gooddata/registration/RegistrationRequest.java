/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.registration;

import static com.gooddata.util.Validate.notEmpty;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * POST new registration request.
 * Serialization only.
 */
@JsonTypeName("postRegistration")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class RegistrationRequest {

    public static final String REGISTRATION_URI = "/gdc/account/registration";

    private final String login;
    private final String licence;
    private final String firstName;
    private final String lastName;
    private final String password;
    private final String verifyPassword;
    private final String captcha;
    private final String verifyCaptcha;

    private RegistrationRequest(final String login, final String licence, final String firstName, final String lastName,
            final String password, final String verifyPassword, final String captcha, final String verifyCaptcha) {
        this.login = notEmpty(login, "login");
        this.licence = notEmpty(licence, "licence");
        this.firstName = notEmpty(firstName, "firstName");
        this.lastName = notEmpty(lastName, "lastName");
        this.password = notEmpty(password, "password");
        this.verifyPassword = notEmpty(verifyPassword, "verifyPassword");
        this.captcha = notEmpty(captcha, "captcha");
        this.verifyCaptcha = notEmpty(verifyCaptcha, "verifyCaptcha");
    }

    public String getLogin() {
        return login;
    }

    public String getLicence() {
        return licence;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword() {
        return password;
    }

    public String getVerifyPassword() {
        return verifyPassword;
    }

    public String getCaptcha() {
        return captcha;
    }

    public String getVerifyCaptcha() {
        return verifyCaptcha;
    }

    public static class Builder {
        private String login;
        private String licence;
        private String firstName;
        private String lastName;
        private String password;
        private String verifyPassword;
        private String captcha;
        private String verifyCaptcha;

        public Builder withLogin(final String login) {
            this.login = login;
            return this;
        }

        public Builder withLicence(final String licence) {
            this.licence = licence;
            return this;
        }

        public Builder withFirstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withPassword(final String password) {
            this.password = password;
            this.verifyPassword = password;
            return this;
        }

        public Builder withCaptcha(final Captcha captcha) {
            this.captcha = captcha.getCaptcha();
            this.verifyCaptcha = captcha.getVerifyCaptcha();
            return this;
        }

        public RegistrationRequest build() {
            return new RegistrationRequest(login, licence, firstName, lastName, password, verifyPassword,
                    captcha, verifyCaptcha);
        }
    }
}
