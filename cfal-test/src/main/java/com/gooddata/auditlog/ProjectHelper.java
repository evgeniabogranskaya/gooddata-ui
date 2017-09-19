/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.FutureResult;
import com.gooddata.GoodData;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Common parent for all tests using a project. Uses an existing one or creates a new and optionally removes is.
 */
public class ProjectHelper {

    private static final Logger logger = LoggerFactory.getLogger(ProjectHelper.class);

    private static ProjectHelper instance;

    private final String existingProjectId; // may be null
    private final boolean keepProject;
    private final GoodData gd;
    private final TestEnvironmentProperties props;
    private Project project;
    private final List<Project> projects = new LinkedList<>();

    public static ProjectHelper getInstance(final GoodData gd, final TestEnvironmentProperties props) {
        if (instance == null) {
            instance = new ProjectHelper(gd, props);
        }
        return instance;
    }

    public ProjectHelper(final GoodData gd, final TestEnvironmentProperties props) {
        this.gd = notNull(gd, "gd");
        this.props = notNull(props, "props");

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

    public Project getOrCreateProject() {
        if (project != null) {
            return project;
        }
        if (existingProjectId == null) {
            project = createProject(props.getProjectToken());
            logger.info("Created project_id={}", project.getId());
        } else {
            project = gd.getProjectService().getProjectById(existingProjectId);
            logger.info("Using project_id={}", project.getId());
        }
        return project;
    }

    public Project createProject() {
        final Project project = createProject(props.getProjectToken());
        projects.add(project);
        return project;
    }

    private Project createProject(final String projectToken) {
        final Project project = new Project("CFAL Test", projectToken);
        project.setEnvironment(ProjectEnvironment.TESTING);
        final FutureResult<Project> result = gd.getProjectService().createProject(project);
        logger.info("Creating project uri={}", result.getPollingUri());
        return result.get(props.getPollTimeoutMinutes(), props.getPollTimeoutUnit());
    }

    public void destroy() {
        if (project != null) {
            if (keepProject) {
                logger.debug("Keeping project_id={}", project.getId());
            } else {
                logger.debug("Removing project_id={}", project.getId());
                gd.getProjectService().removeProject(project);
            }
        }

        projects.forEach(e -> {
            try {
                gd.getProjectService().removeProject(e);
                logger.info("removed project_id={}", e.getId());
            } catch (Exception ex) {
                logger.warn("could not remove project_id={}", e.getId());
            }
        });
    }
}
