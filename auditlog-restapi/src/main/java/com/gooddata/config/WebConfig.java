/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.config;

import static com.gooddata.dto.AuditEventDTO.DOMAIN_AUDIT_URI;

import com.gooddata.context.GdcCallContextFilter;
import com.gooddata.exception.servlet.HttpExceptionTranslator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    /**
     * register GdcCallContextFilter as Filter
     */
    @Bean
    public FilterRegistrationBean gdcCallContextFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(gdcCallContextFilter());
        registration.addUrlPatterns(DOMAIN_AUDIT_URI + "/*");
        registration.setName("GDC_FILTER");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public GdcCallContextFilter gdcCallContextFilter() {
        return new GdcCallContextFilter();
    }

    /**
     * @see {@link com.gooddata.exception.AuditlogExceptionTranslatorAdvice}
     */
    @Bean
    public HttpExceptionTranslator httpExceptionTranslator() {
        return new HttpExceptionTranslator();
    }
}
