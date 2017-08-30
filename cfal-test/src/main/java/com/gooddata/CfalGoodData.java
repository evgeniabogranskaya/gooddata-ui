/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import com.gooddata.authentication.LoginPasswordAuthentication;
import org.apache.http.client.HttpClient;
import org.springframework.web.client.RestTemplate;

/**
 * CFAL extension of GoodData Java client. To be removed once moved into SDK.
 */
public class CfalGoodData extends GoodData {

    public CfalGoodData(final GoodDataEndpoint endpoint, final String login, final String password) {
        super(endpoint, new LoginPasswordAuthentication(login, password));
    }

    public static RestTemplate createRestTemplate(final GoodDataEndpoint endpoint, final HttpClient httpClient) {
        return GoodData.createRestTemplate(endpoint, httpClient);
    }
}
