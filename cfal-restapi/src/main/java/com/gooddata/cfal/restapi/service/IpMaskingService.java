/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import static org.apache.commons.lang3.Validate.notNull;

import com.gooddata.auditevent.AuditEvent;
import com.gooddata.auditevent.AuditEvents;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for masking given IPs (replacing them for 127.0.0.1) in audit events
 */
@Service
public class IpMaskingService {

    private static final String MASKED_IP = "127.0.0.1";

    private final Set<String> ips;

    /**
     * @param ips to mask in audit events
     */
    public IpMaskingService(@Value("${gdc.cfal.mask.ips:}#{T(java.util.Collections).emptySet()}") final Set<String> ips) {
        this.ips = notNull(ips, "ips cannot be null");
    }

    /**
     * Mask ips of audit events. Passed events are checked, whether they IPs match provided IPs to mask and if they
     * match their IPs are replaced by 127.0.0.1
     *
     * @param events to mask their IPs
     * @return events with masked IPs if they matched list of IPs to mask
     */
    public AuditEvents maskIps(final AuditEvents events) {
        List<AuditEvent> eventsWithMaskedIp = events
                .stream()
                .map(e -> {
                    if (ips.contains(e.getUserIp())) {
                        return hideAuditEventIp(e);
                    }
                    return e;
                })
                .collect(Collectors.toList());

        return new AuditEvents(eventsWithMaskedIp, events.getPaging(), events.getLinks());
    }

    private AuditEvent hideAuditEventIp(final AuditEvent e) {
        return new AuditEvent(e.getId(), e.getUserLogin(), e.getOccurred(), e.getRecorded(), MASKED_IP, e.isSuccess(), e.getType(), e.getParams(), e.getLinks());
    }
}
