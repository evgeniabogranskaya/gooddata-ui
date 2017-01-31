/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.selftest;

import com.gooddata.c4.about.AboutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Health check for a C4 client
 * <p>
 * It uses C4 <i>about</i> resource for making sure C4 is accessible with current configuration
 */
@Component
public class C4ConnectionCheck extends AbstractHealthIndicator {

    private final AboutService aboutService;

    @Autowired
    public C4ConnectionCheck(final AboutService aboutService) {
        notNull(aboutService, "aboutService cannot be null");

        this.aboutService = aboutService;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        aboutService.getAbout();
    }
}
