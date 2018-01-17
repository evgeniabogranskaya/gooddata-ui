/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.mail;

import com.gooddata.auditevent.AuditEvent;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.project.Project;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.function.Predicate;

/**
 * Test that scheduled mail produces event, when it is automatically triggered.
 */
public class ScheduleMailAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "SCHEDULED_MAIL_SEND";

    @BeforeClass(groups = MESSAGE_TYPE)
    public void setUp() throws Exception {
        final Project project = projectHelper.getOrCreateProject();
        scheduledMailHelper.runScheduledMail(project, metadataHelper.getOrCreateReport(project));
    }

    @AfterClass(groups = MESSAGE_TYPE)
    public void tearDown() throws Exception {
        scheduledMailHelper.clearScheduledMails();
    }

    @Test(groups = MESSAGE_TYPE)
    public void testScheduledMailSendMessageUserApi() throws Exception {
        doTestUserApi(eventCheck(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testScheduledMailSendMessageAdminApi() throws Exception {
        doTestAdminApi(eventCheck(), MESSAGE_TYPE);
    }

    private Predicate<AuditEvent> eventCheck() {
        return e -> e.getUserLogin().equals(getAccount().getLogin())
                && e.getType().equals(MESSAGE_TYPE)
                && e.isSuccess();
    }
}
