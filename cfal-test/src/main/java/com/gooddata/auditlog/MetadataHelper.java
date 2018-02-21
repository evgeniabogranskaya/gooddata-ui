/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.CfalGoodData;
import com.gooddata.GoodData;
import com.gooddata.dataset.DatasetService;
import com.gooddata.md.*;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.model.ModelService;
import com.gooddata.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Singleton for metadata related stuff.
 * Lazy initialized. Not thread safe.
 */
public class MetadataHelper {

    private static final Logger logger = LoggerFactory.getLogger(MetadataHelper.class);

    private static final String DATASET_NAME = "dataset.star";

    private static MetadataHelper instance;

    // indexed by project id
    private final Map<String, ProjectMetadataState> projectMetadataStates = new HashMap<>();
    private final Map<String, ProjectDashboard> projectDashboards = new HashMap<>();

    private final MetadataService metadataService;
    private final ModelService modelService;
    private final DatasetService datasetService;

    private MetadataHelper(final GoodData gd) {
        notNull(gd, "gd");
        this.metadataService = gd.getMetadataService();
        this.modelService = gd.getModelService();
        this.datasetService = gd.getDatasetService();
    }

    public static MetadataHelper getInstance() {
        if (instance == null) {
            instance = new MetadataHelper(CfalGoodData.getInstance());
        }
        return instance;
    }

    public Report getOrCreateReport(final Project project) {
        final ProjectMetadataState projectMetadataState = getOrCreateProjectMetadataState(project);
        if (projectMetadataState.getReport() == null) {
            createMetadata(project);
        }
        return projectMetadataState.getReport();
    }

    public ReportDefinition getOrCreateReportDefinition(final Project project) {
        final ProjectMetadataState projectMetadataState = getOrCreateProjectMetadataState(project);
        if (projectMetadataState.getReportDefinition() == null) {
            createMetadata(project);
        }
        return projectMetadataState.getReportDefinition();
    }

    /**
     * Gets existing or creates new CFAL testing dashboard for the given project.
     * There's no need for creating more dashboards for individual testing projects so one dashboard per project is
     * enough.
     *
     * @param project GD project
     * @return project dashboard
     */
    public ProjectDashboard getOrCreateDashboard(final Project project) {
        notNull(project, "project cannot be null!");

        ProjectDashboard dashboard = projectDashboards.get(project.getId());
        if (dashboard == null) {
            final ProjectDashboard newDashboard =
                    new ProjectDashboard("CFAL testing dashboard", new ProjectDashboard.Tab("CFAL testing tab"));

            dashboard = metadataService.createObj(project, newDashboard);
            projectDashboards.put(project.getId(), dashboard);
        }
        return dashboard;
    }

    /**
     * Removes default CFAL testing dashboard from the given project.
     *
     * @param project GD project
     */
    public void removeDashboard(final Project project) {
        removeDashboard(project.getId());
    }

    /**
     * Clears all metadata garbage stuff created during acceptance tests.
     */
    public void destroy() {
        //we must create new list from ids first to avoid concurrent modification of the HashMap
        final List<String> projects = new ArrayList<>(projectDashboards.keySet());
        projects.forEach(this::removeDashboard);
    }

    private void removeDashboard(final String projectId) {
        final ProjectDashboard dashboard = projectDashboards.get(projectId);
        if (dashboard != null) {
            metadataService.removeObj(dashboard);
            projectDashboards.remove(projectId);
        }
    }

    private void createMetadata(final Project project) {
        getObjOrRunMAQL(project, Dataset.class, DATASET_NAME,
                "CREATE DATASET {dataset.star} VISUAL(TITLE \"Stars\", DESCRIPTION \"Movie Stars\")"
        );

        final Attribute attr = getObjOrRunMAQL(project, Attribute.class, "attr.star.name",
                "CREATE ATTRIBUTE {attr.star.name} VISUAL(TITLE \"Department\") AS {f_star.id} FULLSET;",
                "ALTER DATASET {dataset.star} ADD {attr.star.name};",
                "ALTER ATTRIBUTE {attr.star.name} ADD LABELS {label.star.name} VISUAL(TITLE \"Name\") AS {f_star.nm_name};"
        );

        final Fact fact = getObjOrRunMAQL(project, Fact.class, "fact.star.size",
                "CREATE FACT {fact.star.size} VISUAL(TITLE \"Star Boobs Size\") AS {f_star.f_size};",
                "ALTER DATASET {dataset.star} ADD {fact.star.size};");

        final Metric metric = getObjOrCreateUsingAPI(project, Metric.class, "metric.avgsize",
                () -> new Metric("Avg size", "SELECT AVG([" + fact.getUri() + "])", "#,##0")
        );

        final ProjectMetadataState projectMetadataState = getOrCreateProjectMetadataState(project);
        projectMetadataState.setReportDefinition(getObjOrCreateUsingAPI(project, ReportDefinition.class, "reportdefinition.avgsize",
                () -> GridReportDefinitionContent.create(
                        "Star avg size",
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(attr.getDefaultDisplayForm())),
                        singletonList(new MetricElement(metric, "Avg size")),
                        singletonList(new Filter("(SELECT [" + metric.getUri() + "]) >= 0"))
                )
        ));

