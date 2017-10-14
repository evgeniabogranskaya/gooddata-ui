/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import com.gooddata.project.Project;
import org.springframework.web.client.RestTemplate;

/**
 * Service for internal API, which manipulates PSS jobs for enqueueing mails to send. Normally emails are send only every 30 minute, but for tests we
 * need them to be send faster.
 */
public class ScheduledMailsAccelerateService extends AbstractService {

    private final String URI = "/gdc/internal/projects/{projectId}/scheduledMails/accelerate";

    public ScheduledMailsAccelerateService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    /**
     * internal API which wraps PSS REST API and adds for given project a pss job, which triggers queuing of emails every minute
     * @param project to accelerate scheduled emails for
     */
    public void accelerateScheduledMailsForProject(final Project project) {
        restTemplate.put(URI, null, project.getId());
    }

    /**
     * returns enqueueing of emails back to normal
     * @param project to decelerate scheduled emails for
     */
    public void decelerateScheduledMailsForProject(final Project project) {
        restTemplate.delete(URI, project.getId());
    }
}
