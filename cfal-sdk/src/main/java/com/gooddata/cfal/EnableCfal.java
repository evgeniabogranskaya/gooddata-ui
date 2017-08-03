/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides auto-configuration of {@link com.gooddata.cfal.AuditLogService} when added to a Spring Boot application.
 * Example:
 * <pre>
 * {@code
 *
 * @literal @SpringBootApplication
 * @literal @EnableCfal
 *  public class MyApplication {
 *  }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface EnableCfal {
}
