/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.config;

import com.gooddata.c4.C4Client;
import com.gooddata.c4.domain.DomainService;
import com.gooddata.c4.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class C4Config {

    @Bean
    public UserService userService(C4Client c4Client) {
        return c4Client.getUserService();
    }

    @Bean
    public DomainService domainService(C4Client c4Client) {
        return c4Client.getDomainService();
    }

    @Bean
    public C4Client c4Client(@Value("${gdc.c4.hostname}") String hostName,
                             @Value("${gdc.c4.port}") Integer port,
                             @Value("${gdc.c4.user}") String userName,
                             @Value("${gdc.c4.pass}") String pass) {
        return new C4Client(hostName, port, userName, pass);
    }
}
