/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.export;

import com.gooddata.auditlog.MetadataHelper;
import com.gooddata.cfal.AbstractAT;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.project.Project;
import org.testng.annotations.BeforeClass;

import java.util.List;
import java.util.function.Predicate;

abstract class AbstractExportAT extends AbstractAT {

    protected static final String MESSAGE_TYPE = "DATA_EXPORT";

    protected MetadataHelper metadata;

    private Project project;

    @BeforeClass
    public void createMetadata() throws Exception {
        project = projectHelper.getOrCreateProject();

        metadata = MetadataHelper.getInstance(gd, project);
    }

    protected Predicate<List<AuditEventDTO>> pageCheckPredicate(final String format) {
        return (auditEvents) -> auditEvents.stream().anyMatch(e ->
                e.getUserLogin().equals(getAccount().getLogin()) &&
                        e.getType().equals(MESSAGE_TYPE) &&
                        e.isSuccess() &&
                        project.getUri().equals(e.getLinks().get("project")) &&
                        format.equals(e.getParams().get("format"))
        );
    }
}
