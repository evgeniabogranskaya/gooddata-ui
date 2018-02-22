/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.config;

import com.codahale.metrics.MetricRegistry;
import com.gooddata.cfal.restapi.exception.AuditlogExceptionTranslatorAdvice;
import com.gooddata.cfal.restapi.util.StringToUTCDateTimeConverter;
import com.gooddata.commons.monitoring.rest.RequestMonitoringInterceptor;
import com.gooddata.commons.monitoring.servlet.ResponseMonitoringFilter;
import com.gooddata.commons.monitoring.servlet.ResponseMonitoringService;
import com.gooddata.commons.web.filter.LoggingContextSetupFilter;
import com.gooddata.commons.web.filter.ResetableRequestFilter;
import com.gooddata.context.GdcCallContextFilter;
import com.gooddata.exception.servlet.HttpExceptionTranslator;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.validation.BindException;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;
import java.util.Map;

import static com.gooddata.auditevent.AuditEvent.GDC_URI;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

@Configuration
//NO @EnableWebMvc, because this is only additional MVC config to spring boot autoconfiguration
public class WebConfig extends WebMvcConfigurerAdapter {

    public static final String COMPONENT_NAME = "auditlog";

    private static final String URL_PATTERN = GDC_URI + "/*";

    private static final String GDC_FILTER_NAME = "GDC_FILTER";
    private static final String GDC_LOGGING_CONTEXT_FILTER_NAME = "GDC_LOGGING_CONTEXT_FILTER";
    private static final String RESPONSE_MONITORING_FILTER_NAME = "RESPONSE_MONITORING_FILTER";
    private static final String RESETABLE_REQUEST_FILTER = "RESETABLE_REQUEST_FILTER";

    private static final String DISPATCHER_SERVLET = "dispatcherServlet";

    @Autowired
    private RequestMonitoringInterceptor requestMonitoringInterceptor;

    /**
     * register GdcCallContextFilter as Filter
     */
    @Bean
    public FilterRegistrationBean gdcCallContextFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new GdcCallContextFilter());
        registration.addUrlPatterns(URL_PATTERN);
        registration.setName(GDC_FILTER_NAME);
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
        registration.addUrlPatterns(URL_PATTERN);
        registration.setName(GDC_LOGGING_CONTEXT_FILTER_NAME);
        registration.setOrder(2);
        return registration;
    }

    /**
     * register ResponseMonitoringFilter as Filter
     */
    @Bean
    public FilterRegistrationBean responseMonitoringFilter(final ResponseMonitoringService responseMonitoringService) {
        final ResponseMonitoringFilter filter = new ResponseMonitoringFilter(responseMonitoringService);
        final DelegatingFilterProxy proxy = new DelegatingFilterProxy(filter);
        proxy.setTargetFilterLifecycle(true);
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean(proxy);
        registrationBean.setName(RESPONSE_MONITORING_FILTER_NAME);
        registrationBean.setOrder(3);
        registrationBean.addServletNames(DISPATCHER_SERVLET);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean resetableRequestFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ResetableRequestFilter());
        registration.addUrlPatterns(URL_PATTERN);
        registration.setName(RESETABLE_REQUEST_FILTER);
        registration.setOrder(4);
        return registration;
    }

    @Bean
    public ResponseMonitoringService responseMonitoringService(final MetricRegistry metricRegistry) {
        return new ResponseMonitoringService(true, metricRegistry);
    }

    /**
     * Create http exception translator and map exceptions to http statuses
     * @see AuditlogExceptionTranslatorAdvice
     */
    @Bean
    public HttpExceptionTranslator httpExceptionTranslator() {
        final HttpExceptionTranslator httpExceptionTranslator = new HttpExceptionTranslator();
        httpExceptionTranslator.setComponent(COMPONENT_NAME);
        final Map<Integer, List<Class<? extends Exception>>> mapping = singletonMap(
            //map BindException and MethodArgumentTypeMismatchException (these exceptions are thrown due to type conversion error) to bad request status, because by default it is 5xx
            HttpStatus.SC_BAD_REQUEST, asList(BindException.class, MethodArgumentTypeMismatchException.class)
        );
        httpExceptionTranslator.setExceptionsToHTTPstatusMapping(mapping);
        return httpExceptionTranslator;
    }

    /**
     * Add interceptor for logging HTTP requests
     */
    @Bean
    public RequestMonitoringInterceptor requestMonitoringInterceptor(final MetricRegistry metricRegistry) {
        return new RequestMonitoringInterceptor(metricRegistry);
    }

    /**
     * register custom String to DateTime converter
     * @see StringToUTCDateTimeConverter
     */
    @Override
    public void addFormatters(final FormatterRegistry registry) {
        registry.addConverter(new StringToUTCDateTimeConverter());
    }

    /**
     * register interceptors
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(requestMonitoringInterceptor);
    }
}
