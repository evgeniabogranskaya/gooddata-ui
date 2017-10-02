/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import com.gooddata.md.Attribute;
import com.gooddata.md.MetadataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * MetadataService extended with functionality needed in acceptance tests
 */
public class ExtendedMetadataService extends MetadataService {

    public ExtendedMetadataService(final RestTemplate restTemplate) {
        super(restTemplate);
    }

    /**
     * List valid elements for attribute
     * @param attribute
     * @return
     */
    public String getAttributeValidElements(final Attribute attribute) {
        final ResponseEntity<String> response = restTemplate.postForEntity(attribute.getDefaultDisplayForm().getUri() + "/validElements", new ValidElementsRequest(), String.class);

        if (response.getStatusCode() == HttpStatus.SEE_OTHER) {
            final URI location = response.getHeaders().getLocation();

            return new PollResult<>(this, new SimplePollHandler<String>(location.toString(), String.class) {
                @Override
                public void handlePollException(final GoodDataRestException e) {
                    throw e;
                }
            }).get();
        }

        return response.getBody();
    }
}
