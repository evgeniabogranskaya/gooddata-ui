/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import com.gooddata.auditevent.AuditEvent;
import com.gooddata.auditevent.AuditEvents;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IpMaskingServiceTest {

    private static final String IP = "127.0.0.1";
    private static final String IP_1 = "1";
    private static final String IP_2 = "2";
    private static final String IP_3 = "3";
    private static final String ID = "ID";
    private static final String USER = "USER";
    private static final String TYPE = "TYPE";
    private static final Map<String, String> PARAMS = Collections.emptyMap();
    private static final Map<String, String> LINKS = Collections.emptyMap();
    private static final DateTime OCCURRED = new DateTime();
    private static final DateTime RECORDED = new DateTime();

    private IpMaskingService ipMaskingService;

    @Before
    public void setUp() throws Exception {
        ipMaskingService = new IpMaskingService(new HashSet<>(Arrays.asList(IP_1, IP_2)));
    }

    @Test
    public void testMasksIps() throws Exception {
        final AuditEvent event1 = new AuditEvent(ID, USER, OCCURRED, RECORDED, IP_1, true, TYPE, PARAMS, LINKS);
        final AuditEvent event2 = new AuditEvent(ID, USER, OCCURRED, RECORDED, IP_3, true, TYPE, PARAMS, LINKS);
        final AuditEvent event3 = new AuditEvent(ID, USER, OCCURRED, RECORDED, IP_2, true, TYPE, PARAMS, LINKS);

        final List<AuditEvent> result = ipMaskingService.maskIps(new AuditEvents(Arrays.asList(event1, event2, event3), null, null));
        final List<String> ips = result.stream().map(e -> e.getUserIp()).collect(Collectors.toList());

        assertThat(result, hasSize(3));
        assertThat(ips, contains(IP, IP_3, IP));
    }
}