        projectMetadataState.setReport(getObjOrCreateUsingAPI(project, Report.class, "report.avgsize",
                () -> new Report(projectMetadataState.getReportDefinition().getTitle(), projectMetadataState.getReportDefinition())
        ));

        if (projectMetadataState.isNeedSynchronize()) {
            logger.info("synchronizing dataset={} in project={}", DATASET_NAME, project.getId());
            modelService.updateProjectModel(project, "SYNCHRONIZE {dataset.star};").get();
        }
    }

    private <T extends AbstractObj & Queryable> T getObjOrRunMAQL(final Project project,
                                                                  final Class<T> cls,
                                                                  final String identifier,
                                                                  final String... maql) {
        return getObjOrCreate(project, cls, identifier, () -> {
                    logger.info("Running MAQL to create type={} identifier={} for project={}", cls.getSimpleName(), identifier, project.getId());
                    modelService.updateProjectModel(project, maql).get();
                    getOrCreateProjectMetadataState(project).setNeedSynchronize(true);
                    return getObjOrCreate(project, cls, identifier,
                            () -> {
                                throw new IllegalStateException("Unable to find created object: " + identifier);
                            }
                    );
                }
        );
    }

    private <T extends AbstractObj & Queryable> T getObjOrCreateUsingAPI(final Project project,
                                                                         final Class<T> cls,
                                                                         final String identifier,
                                                                         final Supplier<T> creator) {
        return getObjOrCreate(project, cls, identifier,
                () -> {
                    final String type = cls.getSimpleName().toLowerCase();
                    final T obj = creator.get();
                    obj.setIdentifier(identifier);
                    logger.info("Creating obj type={} identifier={}", type, identifier);
                    final T created = metadataService.createObj(project, obj);
                    logger.info("Created obj type={} identifier={} uri={}", type, identifier, created.getUri());
                    return created;
                }
        );
    }

    private <T extends AbstractObj & Queryable> T getObjOrCreate(final Project project,
                                                                 final Class<T> cls,
                                                                 final String identifier,
                                                                 final Supplier<T> creator) {
        final Collection<Entry> entries = metadataService.find(project, cls, identifier(identifier));
        if (entries.size() == 1) {
            final Entry entry = entries.iterator().next();
            final String uri = entry.getUri();
            logger.info("Found obj type={} identifier={} uri={}", cls.getSimpleName(), identifier, uri);
            return metadataService.getObjByUri(uri, cls);
        } else if (entries.size() > 1) {
            throw new IllegalStateException("Too many objects with identifier " + identifier + " of type " + cls
                    + " found: " + entries.size());
        } else {
            return creator.get();
        }
    }

    private ProjectMetadataState getOrCreateProjectMetadataState(final Project project) {
        if (!projectMetadataStates.containsKey(project.getId())) {
            projectMetadataStates.put(project.getId(), new ProjectMetadataState());
        }
        return projectMetadataStates.get(project.getId());
    }

    public void ensureDataLoaded(final Project project) {
        final ProjectMetadataState projectMetadataState = getOrCreateProjectMetadataState(project);
        if (!projectMetadataState.isDataLoaded()) {
            logger.info("Loading dataset={} for project={}", DATASET_NAME, project.getId());
            datasetService.loadDataset(project, DATASET_NAME, getClass().getResourceAsStream("/stars.csv")).get();
            projectMetadataState.setDataLoaded(true);
            logger.info("Loaded dataset={} for project={}", DATASET_NAME, project.getId());
        }
    }
}
