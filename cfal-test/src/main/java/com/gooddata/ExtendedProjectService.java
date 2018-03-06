/*
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */

package com.gooddata;

import com.gooddata.account.AccountService;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectService;
import com.gooddata.publicaccess.PublicProjectArtifact;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class ExtendedProjectService extends ProjectService {

    ExtendedProjectService(RestTemplate restTemplate, AccountService accountService) {
        super(restTemplate, accountService);
    }

    /**
     * Enables public access to project.
     * <br/>
     * This doesn't return anything because we just want to test the SST creation during this process.
     *
     * @param project GD project
     */
    public void enablePublicAccess(final Project project) {
        final PublicProjectArtifact artifact = new PublicProjectArtifact("project");
        try {
            restTemplate.postForObject(project.getPublicArtifactsUri(), artifact, Object.class);
        } catch (RestClientException e) {
            throw new GoodDataException("Could not enable public access to project " + project.getId(), e);
        }
    }
}
