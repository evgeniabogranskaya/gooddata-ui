/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.config;

import com.gooddata.c4.C4Client;
import com.gooddata.c4.HttpClientSettings;
import com.gooddata.c4.about.AboutService;
import com.gooddata.c4.domain.DomainService;
import com.gooddata.c4.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class C4Config {

    private static final String USER_AGENT = "CFAL/restapi";

    @Bean
    public UserService userService(C4Client c4Client) {
        return c4Client.getUserService();
    }

    @Bean
    public DomainService domainService(C4Client c4Client) {
        return c4Client.getDomainService();
    }

    @Bean
    public AboutService aboutService(C4Client c4Client) {
        return c4Client.getAboutService();
    }

    @Bean
    public C4Client c4Client(@Value("${gdc.c4.hostname}") String hostName,
                             @Value("${gdc.c4.port}") Integer port,
                             @Value("${gdc.c4.user}") String userName,
                             @Value("${gdc.c4.pass}") String pass) {
        return new C4Client(hostName, port, userName, pass, createHttpClientSettings());
    }

    /**
     * Create {@link HttpClientSettings} with <code>user_agent</code> set smart default
     */
    public HttpClientSettings createHttpClientSettings() {
        final HttpClientSettings result = new HttpClientSettings();
        result.setUserAgent(USER_AGENT);

        return result;
    }
}
