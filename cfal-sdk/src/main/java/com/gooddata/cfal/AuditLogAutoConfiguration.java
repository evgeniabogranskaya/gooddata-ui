/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;


import com.gooddata.cfal.CfalProperties.CfalServiceType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isEmpty;


/**
 * Spring configuration defining {@link AuditLogService}.
 */
@Configuration
@ConditionalOnBean(annotation = EnableCfal.class)
@ConditionalOnMissingBean({AuditLogService.class})
@EnableConfigurationProperties({CfalProperties.class})
public class AuditLogAutoConfiguration {

    @Bean
    public AbstractAuditLogService cfalAuditLogService(final CfalProperties cfalProperties, final AuditLogEventWriter auditLogEventWriter) throws IOException {
        final AbstractAuditLogService auditLogService = createAuditLogService(cfalProperties, auditLogEventWriter);
        auditLogService.setLoggingEnabled(cfalProperties.isEnabled());

        return auditLogService;
    }

    /**
     * Creates a new {@link AuditLogEventWriter} based on given configuration
     */
    @Bean
    public AuditLogEventWriter auditLogEventWriter(final CfalProperties props) throws IOException {
        if (props.getServiceType() == CfalServiceType.NOOP) {
            return new Slf4jAuditLogEventWriter();
        }
        if (isEmpty(props.getCfalDir())) {
            return new AuditLogEventFileWriter(props.getComponent());
        } else {
            return new AuditLogEventFileWriter(new File(props.getCfalDir()), props.getComponent());
        }
    }

    /**
     * Creates new instance of the {@link AuditLogService} based on given configuration
     */
    private AbstractAuditLogService createAuditLogService(final CfalProperties cfalProperties,
                                                          final AuditLogEventWriter auditLogEventWriter) throws IOException {
        final CfalServiceType serviceType = cfalProperties.getServiceType();
        switch (serviceType) {
            case NOOP:
                return new SimpleAuditLogService(cfalProperties.getComponent(), auditLogEventWriter);
            case CONCURRENT:
                return new ConcurrentAuditLogService(cfalProperties.getComponent(), auditLogEventWriter);
            case SIMPLE:
                return new SimpleAuditLogService(cfalProperties.getComponent(), auditLogEventWriter);
            default:
                throw new IllegalStateException("Unable to create audit log service with type: " + serviceType);
        }
    }
}
