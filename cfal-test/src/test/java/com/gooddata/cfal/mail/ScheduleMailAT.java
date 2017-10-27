/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.mail;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

import com.gooddata.SimpleReportAttachment;
import com.gooddata.auditevent.AuditEvent;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.export.ExportFormat;
import com.gooddata.md.ScheduledMail;
import com.gooddata.project.Project;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Test that scheduled mail produces event, when it is automatically triggered.
 */
public class ScheduleMailAT extends AbstractAT {

    private static final String MESSAGE_TYPE = "SCHEDULED_MAIL_SEND";

    private ScheduledMail mail;
    private Project project;

    @BeforeClass(groups = MESSAGE_TYPE)
    public void setUp() throws Exception {
        project = projectHelper.getOrCreateProject();

        gd.getScheduledMailsAccelerateService().accelerateScheduledMailsForProject(project);

        final SimpleReportAttachment reportAttachment = new SimpleReportAttachment(
                metadataHelper.getOrCreateReport(project).getUri(),
                singletonList(ExportFormat.PDF.getValue())
        );

        mail = gd.getMetadataService().createObj(project,
                new ScheduledMail("CFAL", "mail summary")
                        .setStartDate(new LocalDate())
                        .setTimeZone(Calendar.getInstance().getTimeZone().getID())
                        .setRecurrency(createRecurrency())
                        .setSubject("mail subject")
                        .setBody("")
                        .setAttachments(singletonList(reportAttachment))
                        .addToAddress(gd.getAccountService().getCurrent().getEmail())
        );

        logger.info("wait {} seconds for scheduled mail to be send", props.getScheduledMailWaitSeconds());
        TimeUnit.SECONDS.sleep(props.getScheduledMailWaitSeconds());
    }

    @AfterClass(groups = MESSAGE_TYPE)
    public void tearDown() throws Exception {
        if (mail != null) {
            gd.getMetadataService().removeObj(mail);
            gd.getScheduledMailsAccelerateService().decelerateScheduledMailsForProject(project);
        }
    }

    @Test(groups = MESSAGE_TYPE)
    public void testScheduledMailSendMessageUserApi() throws Exception {
        doTestUserApi(eventCheck(), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testScheduledMailSendMessageAdminApi() throws Exception {
        doTestAdminApi(eventCheck(), MESSAGE_TYPE);
    }

    private String createRecurrency() {
        final LocalDateTime time = new LocalDateTime().plusMinutes(1); // +1 minute to be sure that mail schedule starts in future
        return format("0:0:0:*%d:%d:0", time.getHourOfDay(), time.getMinuteOfHour());
    }

    private Predicate<AuditEvent> eventCheck() {
        return e -> e.getUserLogin().equals(getAccount().getLogin())
                && e.getType().equals(MESSAGE_TYPE)
                && e.isSuccess();
    }
}
