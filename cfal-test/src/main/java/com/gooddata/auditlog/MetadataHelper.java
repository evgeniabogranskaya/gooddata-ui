/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.GoodData;
import com.gooddata.dataset.DatasetService;
import com.gooddata.md.AbstractObj;
import com.gooddata.md.Attribute;
import com.gooddata.md.Dataset;
import com.gooddata.md.Entry;
import com.gooddata.md.Fact;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.Queryable;
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

import java.util.Collection;
import java.util.function.Supplier;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Singleton for metadata related stuff. Not thread safe.
 */
public class MetadataHelper {

    private static final Logger logger = LoggerFactory.getLogger(MetadataHelper.class);

    private static final String DATASET_NAME = "dataset.star";

    private static MetadataHelper instance;

    private final MetadataService md;

    private final ModelService model;

    private final DatasetService datasetService;

    private final Project project;

    private Report report;

    private ReportDefinition reportDefinition;

    private boolean needSynchronize;

    private MetadataHelper(final GoodData gd, final Project project) {
        notNull(gd, "gd");
        this.md = gd.getMetadataService();
        this.model = gd.getModelService();
        this.datasetService = gd.getDatasetService();
        this.project = notNull(project, "project");
    }

    public static MetadataHelper getInstance(final GoodData gd, final Project project) {
        if (instance == null) {
            instance = new MetadataHelper(gd, project);
        }
        return instance;
    }

    public Report getOrCreateReport() {
        if (report == null) {
            createMetadata();
        }
        return report;
    }

    public ReportDefinition getOrCreateReportDefinition() {
        if (reportDefinition == null) {
            createMetadata();
        }
        return reportDefinition;
    }

    private void createMetadata() {
        final Dataset dataset = getObjOrRunMAQL(Dataset.class, DATASET_NAME,
                "CREATE DATASET {dataset.star} VISUAL(TITLE \"Stars\", DESCRIPTION \"Movie Stars\")"
        );

        final Attribute attr = getObjOrRunMAQL(Attribute.class, "attr.star.name",
                "CREATE ATTRIBUTE {attr.star.name} VISUAL(TITLE \"Department\") AS {f_star.id} FULLSET;",
                "ALTER DATASET {dataset.star} ADD {attr.star.name};",
                "ALTER ATTRIBUTE {attr.star.name} ADD LABELS {label.star.name} VISUAL(TITLE \"Name\") AS {f_star.nm_name};"
        );

        final Fact fact = getObjOrRunMAQL(Fact.class, "fact.star.size",
                "CREATE FACT {fact.star.size} VISUAL(TITLE \"Star Boobs Size\") AS {f_star.f_size};",
                "ALTER DATASET {dataset.star} ADD {fact.star.size};");

        final Metric metric = getObjOrCreateUsingAPI(Metric.class, "metric.avgsize",
                () -> new Metric("Avg size", "SELECT AVG([" + fact.getUri() + "])", "#,##0")
        );

        this.reportDefinition = getObjOrCreateUsingAPI(ReportDefinition.class, "reportdefinition.avgsize",
                () -> GridReportDefinitionContent.create(
                        "Star avg size",
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(attr.getDefaultDisplayForm())),
                        singletonList(new MetricElement(metric, "Avg size")),
                        singletonList(new Filter("(SELECT [" + metric.getUri() + "]) >= 0"))
                )
        );

        this.report = getObjOrCreateUsingAPI(Report.class, "report.avgsize",
                () -> new Report(reportDefinition.getTitle(), reportDefinition)
        );

        if (needSynchronize) {
            logger.info("synchronizing dataset={}", DATASET_NAME);
            model.updateProjectModel(project, "SYNCHRONIZE {dataset.star};").get();
        }
    }

    private <T extends AbstractObj & Queryable> T getObjOrRunMAQL(final Class<T> cls, final String identifier,
                                                                  final String... maql) {
        return getObjOrCreate(cls, identifier, () -> {
                    logger.info("Running MAQL to create type={} identifier={}", cls.getSimpleName(), identifier);
                    model.updateProjectModel(project, maql).get();
                    this.needSynchronize = true;
                    return getObjOrCreate(cls, identifier,
                            () -> {
                                throw new IllegalStateException("Unable to find created object: " + identifier);
                            }
                    );
                }
        );
    }

    private <T extends AbstractObj & Queryable> T getObjOrCreateUsingAPI(final Class<T> cls, final String identifier,
                                                                         final Supplier<T> creator) {
        return getObjOrCreate(cls, identifier,
                () -> {
                    final String type = cls.getSimpleName().toLowerCase();
                    final T obj = creator.get();
                    obj.setIdentifier(identifier);
                    logger.info("Creating obj type={} identifier={}", type, identifier);
                    final T created = md.createObj(project, obj);
                    logger.info("Created obj type={} identifier={} uri={}", type, identifier, created.getUri());
                    return created;
                }
        );
    }

    private <T extends AbstractObj & Queryable> T getObjOrCreate(final Class<T> cls, final String identifier,
                                                                 final Supplier<T> creator) {
        final Collection<Entry> entries = md.find(project, cls, identifier(identifier));
        if (entries.size() == 1) {
            final Entry entry = entries.iterator().next();
            final String uri = entry.getUri();
            logger.info("Found obj type={} identifier={} uri={}", cls.getSimpleName(), identifier, uri);
            return md.getObjByUri(uri, cls);
        } else if (entries.size() > 1) {
            throw new IllegalStateException("Too many objects with identifier " + identifier + " of type " + cls
                    + " found: " + entries.size());
        } else {
            return creator.get();
        }
    }

    public void loadData() {
        logger.info("Loading dataset={}", DATASET_NAME);
        datasetService.loadDataset(project, DATASET_NAME, getClass().getResourceAsStream("/stars.csv")).get();
        logger.info("Loaded dataset={}", DATASET_NAME);
    }
}
