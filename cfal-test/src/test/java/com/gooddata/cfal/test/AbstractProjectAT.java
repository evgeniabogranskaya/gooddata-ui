/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.test;

import com.gooddata.FutureResult;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectEnvironment;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Common parent for all tests using a project. Uses an existing one or creates a new and optionally removes is.
 */
public abstract class AbstractProjectAT extends AbstractAT {

    private final String existingProjectId; // may be null
    private final String projectToken;
    private final boolean keepProject;
    protected static Project project;

    public AbstractProjectAT() {
        this.projectToken = getProperty("projectToken");
        final String projectId = getProperty("projectId");
        if (isBlank(projectId) && isBlank(projectToken)) {
            this.existingProjectId = "FoodMartDemo";
            this.keepProject = true;
        } else {
            this.keepProject = Boolean.getBoolean("keepProject");
            if (isNotBlank(projectId)) {
                this.existingProjectId = projectId;
            } else {
                this.existingProjectId = null;
            }
        }
    }

    @BeforeSuite
    public void getOrCreateProject() throws Exception {
        if (existingProjectId == null) {
            project = createProject(projectToken);
        } else {
            project = gd.getProjectService().getProjectById(existingProjectId);
        }
        logger.info("Using project_id={}", project.getId());

    }

    private Project createProject(final String projectToken) {
        final Project project = new Project("CFAL Test", projectToken);
        project.setEnvironment(ProjectEnvironment.TESTING);
        final FutureResult<Project> result = gd.getProjectService().createProject(project);
        logger.info("Creating project uri={}", result.getPollingUri());
        return result.get(POLL_TIMEOUT, POLL_TIMEOUT_UNIT);
    }

    @AfterSuite
    public void tearDownProject() throws Exception {
        if (project != null) {
            if (keepProject) {
                logger.debug("Keeping project_id={}", project.getId());
            } else {
                logger.debug("Removing project_id={}", project.getId());
                gd.getProjectService().removeProject(project);
            }
        }
    }
}