/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.CfalGoodData;
import com.gooddata.FutureResult;
import com.gooddata.GoodData;
import com.gooddata.dataload.OutputStage;
import com.gooddata.model.ModelDiff;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectEnvironment;
import com.gooddata.warehouse.Warehouse;
import com.gooddata.warehouse.WarehouseSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Singleton for manipulating with projects. Uses an existing one or creates a new and optionally removes is.
 * Lazy initialized. Not thread safe.
 */
public class ProjectHelper {

    private static final Logger logger = LoggerFactory.getLogger(ProjectHelper.class);

    private static final String PROJECT_MODEL_JSON = "/model.json";

    private static ProjectHelper instance;

    private final String existingProjectId; // may be null
    private final boolean keepProject;
    private final GoodData gd;
    private final TestEnvironmentProperties props;
    private Project project;
    private final List<Project> projects = new LinkedList<>();

    public static ProjectHelper getInstance() {
        if (instance == null) {
            final TestEnvironmentProperties props = TestEnvironmentProperties.getInstance();
            final CfalGoodData gd = CfalGoodData.getInstance();
            instance = new ProjectHelper(gd, props);
        }
        return instance;
    }

    private ProjectHelper(final GoodData gd, final TestEnvironmentProperties props) {
        this.gd = notNull(gd, "gd");
        this.props = notNull(props, "props");

        final String projectId = props.getProjectId();
        if (isBlank(projectId) && isBlank(props.getProjectToken())) {
            this.existingProjectId = "defaultEmptyProject";
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

    /**
     * Sets up given project to the default project model (adds Person and City datasets).
     *
     * @param project GD project where the model should be set up
     */
    public void setupDefaultModel(final Project project) {
        notNull(project, "project cannot be null!");

        final ModelDiff projectModelDiff = gd.getModelService().getProjectModelDiff(project,
                new InputStreamReader(getClass().getResourceAsStream(PROJECT_MODEL_JSON))).get();
        if (!projectModelDiff.getUpdateMaql().isEmpty()) {
            gd.getModelService().updateProjectModel(project, projectModelDiff).get();
        }
        logger.info("updated model of project_id={}", project.getId());
    }

    /**
     * Sets up the OS for the given project by connecting that with the given ADS.
     *
     * @param project GD project
     * @param warehouse ADS warehouse
     */
    public void setupOutputStage(final Project project, final Warehouse warehouse) {
        notNull(project, "project cannot be null!");
        notNull(warehouse, "warehouse cannot be null!");

        final OutputStage outputStage = gd.getOutputStageService().getOutputStage(project);
        final WarehouseSchema schema = gd.getWarehouseService().getDefaultWarehouseSchema(warehouse);

        outputStage.setSchemaUri(schema.getUri());

        gd.getOutputStageService().updateOutputStage(outputStage);
        logger.info("output stage of project_id={} is now set to warehouse_id={}", project.getId(), warehouse.getId());
    }

    /**
     * Clears OS for the given project by setting the ADS to unknown ({@code null}) value.
     *
     * @param project GD project
     */
    public void clearOutputStage(final Project project) {
        notNull(project, "project cannot be null!");

        final OutputStage outputStage = gd.getOutputStageService().getOutputStage(project);

        outputStage.setSchemaUri(null);

        gd.getOutputStageService().updateOutputStage(outputStage);
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
