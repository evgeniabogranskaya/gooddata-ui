/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.config;

import com.gooddata.cfal.restapi.exception.AuditlogExceptionTranslatorAdvice;
import com.gooddata.commons.web.filter.LoggingContextSetupFilter;
import com.gooddata.context.GdcCallContextFilter;
import com.gooddata.exception.servlet.HttpExceptionTranslator;
import org.apache.http.HttpStatus;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.BindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.DOMAIN_AUDIT_URI;

import java.util.Arrays;
import java.util.HashMap;

@Configuration
public class WebConfig {

    /**
     * register GdcCallContextFilter as Filter
     */
    @Bean
    public FilterRegistrationBean gdcCallContextFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new GdcCallContextFilter());
        registration.addUrlPatterns(DOMAIN_AUDIT_URI + "/*");
        registration.setName("GDC_FILTER");
        registration.setOrder(1);
        return registration;
    }

    /**
     * register LoggingContextSetupFilter as Filter
     */
    @Bean
    public FilterRegistrationBean gdcRequestIdHandlingFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new LoggingContextSetupFilter());
        registration.addUrlPatterns(DOMAIN_AUDIT_URI + "/*");
        registration.setName("GDC_LOGGING_CONTEXT_FILTER");
        registration.setOrder(2);
        return registration;
    }

    /**
     * Create http exception translator and map exceptions to http statuses
     * @see {@link AuditlogExceptionTranslatorAdvice}
     */
    @Bean
    public HttpExceptionTranslator httpExceptionTranslator() {
        final HttpExceptionTranslator httpExceptionTranslator = new HttpExceptionTranslator();
        httpExceptionTranslator.setExceptionsToHTTPstatusMapping(new HashMap<Integer, java.util.List<Class<? extends Exception>>>(){{
            //map BindException and MethodArgumentTypeMismatchException (these exceptions are thrown due to type conversion error) to bad request status, because by default it is 5xx
            put(HttpStatus.SC_BAD_REQUEST, Arrays.asList(BindException.class, MethodArgumentTypeMismatchException.class));
        }});
        return httpExceptionTranslator;
    }
}
