/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import com.gooddata.GoodData;
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

    private static MetadataHelper instance;

    private final MetadataService md;

    private final ModelService model;

    private final Project project;

    private Report report;

    private ReportDefinition reportDefinition;

    private boolean needSynchronize;

    private MetadataHelper(final GoodData gd, final Project project) {
        notNull(gd, "gd");
        this.md = gd.getMetadataService();
        this.model = gd.getModelService();
        this.project = notNull(project, "project");
    }

    public static MetadataHelper getInstance(final GoodData gd, final Project project) {
        if (instance != null) {
            return instance;
        }
        return instance = new MetadataHelper(gd, project);
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
        final Dataset dataset = getObjOrRunMAQL(Dataset.class, "dataset.person",
                "CREATE DATASET {dataset.person} VISUAL(TITLE \"Person\", DESCRIPTION \"Dataset with Person-related data\")"
        );

        final Attribute attr = getObjOrRunMAQL(Attribute.class, "attr.person.department",
                "CREATE ATTRIBUTE {attr.person.department} VISUAL(TITLE \"Department\") AS {f_person.id} FULLSET;",
                "ALTER DATASET {dataset.person} ADD {attr.person.department};",
                "ALTER ATTRIBUTE {attr.person.department} ADD LABELS {label.person.department} VISUAL(TITLE \"Department\") AS {d_person_department.nm_department};"
        );

        final Fact fact = getObjOrRunMAQL(Fact.class, "fact.person.shoesize",
                "CREATE FACT {fact.person.shoesize} VISUAL(TITLE \"Person Shoe Size\") AS {f_person.f_shoesize};",
                "ALTER DATASET {dataset.person} ADD {fact.person.shoesize};");

        final Metric metric = getObjOrCreateUsingAPI(Metric.class, "metric.avgshoesize",
                () -> new Metric("Avg shoe size", "SELECT AVG([" + fact.getUri() + "])", "#,##0")
        );

        this.reportDefinition = getObjOrCreateUsingAPI(ReportDefinition.class, "reportdefinition.avgshoesize",
                () -> GridReportDefinitionContent.create(
                        "Department avg shoe size",
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(attr.getDefaultDisplayForm())),
                        singletonList(new MetricElement(metric, "Avg shoe size")),
                        singletonList(new Filter("(SELECT [" + metric.getUri() + "]) >= 0"))
                )
        );

        this.report = getObjOrCreateUsingAPI(Report.class, "report.avgshoesize",
                () -> new Report(reportDefinition.getTitle(), reportDefinition)
        );

        if (needSynchronize) {
            logger.info("synchronizing dataset=dataset.person");
            model.updateProjectModel(project, "SYNCHRONIZE {dataset.person};").get();
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
}
