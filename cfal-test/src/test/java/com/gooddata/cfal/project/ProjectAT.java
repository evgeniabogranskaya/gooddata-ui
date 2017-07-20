/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.project;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.AbstractProjectAT;
import com.gooddata.project.Invitation;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Predicate;

public class ProjectAT extends AbstractProjectAT {

    private static final String MESSAGE_TYPE = "INVITATION_SENT";
    private final String email = "qa+" + RandomStringUtils.randomAlphanumeric(10) + "@gooddata.com";

    @BeforeClass
    public void sendInvitation() throws Exception {
        final Invitation invitation = new Invitation(email);
        gd.getProjectService().sendInvitations(project, invitation);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testAddUserEventUserApi() throws Exception {
        doTestUserApi(pageCheckPredicate(MESSAGE_TYPE), MESSAGE_TYPE);
    }

    @Test(groups = MESSAGE_TYPE)
    public void testAddUserEventAdminApi() throws Exception {
        doTestAdminApi(pageCheckPredicate(MESSAGE_TYPE), MESSAGE_TYPE);
    }

    private Predicate<List<AuditEventDTO>> pageCheckPredicate(final String messageType) {
        return (auditEvents) -> auditEvents.stream()
                .anyMatch(e ->
                        account.getLogin().equals(e.getUserLogin()) &&
                        messageType.equals(e.getType()) &&
                        e.isSuccess() &&
                        email.equals(e.getParams().get("invited")) &&
                        project.getUri().equals(e.getLinks().get("project"))
                );
    }

}
