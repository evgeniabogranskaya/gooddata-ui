/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.dataload.csv;

import static com.gooddata.util.Validate.notEmpty;
import static com.gooddata.util.Validate.notNull;

import com.gooddata.*;
import com.gooddata.project.Project;
import org.springframework.core.io.PathResource;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.List;

/**
 * Simple CSV Uploader service which can upload very simple CSV as a new dataset with these rules:
 * <ul>
 *     <li>Header is on the first line</li>
 *     <li>All columns except the last one are of ATTRIBUTE type</li>
 *     <li>Last column is of FACT type</li>
 * </ul>
 */
public class SimpleCsvUploadService extends AbstractService {

    private static final String DATASET_URI = "/gdc/dataload/internal/projects/{projectId}/csv/datasets/{datasetId}";
    private static final UriTemplate DATASET_URI_TEMPLATE = new UriTemplate(DATASET_URI);

    public SimpleCsvUploadService(final RestTemplate restTemplate) {
        super(restTemplate);
    }

    /**
     * Uploads given CSV to the given project as a new dataset with CSV data loaded.
     * The header column names must be specified.
     *
     * @param project GD project
     * @param csvFile CSV file which should be uploaded
     * @param columns header column names
     * @return ID of the new created CSV dataset
     */
    public String uploadCsv(final Project project, final File csvFile, final List<String> columns) {
        notNull(project, "project");
        notNull(csvFile, "csvFile");
        notEmpty(columns, "columns");

        final String stagingUrl = getStagingUrl(project);
        uploadToStaging(stagingUrl, csvFile, project);
        final Load load = createLoad(project, stagingUrl, csvFile, columns);
        final LoadResult loadResult = executeCsvUpload(load).get();

        if (!loadResult.isOk()) {
            final Load errorLoad = getLoadByUri(load.getSelfLink());

            throw new GoodDataException("Execution of CSV upload for project " + project.getId() +
                    " failed with status '" + errorLoad.getStatus() + "' and error message: '" +
                    errorLoad.getErrorMessage() + "'.");
        }

        return load.getDatasetId();
    }

    /**
     * Invokes delete of CSV dataset, all it's associated loads and their data.
     * The CSV dataset delete is async task, but we don't have to care about its result or progress. We just want to
     * invoke this delete process.
     */
    public void deleteDataset(final Project project, final String datasetId) {
        final URI datasetUri = DATASET_URI_TEMPLATE.expand(project.getId(), datasetId);
        try {
            restTemplate.delete(datasetUri);
        } catch (RestClientException e) {
            throw new GoodDataException("Unable to execute CSV dataset delete for dataset with URI " +
                    datasetUri.toString(), e);
        }
    }

    private String getStagingUrl(final Project project) {
        try {
            final StagingInfo info = restTemplate.getForObject(StagingInfo.URI, StagingInfo.class, project.getId());
            return URLDecoder.decode(info.getStagingUrl(), "UTF-8");
        } catch (RestClientException e) {
            throw new GoodDataException("Unable to get pre-signed staging URL for project " + project.getId(), e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Error converting pre-signed staging URL", e);
        }
    }

    private void uploadToStaging(final String stagingUrl, final File csvFile, final Project project) {
        final PathResource data = new PathResource(csvFile.toURI());
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        final HttpEntity<PathResource> requestEntity = new HttpEntity<>(data, headers);

        try {
            new RestTemplate().exchange(stagingUrl, HttpMethod.PUT, requestEntity, Void.class);
        } catch (RestClientException e) {
            throw new GoodDataException("Unable to upload CSV data to S3 for project " + project.getId(), e);
        }
    }

    private Load createLoad(final Project project, final String stagingUrl, final File csvFile,
            final List<String> columns) {
        final Load loadRequest = Load.newLoadRequest(stagingUrl, csvFile.getName(), columns);

        try {
            return restTemplate.postForObject(Load.LOADS_URI, loadRequest, Load.class, project.getId());
        } catch (RestClientException e) {
            throw new GoodDataException("Unable to create CSV upload metadata for project " + project.getId(), e);
        }
    }

    private PollResult<LoadResult> executeCsvUpload(final Load load) {
        final HttpEntity<Object> blankEntity = new HttpEntity<>(new HttpHeaders());
        UploadAsyncTask asyncTask;
        try {
            final ResponseEntity<UploadAsyncTask> responseEntity =
                    restTemplate.exchange(load.getExecutionsLink(), HttpMethod.POST, blankEntity, UploadAsyncTask.class);
            asyncTask = responseEntity.getBody();
        } catch (RestClientException e) {
            throw new GoodDataException("Unable to execute CSV upload task for load " + load.getSelfLink(), e);
        }
        
        if (asyncTask == null) {
            throw new GoodDataException("Missing response for CSV upload execution of load " + load.getSelfLink());
        }
        
        return new PollResult<>(this, new SimplePollHandler<LoadResult>(asyncTask.getUri(), LoadResult.class) {
            @Override
            public void handlePollException(GoodDataRestException e) {
                throw new GoodDataException("Error during CSV upload execution for load " + load.getSelfLink(), e);
            }
        });
    }

    private Load getLoadByUri(final String loadUri) {
        try {
            return restTemplate.getForObject(loadUri, Load.class);
        } catch (RestClientException e) {
            throw new GoodDataException("Error getting load with URI " + loadUri, e);
        }
    }
}
