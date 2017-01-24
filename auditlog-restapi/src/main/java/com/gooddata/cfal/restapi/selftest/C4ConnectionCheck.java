package com.gooddata.cfal.restapi.selftest;

import com.gooddata.c4.C4Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * Health check for a C4 client
 * <p>
 * It should use C4 const resource for making sure C4 is accessible with current configuration
 */
@Component
public class C4ConnectionCheck extends AbstractHealthIndicator {

    private final C4Client c4Client;

    @Autowired
    public C4ConnectionCheck(final C4Client c4Client) {
        this.c4Client = c4Client;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        c4Client.getDomainService().getDomain("default");
    }
}
