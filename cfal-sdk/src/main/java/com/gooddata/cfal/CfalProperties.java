/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Defines set of parameters CFAL with some handy defaults.
 */
@ConfigurationProperties("gdc.cfal")
public class CfalProperties {
    private String cfalDir;
    private String component;
    private boolean enabled = false;
    private CfalServiceType serviceType = CfalServiceType.CONCURRENT;

    public CfalProperties() {
    }

    public CfalProperties(String component, boolean enabled, CfalServiceType serviceType) {
        setEnabled(enabled);
        setComponent(component);
        setServiceType(serviceType);
    }

    public String getCfalDir() {
        return cfalDir;
    }

    public void setCfalDir(String cfalDir) {
        this.cfalDir = trimToNull(cfalDir);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = notBlank(component, "component");
    }

    public CfalServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(CfalServiceType serviceType) {
        this.serviceType = notNull(serviceType, "serviceType");
    }

    /**
     * List of all supported CFAL-loggers
     */
    public enum CfalServiceType {
        NOOP, SIMPLE, CONCURRENT
    }
}
