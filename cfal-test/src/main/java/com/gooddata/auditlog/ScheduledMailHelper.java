/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.auditlog;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.Validate.notNull;

import com.gooddata.CfalGoodData;
import com.gooddata.SimpleReportAttachment;
import com.gooddata.export.ExportFormat;
import com.gooddata.md.ScheduledMail;
import com.gooddata.md.report.Report;
import com.gooddata.project.Project;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Singleton for scheduled emails stuff.
 * Lazy initialized. Not thread safe.
 */
public class ScheduledMailHelper {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledMailHelper.class);

    private static ScheduledMailHelper instance;

    private final TestEnvironmentProperties props;
    private final CfalGoodData gd;
    private final Set<Project> acceleratedProjects = new HashSet<>();
    private final Set<ScheduledMail> scheduledMails = new HashSet<>();

    private ScheduledMailHelper(final TestEnvironmentProperties props, final CfalGoodData gd) {
        this.props = props;
        this.gd = gd;
    }

    public static ScheduledMailHelper getInstance() {
        if (instance == null) {
            final TestEnvironmentProperties props = TestEnvironmentProperties.getInstance();
            final CfalGoodData gd = CfalGoodData.getInstance();
            ScheduledMailHelper.instance = new ScheduledMailHelper(props, gd);
        }
        return instance;
    }

    /**
     * Runs scheduled email for the given project and report. This turns on scheduled emails acceleration for projects
     * which don't have this acceleration enabled.
     * See {@link com.gooddata.ScheduledMailsAccelerateService#accelerateScheduledMailsForProject(Project)} for more
     * details.
     *
     * @param project GD project
     * @param report GD report
     */
    public void runScheduledMail(final Project project, final Report report) throws Exception {
        notNull(project, "project cannot be null!");
        notNull(report, "report cannot be null!");

        if (!acceleratedProjects.contains(project)) {
            gd.getScheduledMailsAccelerateService().accelerateScheduledMailsForProject(project);
            acceleratedProjects.add(project);
        }

        final SimpleReportAttachment reportAttachment = new SimpleReportAttachment(
                report.getUri(),
                singletonList(ExportFormat.PDF.getValue())
        );

        final ScheduledMail mail = gd.getMetadataService().createObj(project,
                new ScheduledMail("CFAL", "mail summary")
                        .setStartDate(new LocalDate())
                        .setTimeZone(Calendar.getInstance().getTimeZone().getID())
                        .setRecurrency(createRecurrency())
                        .setSubject("mail subject")
                        .setBody("")
                        .setAttachments(singletonList(reportAttachment))
                        .addToAddress(gd.getAccountService().getCurrent().getEmail())
        );
        scheduledMails.add(mail);

        logger.info("wait {} seconds for scheduled mail to be send", props.getScheduledMailWaitSeconds());
        TimeUnit.SECONDS.sleep(props.getScheduledMailWaitSeconds());
    }

    /**
     * Removes given scheduled email if it exists.
     *
     * @param mail scheduled email or {@link null}
     */
    public void removeMail(final ScheduledMail mail) {
        notNull(mail, "mail cannot be null!");

        if (scheduledMails.contains(mail)) {
            gd.getMetadataService().removeObj(mail);
            scheduledMails.remove(mail);
        }
    }

    /**
     * Turns off scheduled emails acceleration for the given project if this project has acceleration enabled.
     *
     * @param project GD project
     */
    public void decelerateForProject(final Project project) {
        notNull(project, "project cannot be null!");

        if (acceleratedProjects.contains(project)) {
            gd.getScheduledMailsAccelerateService().decelerateScheduledMailsForProject(project);
            acceleratedProjects.remove(project);
        }
    }

    /**
     * Removes all scheduled mails.
     *
     * @see #removeMail(ScheduledMail)
     */
    public void clearScheduledMails() {
        scheduledMails.forEach(this::removeMail);
    }

    /**
     * Decelerates all accelerated projects.
     *
     * @see #decelerateForProject(Project)
     */
    public void decelerateAllProjects() {
        acceleratedProjects.forEach(this::decelerateForProject);
    }

    /**
     * Removes all scheduled mails and decelerates all accelerated projects.
     *
     * @see #clearScheduledMails()
     * @see #decelerateAllProjects()
     */
    public void destroy() {
        clearScheduledMails();
        decelerateAllProjects();
    }

    private String createRecurrency() {
        final LocalDateTime time = new LocalDateTime().plusMinutes(1); // +1 minute to be sure that mail schedule starts in future
        return format("0:0:0:*%d:%d:0", time.getHourOfDay(), time.getMinuteOfHour());
    }
}
