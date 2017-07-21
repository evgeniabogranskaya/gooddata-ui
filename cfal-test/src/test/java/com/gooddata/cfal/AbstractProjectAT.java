/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.gooddata.FutureResult;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectEnvironment;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Common parent for all tests using a project. Uses an existing one or creates a new and optionally removes is.
 */
public abstract class AbstractProjectAT extends AbstractAT {

    private final String existingProjectId; // may be null
    private final boolean keepProject;
    protected static Project project;

    public AbstractProjectAT() {
        final String projectId = props.getProjectId();
        if (isBlank(projectId) && isBlank(props.getProjectToken())) {
            this.existingProjectId = "FoodMartDemo";
            this.keepProject = true;
        } else {
            this.keepProject = props.getKeepProject();
            if (isNotBlank(projectId)) {
                this.existingProjectId = projectId;
            } else {
                this.existingProjectId = null;
            }
        }
    }

    @BeforeSuite(alwaysRun = true)
    public void getOrCreateProject() throws Exception {
        if (existingProjectId == null) {
            project = createProject(props.getProjectToken());
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
        return result.get(props.getPollTimeoutMinutes(), props.getPollTimeoutUnit());
    }

    @AfterSuite(alwaysRun = true)
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
