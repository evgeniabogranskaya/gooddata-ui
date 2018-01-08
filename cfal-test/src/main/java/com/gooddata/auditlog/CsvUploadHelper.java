/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata.auditlog;

import static org.apache.commons.lang3.Validate.notNull;

import com.gooddata.CfalGoodData;
import com.gooddata.GoodDataException;
import com.gooddata.project.Project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class CsvUploadHelper {

    private static final Logger logger = LoggerFactory.getLogger(CsvUploadHelper.class);

    private static final String CSV_FILE_NAME = "data.csv";
    private static final List<String> CSV_FILE_COLUMN_NAMES = Arrays.asList("name","town");

    private static CsvUploadHelper instance;

    private final CfalGoodData gd;

    private CsvUploadHelper(CfalGoodData gd) {
        notNull(gd, "gd cannot be null!");

        this.gd = gd;
    }

    public static CsvUploadHelper getInstance() {
        if (instance == null) {
            instance = new CsvUploadHelper(CfalGoodData.getInstance());
        }
        return instance;
    }

    /**
     * Uploads "data.csv" to a given project as a new dataset and returns its dataset ID.
     *
     * @param project GD project
     * @return dataset ID
     */
    public String uploadCsv(final Project project) {
        final URL resource = getClass().getClassLoader().getResource(CSV_FILE_NAME);
        if (resource == null) {
            throw new IllegalArgumentException("File resource '" + CSV_FILE_NAME + "' not found.");
        }

        final String csvFilePath = resource.getPath();

        return gd.getCsvUploadService().uploadCsv(project, csvFilePath, CSV_FILE_COLUMN_NAMES);
    }

    /**
     * Silently deletes given CSV dataset.
     *
     * @param project GD project
     * @param datasetId dataset ID
     *
     * @see com.gooddata.dataload.csv.SimpleCsvUploadService#deleteDataset(Project, String)
     */
    public void deleteCsvDataset(final Project project, final String datasetId) {
        try {
            gd.getCsvUploadService().deleteDataset(project, datasetId);
        } catch (GoodDataException e) {
            logger.warn("Could not remove CSV dataset " + datasetId + " in project " + project.getId(), e);
        }
    }
}
