/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.exception;

import static org.apache.commons.lang3.Validate.notNull;

import com.gooddata.exception.servlet.ErrorStructure;
import com.gooddata.exception.servlet.HandlerExceptionResolver;
import com.gooddata.exception.servlet.HttpExceptionTranslator;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * Converts exceptions thrown inside controller to ErrorStructure
 * Needed because of the RestControllerAdvice annotation, which is not in HandlerExceptionResolver.Exceptions are not translated from controllers otherwise.
 *
 * If <i>Exception</i> is a <i>BindException</i>, then <i>BindException</i> is converted to {@link ValidationException} with custom exception
 * message, message is either taken from <i>message.properties</i> based on error code of <i>BindException</i>'s fieldError or it is
 * created from default message of <i>BindException</i>'s fieldError (if there is no entry for error code in <i>message.properties</i>).
 * If <i>BindException</i> has multiple field errors specified, then first field error is taken and made into custom message of <i>ValidationException</i>.
 * Global errors specified in <i>BindException</i> are not handled at all.
 */
@RestControllerAdvice
public class AuditlogExceptionTranslatorAdvice extends HandlerExceptionResolver {

    private final MessageSource messageSource;

    public AuditlogExceptionTranslatorAdvice(final HttpExceptionTranslator translator, final MessageSource messageSource) {
        super(translator);
        this.messageSource = notNull(messageSource, "messageSource cannot be null");
    }

    @ExceptionHandler(value = {UserNotFoundException.class, DomainNotFoundException.class})
    protected ResponseEntity<ErrorStructure> translateNotFoundExceptionToUnauthorized(final HttpServletRequest request, final Exception ex) {
        return doResolveException(request, new UserNotAuthorizedException("User not authorized", ex));
    }


    @Override
    protected ResponseEntity<ErrorStructure> doResolveException(final HttpServletRequest request, final Exception ex) {
        if(ex instanceof BindException) {
            final BindException bindException = (BindException) ex;
            final FieldError fieldError = bindException.getFieldError(); //only first error is handled
            String message = "Validation error";
            if(fieldError != null) {
                final Object[] args = new Object[]{fieldError.getField(), fieldError.getRejectedValue()};
                final DefaultMessageSourceResolvable messageSourceResolvable =
                        new DefaultMessageSourceResolvable(fieldError.getCodes(), args, fieldError.getDefaultMessage());
                message = messageSource.getMessage(messageSourceResolvable, null); //get message based on field error code or take it's default message
            }
            return super.doResolveException(request, new ValidationException(message, ex));
        }
        return super.doResolveException(request, ex);
    }
}
