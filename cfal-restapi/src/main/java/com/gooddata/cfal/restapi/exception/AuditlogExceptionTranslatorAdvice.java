/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.exception;

import com.gooddata.exception.servlet.HandlerExceptionResolver;
import com.gooddata.exception.servlet.HttpExceptionTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts exceptions thrown inside controller to ErrorStructure
 * Needed because of the RestControllerAdvice annotation, which is not in HandlerExceptionResolver.
 * Exceptions are not translated from controllers otherwise
 */
@RestControllerAdvice
public class AuditlogExceptionTranslatorAdvice extends HandlerExceptionResolver {

    @Autowired
    public AuditlogExceptionTranslatorAdvice(final HttpExceptionTranslator translator) {
        super(translator);
    }
}
