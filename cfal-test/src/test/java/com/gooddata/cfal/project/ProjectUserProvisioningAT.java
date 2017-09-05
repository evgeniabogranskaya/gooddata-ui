/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.project;

import com.gooddata.account.Account;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.project.Project;
import com.gooddata.project.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.function.Predicate;

public class ProjectUserProvisioningAT extends AbstractAT {

    private static final String USER_ADD_MESSAGE_TYPE = "PROJECT_USER_ADD";
    private static final String STATUS_CHANGE_MESSAGE_TYPE = "PROJECT_USER_STATUS_CHANGE";

    private Account addedUser;

    @BeforeClass(groups = {USER_ADD_MESSAGE_TYPE, STATUS_CHANGE_MESSAGE_TYPE})
    public void setUp() {
        addedUser = accountHelper.getOrCreateUser();
        final Project project = projectHelper.getOrCreateProject();
        final User user = gd.getProjectService().addUserToProject(project, addedUser);

        user.setStatus("DISABLED");

        gd.getProjectService().updateUserInProject(project, user);
    }

    @Test(groups = USER_ADD_MESSAGE_TYPE)
    public void testAddUserToProjectMessageUserApi() throws Exception {
        doTestUserApi(eventCheck(USER_ADD_MESSAGE_TYPE), USER_ADD_MESSAGE_TYPE);
    }

    @Test(groups = USER_ADD_MESSAGE_TYPE)
    public void testAddUserToProjectMessageAdminApi() throws Exception {
        doTestAdminApi(eventCheck(USER_ADD_MESSAGE_TYPE), USER_ADD_MESSAGE_TYPE);
    }

    @Test(groups = STATUS_CHANGE_MESSAGE_TYPE)
    public void testProjectUserStatusChangeMessageUserApi() throws Exception {
        doTestUserApi(eventCheck(STATUS_CHANGE_MESSAGE_TYPE), STATUS_CHANGE_MESSAGE_TYPE);
    }

    @Test(groups = STATUS_CHANGE_MESSAGE_TYPE)
    public void testProjectUserStatusChangeMessageAdminApi() throws Exception {
        doTestAdminApi(eventCheck(STATUS_CHANGE_MESSAGE_TYPE), STATUS_CHANGE_MESSAGE_TYPE);
    }

    private Predicate<AuditEventDTO> eventCheck(final String messageType) {
        return (e ->
                        getAccount().getLogin().equals(e.getUserLogin()) &&
                                messageType.equals(e.getType()) &&
                                addedUser.getUri().equals(e.getLinks().get("profile"))
                );
    }
}
