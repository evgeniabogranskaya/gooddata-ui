/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.CfalGoodData;
import com.gooddata.FutureResult;
import com.gooddata.dataload.OutputStage;
import com.gooddata.model.ModelDiff;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectEnvironment;
import com.gooddata.test.ssh.CommandResult;
import com.gooddata.test.ssh.SshClient;
import com.gooddata.warehouse.Warehouse;
import com.gooddata.warehouse.WarehouseSchema;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton for manipulating with projects. Uses an existing one or creates a new and optionally removes is.
 * Lazy initialized. Not thread safe.
 */
public class ProjectHelper {

    private static final Logger logger = LoggerFactory.getLogger(ProjectHelper.class);

    private static final String PROJECT_MODEL_JSON = "/model.json";
    private static final String CFAL_PROJECT_NAME_PREFIX = "CFAL Test";
    private static final String CFAL_PROJECT_NAME_FORMAT = "%s-%s";

    private static ProjectHelper instance;

    private final String existingProjectId; // may be null
    private final boolean keepProject;
    private final CfalGoodData gd;
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

    private ProjectHelper(final CfalGoodData gd, final TestEnvironmentProperties props) {
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
        final Project project = new Project(
                String.format(CFAL_PROJECT_NAME_FORMAT, CFAL_PROJECT_NAME_PREFIX, new DateTime().toInstant().getMillis()),
                projectToken
        );
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

        logger.info("clearing output stage of project_id={}", project.getId());

        final OutputStage outputStage = gd.getOutputStageService().getOutputStage(project);

        outputStage.setSchemaUri(null);

        gd.getOutputStageService().updateOutputStage(outputStage);
    }

    /**
     * Enables public access for the given project.
     * <br/>
     * We have to enable 'canManagePublicAccessCode' permission for project admin to be able to do that. For
     * this purpose we need given ssh connection.
     * <br/>
     * See <a href="https://confluence.intgdc.com/display/plat/Public+Access+Codes+management">Public Access Codes management</a>
     * page and {@link com.gooddata.ExtendedProjectService#enablePublicAccess(Project)} for more info.
     *
     * @param project GD project which should be enabled for public access
     * @param ssh ssh connection - must be sudo user
     */
    public void enablePublicAccess(final Project project, final SshClient ssh) {
        final String cmd = String.format("sudo /opt/common/util/roles.pl -p %s adminRole canManagePublicAccessCode 1",
                project.getId());
        final CommandResult cmdRes = ssh.execCmd(cmd);
        if (cmdRes.getExitCode() != 0) {
            throw new IllegalStateException("Could not add 'canManagePublicAccessCode' permission to 'adminRole' of "
                    + "project: " + project.getId() + ", error: " + cmdRes.getStderr());
        }

        gd.getProjectService().enablePublicAccess(project);
    }

    /**
     * finds all cfal projects and delete those older than 1 hour
     */
    public void preDestroy() {
        final List<Project> cfalProjects = gd.getProjectService().getProjects()
                .stream()
                .filter(project -> project.getTitle().contains(CFAL_PROJECT_NAME_PREFIX))
                .collect(Collectors.toList());

        for (Project project : cfalProjects) {
            if (project.getTitle().equals(CFAL_PROJECT_NAME_PREFIX)) {
                logger.info("found cfal project_id={} to be deleted", project.getId());
                removeProject(project);
            } else if (project.getTitle().matches(CFAL_PROJECT_NAME_PREFIX + "-.*")) {
                logger.info("found cfal project_id={} to be deleted", project.getId());

                final String[] split = project.getTitle().split("-");
                final Instant projectTime = new Instant(Long.parseLong(split[1]));
                if (projectTime.isBefore(new DateTime().minusHours(1))) {
                    removeProject(project);
                }
            }
        }
    }

    public void destroy() {
        if (project != null) {
            if (keepProject) {
                logger.debug("Keeping project_id={}", project.getId());
                clearOutputStage(project);
            } else {
                removeProject(project);
                project = null;
            }
        }

        projects.forEach(this::removeProject);
        projects.clear();
    }

    private void removeProject(final Project project) {
        try {
            logger.info("removing project_id={}", project.getId());
            clearOutputStage(project);
            gd.getProjectService().removeProject(project);
            logger.info("removed project_id={}", project.getId());
        } catch (Exception ex) {
            logger.warn("could not remove project_id=" + project.getId(), ex);
        }
    }
}
